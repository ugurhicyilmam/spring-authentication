package com.ugurhicyilmam.repository;

import com.ugurhicyilmam.model.RefreshToken;
import com.ugurhicyilmam.model.User;
import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {
    RefreshToken findByUser(User user);

    RefreshToken findByToken(String token);

    void deleteByToken(String token);

    void deleteByUser(User user);
}
