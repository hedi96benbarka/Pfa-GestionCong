package tn.iit.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import tn.iit.entities.Role;


public interface RoleRepo extends JpaRepository<Role, Long> {

    Optional<Role> findFirstByName(String roleAdmin);

    Optional<Role> findRoleByName(String role);

}
