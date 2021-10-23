package tn.iit.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tn.iit.entities.Department;
import tn.iit.entities.Role;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SimpleUserDto {
	private long id;
	private String firstName;
	private String lastName;
	private Role role;
	private String email;
	private String phoneNumber;
	private Department department;
	private float solde;
	private float soldeCompensation;
}
