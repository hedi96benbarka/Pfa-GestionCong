package tn.iit.utilservices;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import tn.iit.entities.Department;
import tn.iit.entities.Role;
import tn.iit.entities.SoldeHistory;
import tn.iit.entities.SoldeHistory.Operation;
import tn.iit.entities.User;
import tn.iit.repositories.DepartmentRepo;
import tn.iit.repositories.RoleRepo;
import tn.iit.repositories.UserRepo;

@Component
public class DbInitializer implements ApplicationListener<ContextRefreshedEvent> {

	@Autowired
	private UserRepo userRepo;

	@Autowired
	private RoleRepo roleRepo;

	@Autowired
	private DepartmentRepo departmentRepo;

	@Autowired
	private PasswordEncoder encoder;

	@Autowired
	private GeneratorService generatorServ;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		if (roleRepo.count() < 3)
			initialiseRoles();

		if (departmentRepo.count() < 1)
			initialiseDepartment();

		if (userRepo.count() < 1)
			initialiseUser();

	}

	private void initialiseRoles() {
		final int solde = 21600;
		final List<String> roles = Arrays.asList("ROLE_ADMIN", "ROLE_RH", "ROLE_Salarié", "Role_Enseignant_Permanent",
				"Role_Enseignant_Non_Permanent");
		roles.forEach(role -> roleRepo.save(new Role(role, solde)));

	}

	private void initialiseDepartment() {
		final List<String> departments = Arrays.asList("Administration", "Financier", "Genie Informatique",
				"Genie Mecanique", "Genie Civil");
		departments.forEach(dep -> departmentRepo.save(new Department(dep, "Department " + dep)));
	}

	private void initialiseUser() {
		Optional<Role> role = roleRepo.findFirstByName("ROLE_ADMIN");
		Optional<Role> roleRh = roleRepo.findFirstByName("ROLE_RH");
		Optional<Role> roleEmp = roleRepo.findFirstByName("ROLE_SALARIÉ");
		Optional<Department> department = departmentRepo.findFirstByDepartmentName("Administration");
		if (role.isPresent() && department.isPresent()) {
			User admin = new User("Admin", "Admin", generatorServ.userIdGenerator(), "admin@iit.tn", "55180612",
					encoder.encode("admin").toString(), role.get(), department.get(), true, 21);
			SoldeHistory sh1 = new SoldeHistory(21, "System", Operation.CREATED, admin);
			admin.addHistory(sh1);
			userRepo.save(admin);
			User rh = new User("Rh", "Rh", generatorServ.userIdGenerator(), "rh@iit.tn", "55180612",
					encoder.encode("rh").toString(), roleRh.get(), department.get(), true, 21);
			SoldeHistory sh2 = new SoldeHistory(21, "System", Operation.CREATED, rh);
			rh.addHistory(sh2);
			userRepo.save(rh);
			User emp = new User("Employer", "Employer", generatorServ.userIdGenerator(), "employer@iit.tn", "55180612",
					encoder.encode("employer").toString(), roleEmp.get(), department.get(), true, 21);
			SoldeHistory sh3 = new SoldeHistory(21, "System", Operation.CREATED, emp);
			emp.addHistory(sh3);
			userRepo.save(emp);
		}

	}

}
