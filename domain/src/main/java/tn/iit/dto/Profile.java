package tn.iit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import tn.iit.entities.Department;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Profile {
	private long id;
	private String firstName;
	private String lastName;
	private String username;
	private String phoneNumber;
	private boolean gender;
	private Department department;
	private String currentPassword;
	private String newPassword;
}
