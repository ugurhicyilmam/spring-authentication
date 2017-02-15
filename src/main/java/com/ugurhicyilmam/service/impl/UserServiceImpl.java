package com.ugurhicyilmam.service.impl;

import com.ugurhicyilmam.model.User;
import com.ugurhicyilmam.repository.UserRepository;
import com.ugurhicyilmam.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @Override
    public User findByEmail(String email) {
        return null;
    }

    @Override
    public void create(User user) {
        user.setRegisteredAt(System.currentTimeMillis());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(false);
        user.setAccountNonExpired(true);
        user.setCredentialsNonExpired(true);
        user.setAccountNonLocked(true);
        userRepository.save(user);
    }
}
