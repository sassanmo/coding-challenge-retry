package com.mhp.coding.challenges.retry.core.factories;

import java.util.Date;

import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.stereotype.Component;

import com.mhp.coding.challenges.retry.core.jobs.RetryJob;
import com.mhp.coding.challenges.retry.core.logic.RetryService;

import lombok.extern.slf4j.Slf4j;

/**
 * Factory class to create quartz job triggers.
 * 
 * @author Matteo Sassano
 *
 */
@Slf4j
@Component
public class RetryJobTriggerFactory {
	
	/**
	 * Creates job trigger for a given job.
	 * Reads the number of the performed retry attempts and sets the start time of the job in an
	 * exponential time-displacement.
	 * 
	 * E.g. 1st retry -> 5s; 2nd retry -> 10s; 3rd retry -> 20s;...
	 * 
	 * @param jobDetail detail instance of the Job to be executed in future.
	 * @return trigger for the job.
	 */
	public Trigger buildJobTrigger(JobDetail jobDetail) {
		int retriesAmount = jobDetail.getJobDataMap().getIntValue(RetryJob.AMOUNT_RETRIES_KEY);
		int delay = RetryService.INITIAL_DELAY * (int) Math.pow(RetryService.EXPONENTIAL_BASE, (retriesAmount - 1));
		Date fireDate = new Date(System.currentTimeMillis() + delay);
		log.info("Building job with delay " + delay + " seconds " + fireDate);
		return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobDetail.getKey().getName(), "retry-email-triggers")
                .withDescription("Retry Send Email Trigger")
                .startAt(fireDate)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                .build();
    }

}
