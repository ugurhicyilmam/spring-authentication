package com.ugurhicyilmam.service;

import com.ugurhicyilmam.model.ActivationToken;
import com.ugurhicyilmam.model.User;

public interface ActivationTokenService {
    void issueNewToken(User user);

    ActivationToken findByUser(User user);

    void removeIfExistsForUser(User user);

    ActivationToken findByToken(String token);

    boolean isValid(ActivationToken activationToken);
}
