package com.mhp.coding.challenges.retry.core.factories;

import java.util.UUID;

import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;
import org.springframework.stereotype.Component;

import com.mhp.coding.challenges.retry.core.entities.EmailNotification;
import com.mhp.coding.challenges.retry.core.jobs.RetryJob;

/**
 * Factory which creates new Jobs for retrying to send an email.
 * 
 * @author Matteo Sassano
 *
 */
@Component
public class RetryJobFactory extends SpringBeanJobFactory implements ApplicationContextAware {
	
	private transient AutowireCapableBeanFactory beanFactory;
	
	/**
     * Initializes bean factory to create Job instances from persisted ones at runtime.
     */
    @Override
    public void setApplicationContext(final ApplicationContext context) {
        beanFactory = context.getAutowireCapableBeanFactory();
    }
    
    /**
     * Auto-wires stored Jobs at the restart of the application.
     */
    @Override
    protected Object createJobInstance(final TriggerFiredBundle bundle) throws Exception {
        final Object job = super.createJobInstance(bundle);
        beanFactory.autowireBean(job);
        return job;
    }
	
    /**
     * Returns a new Job for retrying to send an email.
     * The emailNotification object is passed through the JobDataMap into the Job.
     *  
     * @param emailNotification object holding the email contents to be sent.
     * @param retryAttempt number of the current retry attempt.
     * @return new Job which retries to send an email.
     */
	public JobDetail buildJobDetail(EmailNotification emailNotification, int retryAttempt) {
        JobDataMap jobDataMap = new JobDataMap();

        jobDataMap.put(RetryJob.RECIPIENT_KEY, emailNotification.getRecipient());
        jobDataMap.put(RetryJob.SUBJECT_KEY, emailNotification.getSubject());
        jobDataMap.put(RetryJob.TEXT_KEY, emailNotification.getText());
        jobDataMap.put(RetryJob.AMOUNT_RETRIES_KEY, retryAttempt);

        return JobBuilder.newJob(RetryJob.class)
                .withIdentity(UUID.randomUUID().toString(), "retry-email-jobs")
                .withDescription("Retry Send Email Job")
                .usingJobData(jobDataMap)
                .storeDurably()
                .build();
    }

}
