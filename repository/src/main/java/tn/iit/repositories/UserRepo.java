package tn.iit.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import tn.iit.entities.User;

public interface UserRepo extends JpaRepository<User, Long> {

	Optional<User> findByUsername(String userName);

	Boolean existsByUsername(String username);

	Boolean existsByEmail(String email);

	Optional<User> findByEmail(String email);

	Optional<User> findFirstByOrderByIdDesc();

	List<User> findAllByEmailNotAndDepartmentId(String principale, long id);

	List<User> findAllByRoleName(String string);

}
