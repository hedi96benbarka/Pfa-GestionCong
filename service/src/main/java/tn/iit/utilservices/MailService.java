package tn.iit.utilservices;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import tn.iit.dto.Mail;

@Service
public class MailService {

	@Autowired
	private JavaMailSender emailSender;
	@Autowired
	private SpringTemplateEngine templateEngine;

	private static int noOfQuickServiceThreads = 20;
	private ScheduledExecutorService quickService = Executors.newScheduledThreadPool(noOfQuickServiceThreads);

	public void sendValidationMail(final Mail mail) throws MailException, RuntimeException, MessagingException {

		MimeMessage message = emailSender.createMimeMessage();

		MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
				StandardCharsets.UTF_8.name());

		Context context = new Context();
		context.setVariables(mail.getProps());

		String html = templateEngine.process("validation-template", context);

		helper.setTo(mail.getMailTo());
		helper.setText(html, true);
		helper.setSubject(mail.getSubject());
		helper.setFrom(mail.getFrom());

		quickService.submit(new Runnable() {
			@Override
			public void run() {
				try {
					emailSender.send(message);
				} catch (Exception e) {
				}
			}
		});
	}

	public void sendTextMail(final Mail mail, String mailText) throws MessagingException {
		MimeMessage message = emailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		Context context = new Context();
		context.setVariables(mail.getProps());
		helper.setTo(mail.getMailTo());
		helper.setText(mailText);
		helper.setSubject(mail.getSubject());
		helper.setFrom(mail.getFrom());
		quickService.submit(new Runnable() {
			@Override
			public void run() {
				try {
					emailSender.send(message);
				} catch (Exception e) {
				}
			}
		});
	}

}
