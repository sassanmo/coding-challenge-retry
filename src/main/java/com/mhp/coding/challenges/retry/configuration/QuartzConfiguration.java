package com.mhp.coding.challenges.retry.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import com.mhp.coding.challenges.retry.core.factories.RetryJobFactory;

@Configuration
public class QuartzConfiguration {
	
	@Autowired
	private ApplicationContext applicationContext;
	
	@Autowired
	private RetryJobFactory retryJobFactory;
	
	/**
	 * Creates a Job factory with the given application context in order
	 * to be able to inject dependencies in serialized Jobs
	 * @return
	 */
	@Bean
    public SpringBeanJobFactory springBeanJobFactory() {
		retryJobFactory.setApplicationContext(applicationContext);
        return retryJobFactory;
    }
	
	/**
	 * Creates a factory bean which configures a quartz scheduler.
	 * The configurations of the scheduler are loaded form the quartz.properties file.
	 * @return factory bean with a configured scheduler.
	 */
	@Bean
	 public SchedulerFactoryBean schedulerFactoryBean() {
	  SchedulerFactoryBean scheduler = new SchedulerFactoryBean();
	  scheduler.setAutoStartup(true);
	  scheduler.setJobFactory(springBeanJobFactory());
	  scheduler.setApplicationContextSchedulerContextKey("applicationContext");
	  scheduler.setConfigLocation(new ClassPathResource("quartz.properties"));
	  scheduler.setWaitForJobsToCompleteOnShutdown(false);
	  return scheduler;
	 }

}
