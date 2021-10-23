package tn.iit.dto;

import java.util.Date;

import lombok.Data;
import tn.iit.entities.Compensation.CompensationState;
import tn.iit.entities.Leave.HalfDay;

@Data
public class CompensationDto {
	private long id;
	private String document;
	private Date compStartDate;
	private Date compEndDate;
	private HalfDay startHalfDay;
	private HalfDay endHalfDay;
	private float compensationAmount;
	private String detail;
	private String rhNote;
	private CompensationState etat;
	private Date createdAt;
	private Date updatedAt;
	private SimpleUserDto user;

}
