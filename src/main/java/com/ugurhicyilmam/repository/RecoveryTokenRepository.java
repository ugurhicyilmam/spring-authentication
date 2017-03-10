package com.ugurhicyilmam.repository;

import com.ugurhicyilmam.model.RecoveryToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecoveryTokenRepository extends CrudRepository<RecoveryToken, Long> {
}
