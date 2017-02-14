package com.ugurhicyilmam.event;

import com.ugurhicyilmam.model.User;
import org.springframework.context.ApplicationEvent;

public class OnAccountCreation extends ApplicationEvent {

    private User user;

    public OnAccountCreation(User user) {
        super(user);
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
