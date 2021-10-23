package tn.iit.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import tn.iit.entities.UserUtils;

public interface UserUtilsRepo extends JpaRepository<UserUtils, Long> {
	
	Optional<UserUtils> findByTokenAndState(String token , Boolean status);
}
