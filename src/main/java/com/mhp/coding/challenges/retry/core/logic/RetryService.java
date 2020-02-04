package com.mhp.coding.challenges.retry.core.logic;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

import com.mhp.coding.challenges.retry.core.entities.EmailNotification;
import com.mhp.coding.challenges.retry.core.factories.RetryJobFactory;
import com.mhp.coding.challenges.retry.core.factories.RetryJobTriggerFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RetryService {
	
	
	@Autowired
	private SchedulerFactoryBean schedulerFactoryBean;
	
	@Autowired
	private RetryJobFactory retryJobFactory;
	
	@Autowired
	private RetryJobTriggerFactory retryJobTriggerFactory;
	
	public void createRetryJob(EmailNotification emailNotification) {
		createRetryJob(emailNotification, 0);
	}
	
	public void createRetryJob(EmailNotification emailNotification, int retryAttempts) {
		log.info("createRetryJob");
		JobDetail jobDetail = retryJobFactory.buildJobDetail(emailNotification, retryAttempts);
		Trigger trigger = retryJobTriggerFactory.buildJobTrigger(jobDetail);
		try {
			schedulerFactoryBean.getScheduler().scheduleJob(jobDetail, trigger);
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
