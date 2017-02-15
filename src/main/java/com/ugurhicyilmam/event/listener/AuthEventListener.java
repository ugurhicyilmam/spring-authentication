package com.ugurhicyilmam.event.listener;

import com.ugurhicyilmam.event.OnAccountCreation;
import com.ugurhicyilmam.model.User;
import com.ugurhicyilmam.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class AuthEventListener {

    private final EmailService emailService;

    @Autowired
    public AuthEventListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @Async
    @EventListener
    public void handleOnAccountCreation(OnAccountCreation event) {
        User user = event.getUser();

        emailService.sendActivationEmail(user);
    }
}
