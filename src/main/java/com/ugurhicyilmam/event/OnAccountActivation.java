package com.ugurhicyilmam.event;

import com.ugurhicyilmam.model.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

public class OnAccountActivation extends ApplicationEvent {
    @Getter
    private User user;

    public OnAccountActivation(User user) {
        super(user);
        this.user = user;
    }
}
