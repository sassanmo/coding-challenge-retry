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

@Component
public class RetryJobFactory extends SpringBeanJobFactory implements ApplicationContextAware {
	
	private transient AutowireCapableBeanFactory beanFactory;
	
    @Override
    public void setApplicationContext(final ApplicationContext context) {
        beanFactory = context.getAutowireCapableBeanFactory();
    }
    
    @Override
    protected Object createJobInstance(final TriggerFiredBundle bundle) throws Exception {
        final Object job = super.createJobInstance(bundle);
        beanFactory.autowireBean(job);
        return job;
    }
    
	public JobDetail buildJobDetail(EmailNotification emailNotification) {
        return buildJobDetail(emailNotification, 0);
    }
	
	public JobDetail buildJobDetail(EmailNotification emailNotification, int retryAttempts) {
        JobDataMap jobDataMap = new JobDataMap();

        jobDataMap.put(RetryJob.RECIPIENT_KEY, emailNotification.getRecipient());
        jobDataMap.put(RetryJob.SUBJECT_KEY, emailNotification.getSubject());
        jobDataMap.put(RetryJob.TEXT_KEY, emailNotification.getText());
        jobDataMap.put(RetryJob.AMOUNT_RETRIES_KEY, retryAttempts);

        return JobBuilder.newJob(RetryJob.class)
                .withIdentity(UUID.randomUUID().toString(), "retry-email-jobs")
                .withDescription("Retry Send Email Job")
                .usingJobData(jobDataMap)
                .storeDurably()
                .build();
    }

}
