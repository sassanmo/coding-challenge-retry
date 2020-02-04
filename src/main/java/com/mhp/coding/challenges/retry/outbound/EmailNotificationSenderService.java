package com.mhp.coding.challenges.retry.outbound;

import com.mhp.coding.challenges.retry.core.entities.EmailNotification;
import com.mhp.coding.challenges.retry.core.factories.RetryJobTriggerFactory;
import com.mhp.coding.challenges.retry.core.logic.RetryService;
import com.mhp.coding.challenges.retry.core.outbound.NotificationSender;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Service
@Validated
public class EmailNotificationSenderService implements NotificationSender {

    private static final String SENDER_ADDRESS = "info@mhp.com";

    private JavaMailSender mailSender;
    
    @Autowired
    private RetryService retryService;

    public EmailNotificationSenderService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    @Override
    public void sendEmail(@Valid @NotNull EmailNotification emailNotification) {
        try {
        	SimpleMailMessage mailMessage = createSimpleMailMessage(emailNotification);

            mailSender.send(mailMessage);
        } catch (Exception e) {
        	retryService.createRetryJob(emailNotification);
        }
    }
    
    @Override
    public void retrySendEmail(@Valid @NotNull EmailNotification emailNotification, int retryAttempts) {
    	try {
            SimpleMailMessage mailMessage = createSimpleMailMessage(emailNotification);

            mailSender.send(mailMessage);
        } catch (Exception e) {
        	if (retryAttempts < RetryJobTriggerFactory.MAX_RETRY_AMOUNT) {
        		retryService.createRetryJob(emailNotification, retryAttempts + 1);
        	}
        }
    }
    
    public SimpleMailMessage createSimpleMailMessage(EmailNotification emailNotification) {
    	SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(SENDER_ADDRESS);
        mailMessage.setTo(emailNotification.getRecipient());
        mailMessage.setSubject(emailNotification.getSubject());
        mailMessage.setText(emailNotification.getText());
        return mailMessage;
    }
    
}
