package com.kineplan.notification.application;

public interface NotificationPort {
    void sendSms(String phone, String message);
    void sendEmail(String email, String subject, String message);
}