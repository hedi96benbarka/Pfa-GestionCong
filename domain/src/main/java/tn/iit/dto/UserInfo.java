package tn.iit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import tn.iit.entities.Department;
import tn.iit.entities.Role;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class UserInfo {
	private long id;
	private String firstName;
	private String lastName;
	private String fullName;
	private Role role;
	private String email;
	private String phoneNumber;
	private Department department;
	private float solde;
	private float soldeCompensation;
}
