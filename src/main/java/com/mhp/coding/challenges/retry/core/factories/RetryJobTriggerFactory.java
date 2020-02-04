package com.mhp.coding.challenges.retry.core.factories;

import java.time.Instant;
import java.util.Date;

import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.stereotype.Component;

import com.mhp.coding.challenges.retry.core.jobs.RetryJob;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RetryJobTriggerFactory {
	
	public static final int MULTIPLIER = 2;
	public static final int INITIAL_DELAY = 5000;
	public static final int MAX_RETRY_AMOUNT = 5;
	
	public Trigger buildJobTrigger(JobDetail jobDetail) {
		int retriesAmount = jobDetail.getJobDataMap().getIntValue(RetryJob.AMOUNT_RETRIES_KEY);
		int delay = INITIAL_DELAY * (int) Math.pow(MULTIPLIER, retriesAmount);
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
