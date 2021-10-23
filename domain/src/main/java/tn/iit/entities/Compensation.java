package tn.iit.entities;

import java.util.Date;
import java.util.concurrent.TimeUnit;

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

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import tn.iit.entities.Leave.HalfDay;

@Data
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Compensations")
public class Compensation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	private String document;
	private Date compStartDate;
	private Date compEndDate;
	private HalfDay startHalfDay;
	private HalfDay endHalfDay;

	@Type(type = "text")
	private String detail;

	@Type(type = "text")
	private String rhNote;

	private CompensationState etat = CompensationState.PENDING;

	@JsonFormat(pattern = "yyyy-MM-dd")
	@Column(updatable = false)
	private Date createdAt;

	@JsonFormat(pattern = "yyyy-MM-dd")
	private Date updatedAt;

	@ManyToOne
	private User user;

	public enum CompensationState {
		PENDING, APPROVED, REFUSED
	}

	@PrePersist
	protected void onCreate() {
		this.createdAt = new Date();
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = new Date();
	}

	public float calculateCompensation() {
		try {
			float diff = TimeUnit.DAYS.convert(
					Math.abs(this.getCompEndDate().getTime() - this.getCompStartDate().getTime()),
					TimeUnit.MILLISECONDS);
			if (this.getStartHalfDay() == null && this.getEndHalfDay() == null) {
				if (diff == 0)
					return ++diff;
				return diff;
			} else {
				if (diff == 0)
					return diff++;
				if (diff <= 1) {
					switch (this.getStartHalfDay()) {
					case MORNING:
						diff -= 0.5;
						break;
					case AFTERNOON:
						diff -= 0.5;
						break;
					default:
						break;
					}
					return diff;
				}
				if (this.getStartHalfDay() != HalfDay.FULLDAY && this.getEndHalfDay() != HalfDay.FULLDAY
						&& this.getEndHalfDay() != HalfDay.MORNING)
					diff -= 0.5;
				return diff;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
}
