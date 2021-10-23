package tn.iit.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Mail {

	private String from;
	private String mailTo;
	private String subject;
	private List<Object> attachments;
	private Map<String, Object> props;
	
	public Mail(String from, String mailTo, String subject) {
		super();
		this.from = from;
		this.mailTo = mailTo;
		this.subject = subject;
	}

}
