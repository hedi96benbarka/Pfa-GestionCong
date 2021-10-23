package tn.iit.repositories;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import tn.iit.entities.Leave;
import tn.iit.entities.LeaveState;

public interface LeaveRepo extends JpaRepository<Leave, Long> {

	List<Leave> findAllByUserEmailOrderByCreatedAt(String user);

	List<Leave> findAllByUserIdAndLeaveStartDateLessThanEqualAndLeaveEndDateGreaterThanEqual(Long id,Date start, Date end);

	List<Leave> findAllByNotifyAndEtat(boolean b, LeaveState pending);

}
