package com.ugurhicyilmam.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Data
@Entity
public class RefreshToken {

    @Id
    @GeneratedValue
    private long id;

    private String token;

    private long createdAt;

    @ManyToOne
    private User user;
}
