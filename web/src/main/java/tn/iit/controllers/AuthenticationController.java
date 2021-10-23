package tn.iit.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tn.iit.dto.AuthRequest;
import tn.iit.dto.AuthResponse;
import tn.iit.security.JwtUtil;

@RestController
@RequestMapping("/api")
public class AuthenticationController {

	@Autowired
	private AuthenticationManager authenticationManager;
	@Autowired
	private JwtUtil jwtUtil;
	@Autowired
	private UserDetailsService UserDetailsService;

	@PostMapping("/authenticate")
	public ResponseEntity<?> authenticate(@RequestBody AuthRequest authUser) throws Exception {
		try {
			authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(authUser.getMail(), authUser.getPassword()));
		} catch (BadCredentialsException e) {
			return new ResponseEntity<String>("Invalid User or Password", HttpStatus.FORBIDDEN);
		}
		final UserDetails userDetail = UserDetailsService.loadUserByUsername(authUser.getMail());
		final String token = jwtUtil.generateToken(userDetail);
		return ResponseEntity.ok(new AuthResponse(token));

	}
}
