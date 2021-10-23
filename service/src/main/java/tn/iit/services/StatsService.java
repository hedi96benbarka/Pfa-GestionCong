package tn.iit.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tn.iit.entities.User;
import tn.iit.repositories.UserRepo;

@Service
public class StatsService {

	@Autowired
	private UserRepo userRepo;

	public float getUserStats(String user) {
		Optional<User> u = userRepo.findByEmail(user);
		return u.get().getSolde();

	}
}
