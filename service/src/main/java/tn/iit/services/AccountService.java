package tn.iit.services;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import tn.iit.entities.User;
import tn.iit.entities.UserUtils;
import tn.iit.exceptions.GeneralException;
import tn.iit.repositories.UserRepo;
import tn.iit.repositories.UserUtilsRepo;

@Service
public class AccountService {

	@Autowired
	private UserUtilsRepo userUtilRepo;
	@Autowired
	UserRepo userRepo;
	@Autowired
	PasswordEncoder encoder;

	public boolean ValidateAcces(String token) {
		Optional<UserUtils> accountInfo = userUtilRepo.findByTokenAndState(token, false);
		accountInfo.orElseThrow(() -> new GeneralException("not valid Request"));
		UserUtils ut = accountInfo.get();
		if (!validateExpiration(ut.getCreatedAt(), ut.getDuration()))
			accountInfo.orElseThrow(() -> new GeneralException("not valid Request"));
		return true;
	}

	public UserUtils createToken(User user) {
		UserUtils account = new UserUtils();
		account.setToken(UUID.randomUUID().toString() + "-" + user.getUsername());
		account.setDuration(10080);
		account.setUser(user);
		userUtilRepo.save(account);
		return account;
	}

	private boolean validateExpiration(Date expirationDate, int duration) {
		Date currentDate = new Date();
		expirationDate = DateUtils.addMinutes(expirationDate, duration);
		if (currentDate.after(expirationDate))
			return false;
		return true;
	}

	public boolean updateAccount(String token, String password) {
		Optional<UserUtils> accountInfo = userUtilRepo.findByTokenAndState(token, false);
		accountInfo.orElseThrow(() -> new GeneralException("not valid Request"));
		User user = accountInfo.get().getUser();
		if (user == null)
			return false;
		user.setPassword(encoder.encode(password));
		user.setActiveAcount(true);
		userRepo.save(user);
		UserUtils ut = accountInfo.get();
		ut.setState(true);
		userUtilRepo.save(ut);
		return true;
	}
}
