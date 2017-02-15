package com.ugurhicyilmam.repository;

import com.ugurhicyilmam.model.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
    User findByEmail(String email);
}
