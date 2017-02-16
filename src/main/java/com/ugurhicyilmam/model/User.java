package com.ugurhicyilmam.model;

import com.ugurhicyilmam.util.enums.Language;
import lombok.Data;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.Collection;

@Data
@ToString(exclude = {"password"})
@Entity
public class User implements UserDetails {
    @Id
    @GeneratedValue
    private long id;

    private String firstName;

    private String lastName;

    private String password;

    private String email;

    @Enumerated(EnumType.STRING)
    private Language language;

    @OneToOne(mappedBy = "user")
    private ActivationToken activationToken;

    // ### UserDetails ###
    private long registeredAt;

    private boolean accountNonExpired;

    private boolean accountNonLocked;

    private boolean credentialsNonExpired;

    private boolean enabled;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getUsername() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
