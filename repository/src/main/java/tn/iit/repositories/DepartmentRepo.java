package tn.iit.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import tn.iit.entities.Department;

public interface DepartmentRepo extends JpaRepository<Department, Long>{

	Optional<Department> findFirstByDepartmentName(String string);

}
