package tn.iit.controllers;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.mail.MessagingException;

import org.apache.commons.io.FilenameUtils;
import org.dozer.DozerBeanMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import tn.iit.dto.CompensationDto;
import tn.iit.dto.Mail;
import tn.iit.entities.Compensation;
import tn.iit.entities.Compensation.CompensationState;
import tn.iit.entities.User;
import tn.iit.exceptions.GeneralException;
import tn.iit.security.CompensationService;
import tn.iit.services.UserService;
import tn.iit.utilities.FileUploadUtil;
import tn.iit.utilservices.MailService;

@RestController
@RequestMapping("/api")
public class CompensationController {

	@Autowired
	private CompensationService compensationService;
	@Autowired
	private UserService userService;
	@Autowired
	private MailService mailService;

	private DozerBeanMapper mapper = new DozerBeanMapper();

	@GetMapping("/compensation/by-user")
	public List<CompensationDto> findAll() {
		return compensationService.getAllCompensations(getPrincipale()).stream().map(row -> {
			CompensationDto cmpdTO = mapper.map(row, CompensationDto.class);
			cmpdTO.setCompensationAmount(row.calculateCompensation());
			return cmpdTO;
		}).collect(Collectors.toList());
	}

	@GetMapping("/rh/compensations")
	public List<CompensationDto> getAll() {
		return compensationService.getAll().stream().map(row -> {
			CompensationDto cmpdTO = mapper.map(row, CompensationDto.class);
			cmpdTO.setCompensationAmount(row.calculateCompensation());
			return cmpdTO;
		}).collect(Collectors.toList());
	}

	@PostMapping("/compensation")
	public ResponseEntity<?> addCompensationWithDocument(@RequestPart("path") String path,
			@RequestPart("data") String data, @RequestPart("file") MultipartFile file) throws IOException {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			Compensation compensation = objectMapper.readValue(data, Compensation.class);
			User user = (userService.findByMail(getPrincipale()).orElseThrow(() -> new GeneralException("not Found")));
			compensation.setUser(user);
			if (file != null) {
				String document = "Document_" + UUID.randomUUID().toString() + "."
						+ FilenameUtils.getExtension(file.getOriginalFilename());
				FileUploadUtil.saveFile(path, document, file);
				compensation.setDocument(document);
			}
			user.addCompensation(compensation);
			userService.saveUser(user);
			return new ResponseEntity<String>("Done", HttpStatus.ACCEPTED);
		} catch (Exception e) {
			throw new GeneralException(e.getMessage());
		}
	}

	@PostMapping("/compensation/add")
	public ResponseEntity<?> addCompensation(@RequestPart("data") String data) throws IOException {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			Compensation compensation = objectMapper.readValue(data, Compensation.class);
			User user = (userService.findByMail(getPrincipale()).orElseThrow(() -> new GeneralException("not Found")));
			compensation.setUser(user);
			user.addCompensation(compensation);
			userService.saveUser(user);
			return new ResponseEntity<String>("Done", HttpStatus.ACCEPTED);
		} catch (Exception e) {
			throw new GeneralException(e.getMessage());
		}
	}

	@PostMapping("rh/compensation/confirm/{confirmation}")
	public ResponseEntity<?> confirmation(@PathVariable int confirmation, @RequestBody Compensation c)
			throws MessagingException {
		Compensation compensation = compensationService.findCompensation(c.getId())
				.orElseThrow(() -> new GeneralException("can't found this item"));
		User user = compensation.getUser();
		if (user.getEmail().equals(getPrincipale()))
			throw new GeneralException("Tu ne peut pas confirmer votre propre demande");
		compensation.setRhNote(c.getRhNote());
		if (compensation.getEtat() != CompensationState.PENDING)
			throw new GeneralException("the state of this item already changed");
		Mail m = new Mail("IITReplySystem", user.getEmail(), "Reponse a la demande de compensation");
		if (confirmation > 0) {
			compensation.setEtat(CompensationState.APPROVED);
			mailService.sendTextMail(m, mailText(true, compensation));
			user.setSolde(user.getSolde() + compensation.calculateCompensation());
		} else {
			compensation.setEtat(CompensationState.REFUSED);
			mailService.sendTextMail(m, mailText(false, compensation));
		}
		userService.saveUser(user);
		return new ResponseEntity<CompensationDto>(
				mapper.map(compensationService.addCompensation(compensation), CompensationDto.class), HttpStatus.OK);
	}

	private String getPrincipale() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication.getName();
	}

	private String mailText(boolean confirmed, Compensation comp) {
		final String pattern = "dd-MM-yyyy";
		final float nbJour = comp.calculateCompensation();
		String count = "";
		if ((nbJour - (int) nbJour) == 0) {
			count = (int) nbJour + (nbJour >= 2 ? " jours" : " jour");
		} else {
			count = nbJour + (nbJour >= 2 ? " jours" : " jour");
		}
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		if (confirmed)
			return "Bonjour\nNous vous informons que votre demande de Compensation de " + count + " du "
					+ simpleDateFormat.format(comp.getCompStartDate()) + " au "
					+ simpleDateFormat.format(comp.getCompEndDate())
					+ " a été approuvée\nCordialement,\nResponsable Rh";
		return "Bonjour\nNous sommes désolés de vous informer que votre demande de Compensation " + count + " du "
				+ simpleDateFormat.format(comp.getCompStartDate()) + "au"
				+ simpleDateFormat.format(comp.getCompEndDate())
				+ "n'a pas été approuvée \nCordialement,\nResponsable Rh";
	}
}
