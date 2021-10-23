package tn.iit.controllers;

//import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.dozer.DozerBeanMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import tn.iit.dto.AuthRequest;
import tn.iit.dto.Profile;
import tn.iit.dto.UserDto;
import tn.iit.entities.Role;
import tn.iit.entities.User;
import tn.iit.services.AccountService;
import tn.iit.services.UserService;
import tn.iit.utilities.FileUploadUtil;

@RestController
@RequestMapping("/api")
public class AccountController {
	@Autowired
	private UserService userService;
	@Autowired
	private AccountService accountService;
	@Autowired
	PasswordEncoder encoder;
	@Autowired
	private AuthenticationManager authenticationManager;

	DozerBeanMapper mapper = new DozerBeanMapper();

	@GetMapping("/profile")
	public ResponseEntity<?> getProfile() {
		String currentPrincipalName = getPrincipale();
		Optional<User> user = userService.findByMail(currentPrincipalName);
		if (!(user.isPresent())) {
			return new ResponseEntity<String>("Il n'y a aucun utilisateur avec ce username ", HttpStatus.NOT_FOUND);
		}
		User u = user.get();
		u.setRole(
				new Role(u.getId(), u.getRole().getName().substring(5).replace('_', ' ')));
		return new ResponseEntity<UserDto>(mapper.map(u, UserDto.class), HttpStatus.ACCEPTED);
	}

	@GetMapping("/account/confirm-email/{token}")
	public ResponseEntity<?> getProfile(@PathVariable String token) {
		try {
			accountService.ValidateAcces(token);
			return ResponseEntity.ok("");
		} catch (Exception e) {
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.UNAUTHORIZED);
		}
	}

	@PostMapping("/account/confirm-email/{token}")
	public ResponseEntity<?> validateAcount(@PathVariable String token, @RequestBody AuthRequest req) {
		try {
			accountService.ValidateAcces(token);
			accountService.updateAccount(token, req.getPassword());
		} catch (Exception e) {
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.UNAUTHORIZED);
		}
		return new ResponseEntity<String>("", HttpStatus.OK);
	}

	@PostMapping("/account/update/profile")
	public ResponseEntity<?> updateProfile(@RequestParam("file") MultipartFile file,
			@RequestParam("profileInfo") String profileInfo, @RequestParam("path") String path) throws IOException {
		String currentPrincipalName = getPrincipale();
		ObjectMapper objectMapper = new ObjectMapper();
		Profile profile = objectMapper.readValue(profileInfo, Profile.class);
		Optional<User> currentUser = userService.findByMail(currentPrincipalName);
		if (!(currentUser.isPresent())) {
			return new ResponseEntity<String>("", HttpStatus.NOT_FOUND);
		}
		User user = currentUser.get();
		// String oldImg = currentUser.get().getAvatar();
		try {
			try {
				authenticationManager.authenticate(
						new UsernamePasswordAuthenticationToken(currentPrincipalName, profile.getCurrentPassword()));
			} catch (BadCredentialsException e) {
				return new ResponseEntity<String>("Invalid Password", HttpStatus.FORBIDDEN);
			}
			if (file != null) {
				MultipartFile avatar = file;
				String avatarImg = "User_" + UUID.randomUUID().toString() + "."
						+ FilenameUtils.getExtension(avatar.getOriginalFilename());
				FileUploadUtil.saveFile(path, avatarImg, avatar);
				user.setAvatar(avatarImg);
			}
			if (profile.getNewPassword() != null) {
				user.setPassword(encoder.encode(profile.getNewPassword()));
			}
			user.setUpdatedFields(profile);

		} catch (Exception e) {
			e.printStackTrace();
		}
		User updatedUser = userService.saveUser(user);
		/*
		 * if (file != null && !oldImg.equals("default.png")) { deleteOldImg(path + '/'
		 * + oldImg); }
		 */
		return new ResponseEntity<UserDto>(mapper.map(updatedUser, UserDto.class), HttpStatus.ACCEPTED);
	}

	@PutMapping("/account/update/profile")
	public ResponseEntity<?> updateProfileNoImg(@RequestBody Profile profile) throws IOException {
		String currentPrincipalName = getPrincipale();
		Optional<User> currentUser = userService.findByMail(currentPrincipalName);
		if (!(currentUser.isPresent())) {
			return new ResponseEntity<String>("", HttpStatus.NOT_FOUND);
		}
		User user = currentUser.get();
		try {
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(currentPrincipalName, profile.getCurrentPassword()));
			if (profile.getNewPassword() != null) {
				user.setPassword(encoder.encode(profile.getNewPassword()));
			}
			user.setUpdatedFields(profile);

		} catch (BadCredentialsException e) {
			return new ResponseEntity<String>("Invalid Password", HttpStatus.FORBIDDEN);
		} catch (Exception e) {
			e.printStackTrace();
		}

		User updatedUser = userService.saveUser(user);
		return new ResponseEntity<UserDto>(mapper.map(updatedUser, UserDto.class), HttpStatus.ACCEPTED);
	}

	private String getPrincipale() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication.getName();
	}

	/*
	 * private boolean deleteOldImg(String path) { File img = new File(path); return
	 * img.delete(); }
	 */

}
