package com.ugurhicyilmam.service.impl;

import com.ugurhicyilmam.controller.request.LoginRequest;
import com.ugurhicyilmam.controller.request.RegisterRequest;
import com.ugurhicyilmam.event.OnAccountActivation;
import com.ugurhicyilmam.event.OnAccountCreation;
import com.ugurhicyilmam.event.OnResendActivationToken;
import com.ugurhicyilmam.model.ActivationToken;
import com.ugurhicyilmam.model.RefreshToken;
import com.ugurhicyilmam.model.User;
import com.ugurhicyilmam.repository.ActivationTokenRepository;
import com.ugurhicyilmam.repository.RecoveryTokenRepository;
import com.ugurhicyilmam.repository.RefreshTokenRepository;
import com.ugurhicyilmam.repository.UserRepository;
import com.ugurhicyilmam.service.AuthService;
import com.ugurhicyilmam.service.exceptions.InvalidActivationTokenException;
import com.ugurhicyilmam.service.exceptions.LoginFailedException;
import com.ugurhicyilmam.service.transfer.LoginTransfer;
import com.ugurhicyilmam.util.TokenUtils;
import com.ugurhicyilmam.util.enums.Language;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TokenUtils.class)
@PowerMockRunnerDelegate(SpringRunner.class)
public class AuthServiceImplTest {

    @MockBean
    private PasswordEncoder passwordEncoder;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private ActivationTokenRepository activationTokenRepository;
    @MockBean
    private RefreshTokenRepository refreshTokenRepository;
    @MockBean
    private RecoveryTokenRepository recoveryTokenRepository;
    @MockBean
    private ApplicationEventPublisher eventPublisher;

    private long activationTokenLifetime;
    private long accessTokenLifetime;
    private long resetPasswordTokenLifetime;

    private String generatedToken = "TOKEN_UTILS_GENERATED_TOKEN";

    private AuthService authService;

    @Before
    public void setUp() throws Exception {

        activationTokenLifetime = 100;
        accessTokenLifetime = 100;
        resetPasswordTokenLifetime = 100;

        authService = new AuthServiceImpl(
                passwordEncoder,
                userRepository,
                activationTokenRepository,
                refreshTokenRepository,
                recoveryTokenRepository,
                eventPublisher,
                activationTokenLifetime,
                accessTokenLifetime,
                resetPasswordTokenLifetime
        );

        PowerMockito.mockStatic(TokenUtils.class);
        Mockito.when(TokenUtils.generateToken()).thenReturn(generatedToken);

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void register_shouldCorrectlyInitializeUserObject() throws Exception {
        RegisterRequest request = getRegisterRequest();

        String encodedPassword = TokenUtils.generateToken();

        when(passwordEncoder.encode(eq(request.getPassword()))).thenReturn(encodedPassword);

        authService.register(request);

        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userArgumentCaptor.capture());
        User registeredUser = userArgumentCaptor.getValue();

        assertEquals(request.getEmail(), registeredUser.getEmail());
        assertEquals(encodedPassword, registeredUser.getPassword());
        assertEquals(request.getFirstName(), registeredUser.getFirstName());
        assertEquals(request.getLastName(), registeredUser.getLastName());
        assertEquals(request.getLanguage(), registeredUser.getLanguage().toString());
        assertFalse(registeredUser.isEnabled());
        assertTrue(registeredUser.isAccountNonExpired());
        assertTrue(registeredUser.isAccountNonLocked());
        assertTrue(registeredUser.isCredentialsNonExpired());
        assertTrue(registeredUser.getRegisteredAt() < System.currentTimeMillis() + 50);
        assertTrue(registeredUser.getRegisteredAt() > System.currentTimeMillis() - 50);
    }

    @Test
    public void register_shouldCreateCorrectActivationTokenForUser() throws Exception {
        RegisterRequest request = getRegisterRequest();

        User registeredUser = authService.register(request);

        ArgumentCaptor<ActivationToken> activationTokenArgumentCaptor = ArgumentCaptor.forClass(ActivationToken.class);
        verify(activationTokenRepository, times(1)).save(activationTokenArgumentCaptor.capture());
        ActivationToken savedToken = activationTokenArgumentCaptor.getValue();

        assertEquals(generatedToken, savedToken.getToken());
        assertEquals(registeredUser, savedToken.getUser());
        assertEquals(registeredUser.getActivationToken(), savedToken);
        assertTrue(savedToken.getValidUntilInEpoch() > System.currentTimeMillis() + activationTokenLifetime - 50);
        assertTrue(savedToken.getValidUntilInEpoch() < System.currentTimeMillis() + activationTokenLifetime + 50);
    }

    @Test
    public void register_shouldInvokeDeleteByUserBeforeSaveOfActivationTokenRepository() throws Exception {
        authService.register(getRegisterRequest());

        InOrder inOrder = Mockito.inOrder(activationTokenRepository);

        inOrder.verify(activationTokenRepository, times(1)).deleteByUser(any(User.class));
        inOrder.verify(activationTokenRepository, times(1)).save(any(ActivationToken.class));
    }

    @Test
    public void register_shouldInvokeDeleteByUserOfActivationTokenRepositoryWithRegisteredUser() throws Exception {
        User registeredUser = authService.register(getRegisterRequest());
        verify(activationTokenRepository, times(1)).deleteByUser(eq(registeredUser));
    }

    @Test
    public void register_shouldPublishEventForCorrectUser() throws Exception {
        authService.register(getRegisterRequest());
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userArgumentCaptor.capture());
        User registeredUser = userArgumentCaptor.getValue();

        ArgumentCaptor<OnAccountCreation> onAccountCreationArgumentCaptor = ArgumentCaptor.forClass(OnAccountCreation.class);
        verify(eventPublisher, times(1)).publishEvent(onAccountCreationArgumentCaptor.capture());

        OnAccountCreation firedEvent = onAccountCreationArgumentCaptor.getValue();

        assertEquals(registeredUser, firedEvent.getUser());
    }

    @Test
    public void register_shouldFollowCorrectAlgorithm() throws Exception {
        User user = authService.register(getRegisterRequest());

        InOrder inOrder = Mockito.inOrder(userRepository, activationTokenRepository, eventPublisher);

        inOrder.verify(userRepository, times(1)).save(eq(user));
        inOrder.verify(activationTokenRepository, times(1)).deleteByUser(eq(user));
        inOrder.verify(activationTokenRepository, times(1)).save(any(ActivationToken.class));
        inOrder.verify(eventPublisher, times(1)).publishEvent(any(OnAccountCreation.class));
    }


    @Test
    public void activate_shouldActivateUserWhenValidActivationToken() throws Exception {
        String token = generatedToken;
        User user = new User();
        user.setEnabled(false);
        ActivationToken activationToken = new ActivationToken();
        activationToken.setUser(user);
        activationToken.setValidUntilInEpoch(System.currentTimeMillis() + activationTokenLifetime);

        when(activationTokenRepository.findByToken(eq(token))).thenReturn(activationToken);

        assertFalse(user.isEnabled());

        authService.activate(token);

        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userArgumentCaptor.capture());
        User activatedUser = userArgumentCaptor.getValue();

        assertEquals(activatedUser, user);
        assertTrue(activatedUser.isEnabled());
    }

    @Test
    public void activate_shouldInvokeDeleteByUserOfActivationTokenRepositoryWhenTokenValid() throws Exception {
        String token = generatedToken;
        User user = new User();
        ActivationToken activationToken = new ActivationToken();
        activationToken.setValidUntilInEpoch(System.currentTimeMillis() + activationTokenLifetime);
        activationToken.setUser(user);
        when(activationTokenRepository.findByToken(eq(token))).thenReturn(activationToken);

        authService.activate(token);

        verify(activationTokenRepository, times(1)).deleteByUser(eq(user));
    }

    @Test
    public void activate_shouldPublishOnAccountActivationEventWhenTokenValidWithCorrectUser() throws Exception {
        String token = generatedToken;
        User user = new User();
        ActivationToken activationToken = new ActivationToken();
        activationToken.setValidUntilInEpoch(System.currentTimeMillis() + activationTokenLifetime);
        activationToken.setUser(user);
        when(activationTokenRepository.findByToken(eq(token))).thenReturn(activationToken);

        authService.activate(token);

        ArgumentCaptor<OnAccountActivation> onAccountActivationArgumentCaptor = ArgumentCaptor.forClass(OnAccountActivation.class);

        verify(eventPublisher, times(1)).publishEvent(onAccountActivationArgumentCaptor.capture());

        OnAccountActivation publishedEvent = onAccountActivationArgumentCaptor.getValue();

        assertEquals(user, publishedEvent.getUser());
    }

    @Test
    public void activate_shouldThrowExceptionWhenTokenExpired() throws Exception {
        String token = generatedToken;
        ActivationToken activationToken = new ActivationToken();
        activationToken.setValidUntilInEpoch(System.currentTimeMillis() - 10);

        when(activationTokenRepository.findByToken(eq(token))).thenReturn(activationToken);

        try {
            authService.activate(token);
            Assert.fail();
        } catch (InvalidActivationTokenException ex) {
            //test passed.
        }
    }

    @Test
    public void activate_shouldThrowExceptionWhenTokenNotFound() throws Exception {
        String token = generatedToken;

        when(activationTokenRepository.findByToken(eq(token))).thenReturn(null);

        try {
            authService.activate(token);
            Assert.fail();
        } catch (InvalidActivationTokenException ex) {
            //test passed.
        }
    }

    @Test
    public void resendActivationToken_shouldCreateCorrectActivationTokenForUser() throws Exception {
        User user = new User();

        authService.resendActivationToken(user);

        ArgumentCaptor<ActivationToken> activationTokenArgumentCaptor = ArgumentCaptor.forClass(ActivationToken.class);
        verify(activationTokenRepository, times(1)).save(activationTokenArgumentCaptor.capture());
        ActivationToken savedToken = activationTokenArgumentCaptor.getValue();

        assertEquals(generatedToken, savedToken.getToken());
        assertEquals(user, savedToken.getUser());
        assertEquals(user.getActivationToken(), savedToken);
        assertTrue(savedToken.getValidUntilInEpoch() > System.currentTimeMillis() + activationTokenLifetime - 50);
        assertTrue(savedToken.getValidUntilInEpoch() < System.currentTimeMillis() + activationTokenLifetime + 50);
    }

    @Test
    public void resendActivationToken_shouldPublishOnResendActivationEventForCorrectUser() throws Exception {
        User user = new User();

        authService.resendActivationToken(user);

        ArgumentCaptor<OnResendActivationToken> onResendActivationTokenArgumentCaptor = ArgumentCaptor.forClass(OnResendActivationToken.class);
        verify(eventPublisher, times(1)).publishEvent(onResendActivationTokenArgumentCaptor.capture());
        OnResendActivationToken firedEvent = onResendActivationTokenArgumentCaptor.getValue();

        assertEquals(user, firedEvent.getUser());
    }

    @Test
    public void login_shouldGenerateAccessAndRefreshTokensWhenCredentialsValid() throws Exception {
        LoginRequest request = new LoginRequest("ugur@example.com", "123456");
        User user = new User();
        user.setPassword("encoded_password");
        user.setLanguage(Language.TR);

        when(userRepository.findByEmail(eq(request.getEmail()))).thenReturn(user);
        when(passwordEncoder.matches(eq(request.getPassword()), eq(user.getPassword()))).thenReturn(true);

        LoginTransfer loginTransfer = authService.login(request);

        assertEquals(generatedToken, loginTransfer.getTokenTransfer().getAccessToken());
        assertEquals(generatedToken, loginTransfer.getTokenTransfer().getRefreshToken());

        ArgumentCaptor<RefreshToken> refreshTokenArgumentCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository, times(1)).save(refreshTokenArgumentCaptor.capture());
        RefreshToken generatedRefreshToken = refreshTokenArgumentCaptor.getValue();

        assertEquals(generatedToken, generatedRefreshToken.getToken());
        assertEquals(user, generatedRefreshToken.getUser());
    }

    @Test
    public void login_shouldThrowExceptionWhenUserNotFound() throws Exception {
        LoginRequest request = new LoginRequest("ugur@example.com", "123456");

        when(userRepository.findByEmail(eq(request.getEmail()))).thenReturn(null);

        try {
            authService.login(request);
            fail();
        } catch (LoginFailedException ex) {
            //test passed
        }
    }


    @Test
    public void login_shouldThrowExceptionWhenPasswordNull() throws Exception {
        LoginRequest request = new LoginRequest("ugur@example.com", null);

        when(userRepository.findByEmail(eq(request.getEmail()))).thenReturn(new User());

        try {
            authService.login(request);
            fail();
        } catch (LoginFailedException ex) {
            //test passed
        }
    }

    @Test
    public void login_shouldThrowExceptionWhenPasswordWrong() throws Exception {
        LoginRequest request = new LoginRequest("ugur@example.com", "123123");

        when(userRepository.findByEmail(eq(request.getEmail()))).thenReturn(new User());
        when(passwordEncoder.matches(eq(request.getPassword()), anyString())).thenReturn(false);

        try {
            authService.login(request);
            fail();
        } catch (LoginFailedException ex) {
            //test passed
        }
    }

    @Test
    public void recover() throws Exception {

    }

    @Test
    public void reset() throws Exception {

    }

    @Test
    public void refresh() throws Exception {

    }

    @Test
    public void logout() throws Exception {

    }

    @Test
    public void changePassword() throws Exception {

    }

    private RegisterRequest getRegisterRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("ugur@example.com");
        request.setPassword("123123");
        request.setPasswordConfirmation("123123");
        request.setFirstName("Ugur");
        request.setLastName("Hicyilmam");
        request.setLanguage("TR");
        return request;
    }

}