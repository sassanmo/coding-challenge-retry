package com.mhp.coding.challenges.retry.outbound;

import com.mhp.coding.challenges.retry.core.entities.EmailNotification;
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
    
    /**
     * Service handling the retry attempts.
     */
    @Autowired
    private RetryService retryService;

    public EmailNotificationSenderService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Asynchronous method which tries to send an email.
     * In cases of emerging problems (raised MailExeption,...) a retry mechanism of sending the mail
     * is started.
     */
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
    
    /**
     * Method called by a retry job in order to retry sending an email.
     * 
     * (Was added in order to not change the EmailNotification model -> separation of concerns)
     * 
     * @param emailNotification object holding the email contents to be sent 
     * @param retryAttempt number of the current retry attempt.
     */
    @Override
    public void retrySendEmail(@Valid @NotNull EmailNotification emailNotification, int retryAttempt) {
    	try {
            SimpleMailMessage mailMessage = createSimpleMailMessage(emailNotification);
            mailSender.send(mailMessage);
        } catch (Exception e) {
        	retryService.handleSendFailure(emailNotification, retryAttempt + 1);
        }
    }
    
    /**
     * Creates an instance of SimpleMailMessage.
     * @param emailNotification object holding the email contents to be sent.
     * @return SimpleMailMessage instance.
     */
    public SimpleMailMessage createSimpleMailMessage(EmailNotification emailNotification) {
    	SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(SENDER_ADDRESS);
        mailMessage.setTo(emailNotification.getRecipient());
        mailMessage.setSubject(emailNotification.getSubject());
        mailMessage.setText(emailNotification.getText());
        return mailMessage;
    }
    
}
