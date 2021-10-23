package tn.iit.security;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tn.iit.entities.Compensation;
import tn.iit.repositories.CompensationRepo;

@Service
public class CompensationService {

	@Autowired
	private CompensationRepo compensationRepo;
	
	public List<Compensation> getAllCompensations(String principle) {
		return compensationRepo.findAllByUserEmail(principle);
	}
	
	public Compensation addCompensation(Compensation compensation) {
		return compensationRepo.save(compensation);
	}

	public List<Compensation> getAll() {
		return compensationRepo.findAll();
	}

	public Optional<Compensation> findCompensation(long id) {
		return compensationRepo.findById(id);
	}
}
