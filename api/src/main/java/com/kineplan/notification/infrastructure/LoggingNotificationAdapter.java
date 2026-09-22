package com.kineplan.notification.infrastructure;

import com.kineplan.notification.application.NotificationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingNotificationAdapter implements NotificationPort {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingNotificationAdapter.class);

    @Override
    public void sendSms(String phone, String message) {
        LOGGER.info("notification channel=SMS status=SIMULATED");
    }

    @Override
    public void sendEmail(String email, String subject, String message) {
        LOGGER.info("notification channel=EMAIL status=SIMULATED");
    }
}