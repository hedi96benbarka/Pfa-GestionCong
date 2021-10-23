package tn.iit.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tn.iit.entities.Department;
import tn.iit.entities.Role;
import tn.iit.entities.User;
import tn.iit.repositories.DepartmentRepo;
import tn.iit.repositories.RoleRepo;
import tn.iit.repositories.UserRepo;

@Service
public class UserService {

	@Autowired
	private UserRepo userRepo;
	@Autowired
	private DepartmentRepo departmentRepo;
	@Autowired
	private RoleRepo roleRepo;

	public List<User> findAll() {
		return userRepo.findAll();
	}

	public Optional<User> findByUserName(String username) {
		return userRepo.findByUsername(username);
	}

	public Optional<User> findByID(Long id) {
		return userRepo.findById(id);
	}

	public User saveUser(User user) {
		return userRepo.save(user);
	}
	public Optional<User> findByMail(String currentPrincipalName) {
		return userRepo.findByEmail(currentPrincipalName);
	}

	public List<Department> getAllDepart() {
		return departmentRepo.findAll();
	}

	public List<Role> getRoles() {
		return roleRepo.findAll();
	}

	public Optional<Role> findRole(long id) {
		return roleRepo.findById(id);
	}
	
	public Role saveRole(Role role) {
		return roleRepo.save(role);
	}

	public List<User> getAllSameDepartment(String principale) {
		
		return userRepo.findAllByEmailNotAndDepartmentId(principale,userRepo.findByEmail(principale).get().getDepartment().getId());
	}

}
