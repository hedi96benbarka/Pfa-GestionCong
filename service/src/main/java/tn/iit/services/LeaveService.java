package tn.iit.services;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tn.iit.entities.Leave;
import tn.iit.repositories.LeaveRepo;

@Service
public class LeaveService {

	@Autowired
	private LeaveRepo leaveRepo;

	public List<Leave> getUserLeaves(String user) {
		return leaveRepo.findAllByUserEmailOrderByCreatedAt(user);
	}

	public Optional<Leave> findLeave(long id) {
		return leaveRepo.findById(id);

	}

	public Leave saveLeave(Leave leave) {
		return leaveRepo.save(leave);
	}

	public List<Leave> getAllLeaves() {
		return leaveRepo.findAll();
	}

	public List<Leave> checkLeaves(Long id, Date start, Date end) {
		return leaveRepo.findAllByUserIdAndLeaveStartDateLessThanEqualAndLeaveEndDateGreaterThanEqual(id, start, end);
	}

}
