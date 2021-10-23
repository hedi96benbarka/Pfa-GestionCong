package tn.iit.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tn.iit.dto.Profile;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@NotBlank(message = "Nom d'utilisateur est nécessaire ")
	@Column(unique = true, nullable = false)
	private String username;

	@NotBlank(message = "Le prénom est requis")
	private String firstName;

	@NotBlank(message = "Le nom est obligatoire")
	private String lastName;

	@NotBlank(message = "Adresse e-mail est nécessaire ")
	@Email(message = "Format d'email invalide")
	@Column(unique = true)
	private String email;

	@NotBlank(message = "Le numéro de téléphone est obligatoire ")
	private String phoneNumber;

	private String password;

	private String avatar;

	private boolean gender;

	private float solde;
	
	private Date dateRecretement;
	
	private boolean activeAcount = false;

	private boolean blockedAccount = false;

	@JsonFormat(pattern = "yyyy-MM-dd")
	@Column(updatable = false)
	private Date createdAt;

	@JsonFormat(pattern = "yyyy-MM-dd")
	private Date updatedAt;

	@ManyToOne
	private Role role;

	@ManyToOne
	private Department department;
	
	@ManyToOne
	private Contrat contrat;

	@OneToMany(cascade = CascadeType.ALL)
	private List<Leave> leaves;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<SoldeHistory> updates;
	
	@JsonBackReference(value = "jbCompensations")
	@OneToMany(cascade = CascadeType.ALL)
	private List<Compensation> compensations;

	public User(String firstName, String lastName, String username, String email, String phoneNumber, String password,
			Role role, Department department, boolean activeAcount, float solde) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.username = username;
		this.email = email;
		this.phoneNumber = phoneNumber;
		this.password = password;
		this.avatar = "default.png";
		this.role = role;
		this.department = department;
		this.blockedAccount = false;
		this.activeAcount = activeAcount;
		this.solde = solde;
	}

	@PrePersist
	protected void onCreate() {
		this.createdAt = new Date();
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = new Date();
	}

	public void setUpdatedFields(Profile user) {
		this.firstName = user.getFirstName();
		this.lastName = user.getLastName();
		this.phoneNumber = user.getPhoneNumber();
	}

	public void addLeave(Leave leave) {
		this.leaves = new ArrayList<>();
		if (leave != null)
			this.leaves.add(leave);
	}

	public void addHistory(SoldeHistory soldeHistory) {
		this.updates = new ArrayList<>();
		if (soldeHistory != null) {
			this.updates.add(soldeHistory);
		}
	}

	public void addCompensation(Compensation compensation) {
		this.compensations = new ArrayList<>();
		if (compensation != null) {
			this.compensations.add(compensation);
		}

	}

}
