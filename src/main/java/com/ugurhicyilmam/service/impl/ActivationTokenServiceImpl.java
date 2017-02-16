package com.ugurhicyilmam.service.impl;

import com.ugurhicyilmam.model.ActivationToken;
import com.ugurhicyilmam.model.User;
import com.ugurhicyilmam.repository.ActivationTokenRepository;
import com.ugurhicyilmam.service.ActivationTokenService;
import com.ugurhicyilmam.util.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class ActivationTokenServiceImpl implements ActivationTokenService {

    private final ActivationTokenRepository activationTokenRepository;
    private final long tokenLifeLength;

    @Autowired
    public ActivationTokenServiceImpl(ActivationTokenRepository activationTokenRepository, @Value("${application.activation-token-life-length}") long tokenLifeLength) {
        this.activationTokenRepository = activationTokenRepository;
        this.tokenLifeLength = tokenLifeLength;
    }

    @Override
    public void issueNewToken(User user) {
        removeIfExistsForUser(user);
        ActivationToken activationToken = generateActivationTokenForUser(user);
        activationTokenRepository.save(activationToken);
    }

    private ActivationToken generateActivationTokenForUser(User user) {
        ActivationToken activationToken = new ActivationToken();
        activationToken.setToken(TokenUtils.generateToken());
        activationToken.setUser(user);
        activationToken.setValidUntil(getValidUntil());
        return activationToken;
    }

    @Override
    public ActivationToken findByUser(User user) {
        return activationTokenRepository.findByUser(user);
    }

    @Override
    public void removeIfExistsForUser(User user) {
        ActivationToken activationToken = findByUser(user);
        if (activationToken != null)
            activationTokenRepository.delete(activationToken);
    }


    private long getValidUntil() {
        long currentTime = System.currentTimeMillis();
        return currentTime + hoursToMillis(this.tokenLifeLength);
    }

    private long hoursToMillis(long hours) {
        return TimeUnit.HOURS.toMillis(hours);
    }

}
