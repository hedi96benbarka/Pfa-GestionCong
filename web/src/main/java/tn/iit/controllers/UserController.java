package tn.iit.controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.apache.commons.lang3.time.DateUtils;
import org.dozer.DozerBeanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tn.iit.dto.Mail;
import tn.iit.dto.RoleDto;
import tn.iit.dto.UserDto;
import tn.iit.entities.Contrat;
import tn.iit.entities.Role;
import tn.iit.entities.SoldeHistory;
import tn.iit.entities.SoldeHistory.Operation;
import tn.iit.entities.User;
import tn.iit.entities.UserUtils;
import tn.iit.services.AccountService;
import tn.iit.services.ContratService;
import tn.iit.services.UserService;
import tn.iit.utilities.MapValidationErrorService;
import tn.iit.utilservices.GeneratorService;
import tn.iit.utilservices.MailService;

@RestController
@RequestMapping("/api/admin/users")
public class UserController {

	@Autowired
	private UserService userService;
	@Autowired
	private ContratService contratService;
	@Autowired
	private MapValidationErrorService MapErrors;
	@Autowired
	PasswordEncoder encoder;
	@Autowired
	private AccountService accountService;
	@Autowired
	private MailService mailService;
	@Autowired
	private GeneratorService generatorService;

	@Value("${tn.iit.emailValidation}")
	private String hostLink;

	private DozerBeanMapper mapper = new DozerBeanMapper();
	private static Logger log = LoggerFactory.getLogger(UserController.class);
	private SimpleDateFormat formatDate = new SimpleDateFormat("dd-MM-yyyy");

	@GetMapping("")
	public List<UserDto> getAll() {
		List<User> users = userService.findAll();
		return users.stream().map(user -> mapper.map(user, UserDto.class)).collect(Collectors.toList());
	}

	@GetMapping("/{username}")
	public ResponseEntity<?> getByUsername(@PathVariable String username) {
		Optional<User> user = userService.findByUserName(username);
		if (!(user.isPresent())) {
			return new ResponseEntity<String>("Il n'y a aucun utilisateur avec ce username ", HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<UserDto>(mapper.map(user.get(), UserDto.class), HttpStatus.ACCEPTED);
	}

	@GetMapping("/contracts")
	public List<Contrat> getAllContracts() {
		return contratService.getAllContract();
	}

	@PostMapping("")
	public ResponseEntity<?> createUser(@Valid @RequestBody UserDto userDto, BindingResult result) {
		ResponseEntity<?> errorMap = MapErrors.MapValidationService(result);
		if (errorMap != null)
			return errorMap;
		User u;
		try {
			u = userService.saveUser(bindUser(userDto));
			if (u == null)
				return new ResponseEntity<String>("Error produced when creating the user",
						HttpStatus.EXPECTATION_FAILED);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<String>("Error produced when creating the user", HttpStatus.EXPECTATION_FAILED);
		}
		UserUtils account = accountService.createToken(u);
		emailSender(account);
		return new ResponseEntity<UserDto>(mapper.map(u, UserDto.class), HttpStatus.CREATED);
	}

	@GetMapping("/account/state/{username}")
	public ResponseEntity<?> changeAcountState(@PathVariable String username) {
		Optional<User> user = userService.findByUserName(username);
		if (!(user.isPresent())) {
			return new ResponseEntity<String>("Il n'y a aucun utilisateur avec ce username ", HttpStatus.NOT_FOUND);
		}
		User u = user.get();
		u.setBlockedAccount(!u.isBlockedAccount());
		return new ResponseEntity<UserDto>(mapper.map(userService.saveUser(u), UserDto.class), HttpStatus.OK);

	}

	@GetMapping("/roles")
	public ResponseEntity<?> getRoles() {
		List<Role> roles = userService.getRoles();
		List<RoleDto> dtoRoles = roles.stream().map(r -> {
			String role = r.getName().substring(5);
			role = role.replace('_', ' ');
			return new RoleDto(r.getId(), role);
		}).collect(Collectors.toList());
		return new ResponseEntity<List<RoleDto>>(dtoRoles, HttpStatus.OK);

	}

	@PostMapping("/role")
	public ResponseEntity<?> updateRole(@RequestBody Role role) {
		String name = role.getName();
		if (role.getId() == 1 || role.getId() == 2)
			return new ResponseEntity<String>("this role cannot be updated", HttpStatus.METHOD_NOT_ALLOWED);
		role.setName(("ROLE_" + role.getName().replace(' ', '_')).toUpperCase());
		Role r = userService.saveRole(role);
		r.setName(name);
		return new ResponseEntity<Role>(r, HttpStatus.OK);
	}

	@PostMapping("/change/roles")
	public ResponseEntity<?> updateUserRole(@RequestBody UserDto u) {
		Optional<User> us = userService.findByUserName(u.getUsername());
		if (!(us.isPresent())) {
			return new ResponseEntity<String>("Il n'y a aucun utilisateur avec ce username ", HttpStatus.NOT_FOUND);
		}
		User user = us.get();
		user.setRole(u.getRole());
		return new ResponseEntity<UserDto>(mapper.map(userService.saveUser(user), UserDto.class), HttpStatus.OK);
	}

	@PostMapping("/change/department")
	public ResponseEntity<?> updateDepartments(@RequestBody UserDto u) {
		Optional<User> us = userService.findByUserName(u.getUsername());
		if (!(us.isPresent())) {
			return new ResponseEntity<String>("Il n'y a aucun utilisateur avec ce username ", HttpStatus.NOT_FOUND);
		}
		User user = us.get();
		user.setDepartment(u.getDepartment());
		return new ResponseEntity<UserDto>(mapper.map(userService.saveUser(user), UserDto.class), HttpStatus.OK);
	}

	private String generatePass(User user) {
		String pass = "";
		if (user.getFirstName().length() > 2)
			pass += user.getFirstName().substring(0, 3).toUpperCase();
		if (user.getLastName().length() > 2)
			pass += user.getLastName().substring(0, 3);
		if (user.getPhoneNumber().length() > 5)
			pass += user.getPhoneNumber().substring(0, 5);
		return pass;

	}

	private void emailSender(UserUtils account) {

		log.info("START... Sending email");
		Mail mail = new Mail();
		mail.setFrom("3f2700aabd-56dfb2@inbox.mailtrap.io");
		mail.setMailTo(account.getUser().getEmail());
		mail.setSubject("Vérification de compte");

		try {
			Map<String, Object> model = new HashMap<String, Object>();
			model.put("fname", account.getUser().getFirstName());
			model.put("lname", account.getUser().getLastName());
			model.put("username", account.getUser().getFirstName() + " " + account.getUser().getLastName());
			model.put("validationLink", hostLink);
			model.put("token", account.getToken());
			model.put("expirationDate", parseDate(account.getCreatedAt(), account.getDuration()));
			mail.setProps(model);
			mailService.sendValidationMail(mail);
			log.info("END... Email sent success");
		} catch (Exception e) {
			System.out.println(e.getCause());
			log.info(e.getMessage());
		}

	}

	private String parseDate(Date inputDate, int duration) throws ParseException {
		Date expirationDate = DateUtils.addMinutes(inputDate, duration);
		return formatDate.format(expirationDate);
	}

	private User bindUser(UserDto userDto) throws Exception {
		User user = mapper.map(userDto, User.class);
		user.addHistory(new SoldeHistory(userDto.getSolde(), getPrincipale(), Operation.CREATED, user));
		user.setSolde(userDto.getSolde());
		user.setPassword(encoder.encode(generatePass(user)));
		user.setAvatar("default.png");
		user.setUsername(generatorService.userIdGenerator());
		return user;
	}

	private String getPrincipale() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication.getName();
	}

}
