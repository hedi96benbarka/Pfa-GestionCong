package tn.iit.dto;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tn.iit.entities.Leave.HalfDay;
import tn.iit.entities.LeaveState;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserLeavesDto {

	private long id;
	private Date leaveStartDate;
	private Date leaveEndDate;
	private HalfDay startHalfDay;
	private HalfDay endHalfDay;
	private String document;
	private String detail;
	private String rhNote;
	private String userNote;
	private LeaveState etat;
	private Date createdAt;
	private String duration;
	private UserInfo alternative;

	public void calculateDetails() {
		try {
			float diff = TimeUnit.DAYS.convert(Math.abs(leaveStartDate.getTime() - leaveEndDate.getTime()),
					TimeUnit.MILLISECONDS);
			if (diff == 0) {
				switch (this.startHalfDay) {
				case MORNING:
					this.duration = "1/2 Jour matin";
					break;
				case AFTERNOON:
					this.duration = "1/2 Jour matin";
					break;
				default:
					this.duration = "1 Jour";
				}

				return;
			}
			DecimalFormat format = new DecimalFormat("0.#");
			if (this.startHalfDay == HalfDay.FULLDAY && this.endHalfDay == HalfDay.FULLDAY) {
				this.duration = format.format(diff) + " Jours";
				return;
			}
			SimpleDateFormat forme = new SimpleDateFormat("dd-MM-yyyy");
			if (this.startHalfDay == HalfDay.AFTERNOON)
				diff = (float) (diff - 0.5);
			if (this.endHalfDay == HalfDay.MORNING)
				diff = (float) (diff - 0.5);
			this.duration = format.format(diff) + " Jours ( Du " + forme.format(this.leaveStartDate)
					+ (this.startHalfDay == HalfDay.AFTERNOON ? " après midi " : "") + " au "
					+ forme.format(this.leaveStartDate) + (this.endHalfDay == HalfDay.MORNING ? " après midi " : "")
					+ " )";

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
