package com.mhp.coding.challenges.retry.core.outbound;

import com.mhp.coding.challenges.retry.core.entities.EmailNotification;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public interface NotificationSender {

    void sendEmail(@Valid @NotNull EmailNotification emailNotification);
    
    /**
     * Method called by a retry job in order to retry sending an email.
     * 
     * (Was added in order to not change the EmailNotification model -> separation of concerns)
     * 
     * @param emailNotification object holding the email contents to be sent 
     * @param retryAttempt number of the current retry attempt.
     */
    void retrySendEmail(@Valid @NotNull EmailNotification emailNotification, int retryAttempt);
}
