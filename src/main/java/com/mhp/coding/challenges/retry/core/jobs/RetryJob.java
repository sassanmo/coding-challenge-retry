package com.mhp.coding.challenges.retry.core.jobs;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import com.mhp.coding.challenges.retry.core.entities.EmailNotification;
import com.mhp.coding.challenges.retry.core.outbound.NotificationSender;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RetryJob extends QuartzJobBean {
	
	public static final String RECIPIENT_KEY = "recipient";
	public static final String SUBJECT_KEY = "subject";
	public static final String TEXT_KEY = "text";
	public static final String AMOUNT_RETRIES_KEY = "retryAttempts";
	
	@Autowired
	private NotificationSender notificationSender;

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		log.info("Executing job ...");
		JobDataMap jobDataMap = context.getMergedJobDataMap();
        String recipient = jobDataMap.getString(RECIPIENT_KEY);
        String subject = jobDataMap.getString(SUBJECT_KEY);
        String text = jobDataMap.getString(TEXT_KEY);
        int retryAttempts = jobDataMap.getIntValue(AMOUNT_RETRIES_KEY);
        EmailNotification emailNotification = new EmailNotification();
        emailNotification.setRecipient(recipient);
        emailNotification.setSubject(subject);
        emailNotification.setText(text);
        notificationSender.retrySendEmail(emailNotification, retryAttempts);
	}

}
