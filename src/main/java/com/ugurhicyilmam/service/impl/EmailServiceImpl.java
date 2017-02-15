package com.ugurhicyilmam.service.impl;

import com.ugurhicyilmam.model.User;
import com.ugurhicyilmam.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Autowired
    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendActivationEmail(User user) {
        String recipient = user.getEmail();

        sendMail(createSimpleMailMessage(recipient, "Hello", "World"));
    }

    private boolean sendMail(SimpleMailMessage mailMessage) {
        try {
            mailSender.send(mailMessage);
        } catch (MailSendException ex) {
            throw new RuntimeException(ex);
        }
        return true;
    }

    private SimpleMailMessage createSimpleMailMessage(String to, String subject, String message) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(to);
        mailMessage.setSubject(subject);
        mailMessage.setText(message);
        return mailMessage;
    }
}
