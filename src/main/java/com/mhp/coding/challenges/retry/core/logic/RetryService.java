package com.mhp.coding.challenges.retry.core.logic;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

import com.mhp.coding.challenges.retry.core.entities.EmailNotification;
import com.mhp.coding.challenges.retry.core.factories.RetryJobFactory;
import com.mhp.coding.challenges.retry.core.factories.RetryJobTriggerFactory;

import lombok.extern.slf4j.Slf4j;

/**
 * Service handling the mail delivery failure with the retry pattern.
 * 
 * @author Matteo Sassano
 */
@Slf4j
@Service
public class RetryService {
	
	/**
	 * Base of the exponential function to calculate the future job
	 * start times.
	 */
	public static final int EXPONENTIAL_BASE = 2;
	
	/**
	 * Time delay of the first retry attempt in milliseconds.
	 */
	public static final int INITIAL_DELAY = 5000;
	
	/**
	 * The maximum number of retry attempts to be performed.
	 */
	public static final int MAX_RETRY_ATTEMPTS = 5;
	
	@Autowired
	private SchedulerFactoryBean schedulerFactoryBean;
	
	@Autowired
	private RetryJobFactory retryJobFactory;
	
	@Autowired
	private RetryJobTriggerFactory retryJobTriggerFactory;

	/**
	 * Handles a mail sending failure at the first time.
	 * 
	 * @param emailNotification object holding the email contents to be sent.
	 */
	public void handleSendFailure(@Valid @NotNull EmailNotification emailNotification) {
		createRetryJob(emailNotification);
	}

	/**
	 * Handles a mail sending failure.
	 * 
	 * If the max retry attempts are exceeded the email notification object is persisted in the database.
	 * (Was not required, but maybe util in future in order to retry in a later point of time)
	 * 
	 * @param emailNotification object holding the email contents to be sent.
	 * @param retryAttempt actual retry attempt to send the mail.
	 */
	public void handleSendFailure(@Valid @NotNull EmailNotification emailNotification, int retryAttempt) {
		if (retryAttempt <= RetryService.MAX_RETRY_ATTEMPTS) {
			createRetryJob(emailNotification, retryAttempt);
		} else {
			// Retry attempts are exceeded here. Further handling comes here.
		}
	}
	
	/**
	 * Creates the initial retry job in the case a mail couldn't be delivered
	 * at the first time.
	 * 
	 * @param emailNotification object holding the email contents to be sent.
	 */
	public void createRetryJob(EmailNotification emailNotification) {
		createRetryJob(emailNotification, 1);
	}
	
	/**
	 * Creates the initial retry job in the case a mail couldn't be delivered
	 * within a previously created retry job.
	 * 
	 * @param emailNotification emailNotification object holding the email contents to be sent
	 * @param retryAttempt current retry attempt of the email delivery
	 */
	public void createRetryJob(EmailNotification emailNotification, int retryAttempt) {
		JobDetail jobDetail = retryJobFactory.buildJobDetail(emailNotification, retryAttempt);
		Trigger trigger = retryJobTriggerFactory.buildJobTrigger(jobDetail);
		try {
			schedulerFactoryBean.getScheduler().scheduleJob(jobDetail, trigger);
			log.info("Scheduled new job. Retry attempt #" + retryAttempt);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

}
