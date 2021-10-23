package tn.iit.utilservices;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tn.iit.entities.User;
import tn.iit.repositories.UserRepo;

@Service
public class GeneratorService {
	@Autowired
	private UserRepo userRepo;

	public String userIdGenerator() {
		long userId = 0;
		Optional<User> user = userRepo.findFirstByOrderByIdDesc();
		if (user.isPresent())
			userId = user.get().getId();
		userId++;
		return "USER_" + userId;
	}
}
