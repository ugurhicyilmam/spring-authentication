package com.ugurhicyilmam.service.impl;

import com.ugurhicyilmam.controller.request.LoginRequest;
import com.ugurhicyilmam.controller.request.RegisterRequest;
import com.ugurhicyilmam.controller.request.ResetRequest;
import com.ugurhicyilmam.event.OnAccountActivation;
import com.ugurhicyilmam.event.OnAccountCreation;
import com.ugurhicyilmam.event.OnAccountRecover;
import com.ugurhicyilmam.model.ActivationToken;
import com.ugurhicyilmam.model.RecoveryToken;
import com.ugurhicyilmam.model.RefreshToken;
import com.ugurhicyilmam.model.User;
import com.ugurhicyilmam.repository.ActivationTokenRepository;
import com.ugurhicyilmam.repository.RecoveryTokenRepository;
import com.ugurhicyilmam.repository.RefreshTokenRepository;
import com.ugurhicyilmam.repository.UserRepository;
import com.ugurhicyilmam.service.AuthService;
import com.ugurhicyilmam.service.exceptions.InvalidActivationTokenException;
import com.ugurhicyilmam.service.exceptions.LoginFailedException;
import com.ugurhicyilmam.service.exceptions.RecoveryTokenInvalidException;
import com.ugurhicyilmam.service.exceptions.UserNotFoundException;
import com.ugurhicyilmam.service.transfer.LoginTransfer;
import com.ugurhicyilmam.service.transfer.TokenTransfer;
import com.ugurhicyilmam.service.transfer.UserTransfer;
import com.ugurhicyilmam.util.TokenUtils;
import com.ugurhicyilmam.util.enums.Language;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final ActivationTokenRepository activationTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RecoveryTokenRepository recoveryTokenRepository;
    private final ApplicationEventPublisher eventPublisher;
    private static final Map<User, Set<AccessToken>> accessTokens = new ConcurrentHashMap<>();
    private final long activationTokenLifetime;
    private final long accessTokenLifetime;
    private final long resetPasswordTokenLifetime;

    public AuthServiceImpl(PasswordEncoder passwordEncoder, UserRepository userRepository, ActivationTokenRepository activationTokenRepository, RefreshTokenRepository refreshTokenRepository, RecoveryTokenRepository recoveryTokenRepository, ApplicationEventPublisher eventPublisher, @Value("${application.activation-token-lifetime}") long activationTokenLifetime, @Value("${application.access-token-lifetime}") long accessTokenLifetime, @Value("${application.recover-token-lifetime}") long resetPasswordTokenLifetime) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.activationTokenRepository = activationTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.recoveryTokenRepository = recoveryTokenRepository;
        this.eventPublisher = eventPublisher;
        this.activationTokenLifetime = activationTokenLifetime;
        this.accessTokenLifetime = accessTokenLifetime;
        this.resetPasswordTokenLifetime = resetPasswordTokenLifetime;
    }

    @Override
    public User register(RegisterRequest request) {
        User user = createUserByRegistrationRequest(request);
        createActivationTokenForUser(user);
        eventPublisher.publishEvent(new OnAccountCreation(user));
        return user;
    }

    @Override
    public void activate(String token) {
        ActivationToken activationToken = validateAndFetchActivationToken(token);
        User user = activationToken.getUser();
        activateUser(user);
        activationTokenRepository.deleteByUser(user);
        eventPublisher.publishEvent(new OnAccountActivation(user));
    }

    @Override
    public void resendActivationToken(User user) {
        createActivationTokenForUser(user);
        // TODO: 10.03.2017 implement this
//        eventPublisher.publishEvent(new OnResendActivationToken(user));
    }

    @Override
    public LoginTransfer login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail());
        if (user == null || request.getPassword() == null || !passwordEncoder.matches(request.getPassword(), user.getPassword()))
            throw new LoginFailedException();
        return getLoginTransferForUser(user);
    }

    @Override
    public void recover(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null)
            throw new UserNotFoundException();
        createRecoveryTokenForUser(user);
        eventPublisher.publishEvent(new OnAccountRecover(user));
    }

    @Override
    public LoginTransfer reset(String token, String password) {
        RecoveryToken recoveryToken = recoveryTokenRepository.findByToken(token);
        if (recoveryToken == null || recoveryToken.getValidUntilInEpoch() < System.currentTimeMillis()) {
            throw new RecoveryTokenInvalidException();
        }
        User user = recoveryToken.getUser();
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        return login(new LoginRequest(user.getEmail(), password));
    }

    private String createRecoveryTokenForUser(User user) {
        String token = TokenUtils.generateToken();

        RecoveryToken recoveryToken = new RecoveryToken();
        recoveryToken.setToken(passwordEncoder.encode(token));
        recoveryToken.setUser(user);
        recoveryToken.setValidUntilInEpoch(System.currentTimeMillis() + resetPasswordTokenLifetime);
        user.setRecoveryToken(recoveryToken);
        recoveryTokenRepository.save(recoveryToken);
        return token;
    }

    private User createUserByRegistrationRequest(RegisterRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setLanguage(Language.valueOf(request.getLanguage()));
        user.setRegisteredAt(System.currentTimeMillis());
        user.setEnabled(false);
        user.setAccountNonExpired(true);
        user.setCredentialsNonExpired(true);
        user.setAccountNonLocked(true);
        userRepository.save(user);
        return user;
    }

    private void createActivationTokenForUser(User user) {
        activationTokenRepository.deleteByUser(user);
        ActivationToken activationToken = generateActivationTokenForUser(user);
        activationTokenRepository.save(activationToken);
    }

    private ActivationToken generateActivationTokenForUser(User user) {
        ActivationToken activationToken = new ActivationToken();
        activationToken.setToken(TokenUtils.generateToken());
        activationToken.setUser(user);
        activationToken.setValidUntilInEpoch(System.currentTimeMillis() + this.activationTokenLifetime);
        user.setActivationToken(activationToken);
        return activationToken;
    }

    private ActivationToken validateAndFetchActivationToken(String token) {
        ActivationToken activationToken = activationTokenRepository.findByToken(token);
        if (activationToken == null || activationToken.getValidUntilInEpoch() < System.currentTimeMillis())
            throw new InvalidActivationTokenException();
        return activationToken;
    }


    private void activateUser(User user) {
        user.setEnabled(true);
        userRepository.save(user);
    }

    private String createAccessTokenForUser(User user) {
        accessTokens.putIfAbsent(user, ConcurrentHashMap.newKeySet());
        AccessToken accessToken = new AccessToken(user);
        accessTokens.get(user).add(accessToken);
        return accessToken.getToken();
    }

    private LoginTransfer getLoginTransferForUser(User user) {
        LoginTransfer loginTransfer = new LoginTransfer();
        loginTransfer.setTokenTransfer(createAccessAndRefreshTokens(user));
        loginTransfer.setUserInformation(new UserTransfer(user));
        return loginTransfer;
    }

    private TokenTransfer createAccessAndRefreshTokens(User user) {
        TokenTransfer tokenTransfer = new TokenTransfer();
        tokenTransfer.setAccessToken(createAccessTokenForUser(user));
        tokenTransfer.setRefreshToken(createRefreshTokenForUser(user));
        return tokenTransfer;
    }

    private String createRefreshTokenForUser(User user) {
        String token = TokenUtils.generateToken();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(passwordEncoder.encode(token));
        refreshToken.setCreatedAt(System.currentTimeMillis());
        refreshToken.setUser(user);
        refreshTokenRepository.save(refreshToken);

        return token;
    }

    @Getter
    private class AccessToken {
        private String token;
        private long validUntilInEpoch;
        private User user;

        AccessToken(User user) {
            this.user = user;
            this.token = TokenUtils.generateToken();
            this.validUntilInEpoch = System.currentTimeMillis() + accessTokenLifetime;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            AccessToken that = (AccessToken) o;

            return token.equals(that.token);
        }

        @Override
        public int hashCode() {
            return token.hashCode();
        }
    }
}
