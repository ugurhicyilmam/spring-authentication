package com.ugurhicyilmam.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Data
@Entity
public class RecoveryToken {
    @Id
    @GeneratedValue
    private long id;

    private String token;

    private long validUntilInEpoch;

    @OneToOne
    private User user;
}
