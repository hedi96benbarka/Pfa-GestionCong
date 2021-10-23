package tn.iit.controllers;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
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

import tn.iit.dto.LeavesDto;
import tn.iit.dto.Mail;
import tn.iit.dto.UserInfo;
import tn.iit.dto.UserLeavesDto;
import tn.iit.entities.Leave;
import tn.iit.entities.Leave.HalfDay;
import tn.iit.entities.LeaveState;
import tn.iit.entities.SoldeHistory;
import tn.iit.entities.SoldeHistory.Operation;
import tn.iit.entities.User;
import tn.iit.exceptions.GeneralException;
import tn.iit.services.LeaveService;
import tn.iit.services.UserService;
import tn.iit.utilities.FileUploadUtil;
import tn.iit.utilservices.MailService;

@RestController
@RequestMapping("/api/leaves")
public class LeaveController {
	@Autowired
	private LeaveService leaveService;
	@Autowired
	private UserService userService;
	@Autowired
	private MailService mailService;

	private DozerBeanMapper mapper = new DozerBeanMapper();

	@GetMapping("/users")
	public ResponseEntity<?> getAllUserSameDep() {
		List<UserInfo> usersList = userService.getAllSameDepartment(getPrincipale()).stream().map(row -> {
			UserInfo dto = mapper.map(row, UserInfo.class);
			dto.setFullName(dto.getFirstName() + " " + dto.getLastName());
			return dto;
		}).collect(Collectors.toList());
		return new ResponseEntity<List<UserInfo>>(usersList, HttpStatus.OK);
	}

	@GetMapping("")
	public ResponseEntity<?> getAllUserLeaves() {
		List<UserLeavesDto> lDto = leaveService.getUserLeaves(getPrincipale()).stream().map(row -> {
			UserLeavesDto dto = mapper.map(row, UserLeavesDto.class);
			dto.calculateDetails();
			return dto;
		}).collect(Collectors.toList());
		return new ResponseEntity<List<UserLeavesDto>>(lDto, HttpStatus.OK);
	}

	@GetMapping("/all")
	public ResponseEntity<?> getAllLeaves() {

		List<LeavesDto> lDto = leaveService.getAllLeaves().stream().map(row -> {
			LeavesDto dto = mapper.map(row, LeavesDto.class);
			dto.calculateDetails();
			return dto;
		}).collect(Collectors.toList());

		return new ResponseEntity<List<LeavesDto>>(lDto, HttpStatus.OK);
	}

	@PostMapping("")
	public ResponseEntity<?> addLeaveWithDocument(@RequestPart("path") String path, @RequestPart("data") String data,
			@RequestPart("file") MultipartFile file) throws IOException {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			Leave leave = objectMapper.readValue(data, Leave.class);
			User user = (userService.findByMail(getPrincipale()).orElseThrow(() -> new GeneralException("not Found")));
			List<Leave> leaveLists = leaveService
					.checkLeaves(user.getId(), leave.getLeaveStartDate(), leave.getLeaveStartDate()).stream()
					.filter(item -> item.getEtat() != LeaveState.CANCELLED).collect(Collectors.toList());
			if (leaveLists.size() > 0 && leave.getId() < 1)
				throw new GeneralException("Vous avez déjà demandé un congé pendant cette période ");
			leave.setUser(user);
			if (file != null) {
				String document = "Document_" + UUID.randomUUID().toString() + "."
						+ FilenameUtils.getExtension(file.getOriginalFilename());
				FileUploadUtil.saveFile(path, document, file);
				leave.setDocument(document);
			}
			user.addLeave(leave);
			userService.saveUser(user);
			if (leave.getId() > 0) {
				UserLeavesDto dto = mapper.map(leaveService.findLeave(leave.getId()).get(), UserLeavesDto.class);
				dto.calculateDetails();
				return new ResponseEntity<UserLeavesDto>(dto, HttpStatus.ACCEPTED);
			}
			return new ResponseEntity<String>("Done", HttpStatus.ACCEPTED);
		} catch (Exception e) {
			throw new GeneralException(e.getMessage());
		}

	}

	@PostMapping("/add")
	public ResponseEntity<?> addLeaveWithDocument(@RequestPart("data") String data) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			Leave leave = objectMapper.readValue(data, Leave.class);
			User user = (userService.findByMail(getPrincipale()).orElseThrow(() -> new GeneralException("not Found")));
			List<Leave> leaveLists = leaveService
					.checkLeaves(user.getId(), leave.getLeaveStartDate(), leave.getLeaveStartDate()).stream()
					.filter(item -> item.getEtat() != LeaveState.CANCELLED).collect(Collectors.toList());
			if (leaveLists.size() > 0 && leave.getId() < 1)
				throw new GeneralException("Vous avez déjà demandé un congé pendant cette période ");
			leave.setUser(user);
			if (leave.getId() > 0) {
				UserLeavesDto dto = mapper.map(leaveService.saveLeave(leave), UserLeavesDto.class);
				dto.calculateDetails();
				return new ResponseEntity<UserLeavesDto>(dto, HttpStatus.ACCEPTED);
			} else {
				user.addLeave(leave);
				userService.saveUser(user);
			}
			return new ResponseEntity<String>("Done", HttpStatus.ACCEPTED);

		} catch (Exception e) {
			throw new GeneralException(e.getMessage());
		}

	}

	private String getPrincipale() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication.getName();
	}

	@GetMapping("/cancel/{id}")
	public ResponseEntity<?> cancelLeave(@PathVariable long id) {
		Leave l = leaveService.findLeave(id).orElseThrow(() -> new GeneralException("can't found this item"));
		if (l.getEtat() != LeaveState.PENDING)
			throw new GeneralException("the state of this item already changed");
		l.setEtat(LeaveState.CANCELLED);
		UserLeavesDto dto = mapper.map(leaveService.saveLeave(l), UserLeavesDto.class);
		dto.calculateDetails();
		return new ResponseEntity<UserLeavesDto>(dto, HttpStatus.OK);
	}

	@PostMapping("/confirm/{confirmation}")
	public ResponseEntity<?> confirmation(@PathVariable int confirmation, @RequestBody Leave l)
			throws MessagingException {
		Leave leave = leaveService.findLeave(l.getId())
				.orElseThrow(() -> new GeneralException("can't found this item"));
		if (leave.getUser().getEmail().equals(getPrincipale()))
			throw new GeneralException("Tu ne peut pas confirmer votre propre demande");
		leave.setRhNote(l.getRhNote());
		if (leave.getEtat() != LeaveState.PENDING)
			throw new GeneralException("the state of this item already changed");
		final float duration = calculateSolde(leave);
		Mail m = new Mail("IITReplySystem", leave.getUser().getEmail(), "Confirmation du Congé");
		if (confirmation > 0) {
			updateSolde(leave.getUser(), duration);
			leave.setEtat(LeaveState.APPROVED);
			mailService.sendTextMail(m, mailText(true, leave));
		} else {
			leave.setEtat(LeaveState.REFUSED);
			mailService.sendTextMail(m, mailText(false, leave));
		}

		LeavesDto dto = mapper.map(leaveService.saveLeave(leave), LeavesDto.class);
		return new ResponseEntity<Object>(dto, HttpStatus.OK);
	}

	private String mailText(boolean confirmed, Leave leave) {
		String pattern = "dd-MM-yyyy";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		if (confirmed)
			return "Bonjour\nNous vous informons que votre demande de congé du "
					+ simpleDateFormat.format(leave.getLeaveStartDate()) + " au "
					+ simpleDateFormat.format(leave.getLeaveStartDate()) + " a été approuvée\nCordialement,\nResponsable Rh";
		return "Bonjour\nNous sommes désolés de vous informer que votre demande de congé du "
				+ simpleDateFormat.format(leave.getLeaveStartDate()) + " au "
				+ simpleDateFormat.format(leave.getLeaveStartDate()) + " n'a pas été approuvée \nCordialement,\nResponsable Rh";
	}

	private void updateSolde(User user, float duration) {
		user.addHistory(new SoldeHistory(duration, getPrincipale(), Operation.SUBTRACTED, user));
		user.setSolde(user.getSolde() - duration);
		userService.saveUser(user);
	}

	private float calculateSolde(Leave leave) {
		try {
			float diff = TimeUnit.DAYS.convert(
					Math.abs(leave.getLeaveStartDate().getTime() - leave.getLeaveEndDate().getTime()),
					TimeUnit.MILLISECONDS);
			if (diff == 0)
				diff++;
			if (diff <= 1) {
				switch (leave.getStartHalfDay()) {
				case MORNING:
					diff -= 0.5;
					break;
				case AFTERNOON:
					diff -= 0.5;
					break;
				default:
					break;
				}
				return diff;
			}
			if (leave.getStartHalfDay() != HalfDay.FULLDAY && leave.getEndHalfDay() != HalfDay.FULLDAY
					&& leave.getEndHalfDay() != HalfDay.MORNING)
				diff -= 0.5;
			return diff;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;

	}
}
