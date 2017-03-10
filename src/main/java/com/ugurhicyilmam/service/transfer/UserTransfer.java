package com.ugurhicyilmam.service.transfer;

import com.ugurhicyilmam.model.User;
import lombok.Data;

@Data
public class UserTransfer {

    private String firstName;

    private String lastName;

    private String email;

    private long registeredAt;

    private boolean accountNonExpired;

    private boolean accountNonLocked;

    private boolean credentialsNonExpired;

    private boolean enabled;

    private String language;

    public UserTransfer(User user) {
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.registeredAt = user.getRegisteredAt();
        this.accountNonExpired = user.isAccountNonExpired();
        this.accountNonLocked = user.isAccountNonLocked();
        this.credentialsNonExpired = user.isCredentialsNonExpired();
        this.enabled = user.isEnabled();
        this.language = user.getLanguage().toString();
    }
}
