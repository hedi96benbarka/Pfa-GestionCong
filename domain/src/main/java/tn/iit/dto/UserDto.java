package tn.iit.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import tn.iit.entities.Contrat;
import tn.iit.entities.Department;
import tn.iit.entities.Role;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class UserDto {

	private long id;
	private String firstName;
	private String lastName;
	private String username;
	private String email;
	private String phoneNumber;
	private String avatar;
	private boolean gender;
	private boolean activeAcount;
	private boolean blockedAccount;
	private Role role;
	private Date createdAt;
	private Date updatedAt;
	private Department department;
	private float solde;
	private float soldeCompensation;
	private Contrat contrat;
	private Date dateRecretement;

}
