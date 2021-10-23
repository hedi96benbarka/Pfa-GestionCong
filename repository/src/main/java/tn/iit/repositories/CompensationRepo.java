package tn.iit.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import tn.iit.entities.Compensation;

public interface CompensationRepo extends JpaRepository<Compensation, Long> {

	List<Compensation> findAllByUserEmail(String userMail);

}
