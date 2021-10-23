package tn.iit.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tn.iit.entities.Department;
import tn.iit.services.UserService;

@RestController
@RequestMapping("/api")
public class DepartmentController {
	@Autowired
	private UserService userService;
	
	@GetMapping("/departments")
	public ResponseEntity<?> getAllDepartments(){
		return new ResponseEntity<List<Department>>(userService.getAllDepart() , HttpStatus.OK);
	}
}
