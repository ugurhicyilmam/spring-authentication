package com.ugurhicyilmam.repository;

import com.ugurhicyilmam.model.ActivationToken;
import com.ugurhicyilmam.model.User;
import org.springframework.data.repository.CrudRepository;

public interface ActivationTokenRepository extends CrudRepository<ActivationToken, Long> {
    ActivationToken findByUser(User user);

    ActivationToken findByToken(String token);
}
