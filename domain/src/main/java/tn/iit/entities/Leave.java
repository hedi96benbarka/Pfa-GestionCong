package tn.iit.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Data
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "leaves")
public class Leave {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	private Date leaveStartDate;

	private Date leaveEndDate;

	private HalfDay startHalfDay;

	private HalfDay endHalfDay;

	private boolean notify = false;

	@Type(type = "text")
	private String detail;

	@Type(type = "text")
	private String rhNote;

	@Type(type = "text")
	private String userNote;

	private String document;

	private LeaveState etat = LeaveState.PENDING;

	@JsonFormat(pattern = "yyyy-MM-dd")
	@Column(updatable = false)
	private Date createdAt;

	@JsonFormat(pattern = "yyyy-MM-dd")
	private Date updatedAt;

	@JsonBackReference(value = "refAlternative")
	@ManyToOne
	private User alternative;

	@JsonBackReference(value = "refUser")
	@ManyToOne
	private User user;

	public enum HalfDay {
		FULLDAY, MORNING, AFTERNOON
	}

	@PrePersist
	protected void onCreate() {
		this.createdAt = new Date();
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = new Date();
	}

}
