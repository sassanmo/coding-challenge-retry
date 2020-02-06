package com.mhp.coding.challenges.retry.core.jobs;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import com.mhp.coding.challenges.retry.core.entities.EmailNotification;
import com.mhp.coding.challenges.retry.core.outbound.NotificationSender;

/**
 * QuartzJobBean implementing the execution of a retry job.
 * 
 * @author Matteo Sassano
 *
 */
@Component
public class RetryJob extends QuartzJobBean {
	
	/**
	 * Key for getting the recipient value from the jobDataMap.
	 */
	public static final String RECIPIENT_KEY = "recipient";
	
	/**
	 * Key for getting the subject value from the jobDataMap.
	 */
	public static final String SUBJECT_KEY = "subject";
	
	/**
	 * Key for getting the email text value from the jobDataMap.
	 */
	public static final String TEXT_KEY = "text";
	
	/**
	 * Key for getting the amount of retry attempts  value from the jobDataMap.
	 */
	public static final String AMOUNT_RETRIES_KEY = "retryAttempts";
	
	@Autowired
	private NotificationSender notificationSender;

	/**
	 * Execution procedure of the retry job.
	 * Collects data from the jobDataMap in order to recreate the email Notification
	 * to send.
	 * Calls the retrySendEmail method to send the email.
	 * 
	 * @param context holding information about the firing job
	 * @throws JobExecutionException
	 */
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
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
