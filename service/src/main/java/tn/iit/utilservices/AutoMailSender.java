package tn.iit.utilservices;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import tn.iit.dto.Mail;
import tn.iit.entities.Leave;
import tn.iit.entities.LeaveState;
import tn.iit.entities.User;
import tn.iit.repositories.LeaveRepo;
import tn.iit.repositories.UserRepo;

@Service
public class AutoMailSender {
	@Autowired
	private LeaveRepo leaveRepo;
	@Autowired
	private UserRepo userRepo;
	@Autowired
	private MailService mailService;

	private static final Logger log = LoggerFactory.getLogger(AutoMailSender.class);
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

	@Scheduled(fixedRate = 1000000)
	public void reportCurrentTime() {
		log.info("Checking...");
		final String pattern = "dd-MM-yyyy";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		List<Leave> leavesList = leaveRepo.findAllByNotifyAndEtat(false, LeaveState.PENDING);
		if (leavesList.size() > 0) {
			List<User> listRh = userRepo.findAllByRoleName("ROLE_RH");
			for (Leave leave : leavesList) {
				long diff = TimeUnit.HOURS.convert(Math.abs(new Date().getTime() - leave.getCreatedAt().getTime()),
						TimeUnit.MILLISECONDS);
				if (diff >= 48) {
					listRh.forEach(rh -> {
						try {
							Leave l = leave;
							l.setNotify(true);
							if (rh.getId() != leave.getUser().getId()) {
								Mail mail = new Mail("IITNotificationSystem", rh.getEmail(),
										"Notification des congés pas encore confirmé");
								mailService.sendTextMail(mail,
										"Bonjour "+ rh.getFirstName() +" "+rh.getLastName() +"\nLa demande de Mr " + leave.getUser().getFirstName() + " "
												+ leave.getUser().getLastName() + " créé le "
												+ simpleDateFormat.format(leave.getCreatedAt())
												+ " est en attente de votre confirmation\nCordialement\nIIT Notification System");
								leaveRepo.save(l);
								log.info("Email send to {} at {}", rh.getFirstName(), dateFormat.format(new Date()));
							}
						} catch (MessagingException e) {
							e.printStackTrace();
						}
					});
				} else {
					log.info("0 Email Sent");
				}

			}
		} else {
			log.info("0 Email Sent");

		}

	}

}
