package com.ugurhicyilmam.service.impl;

import com.ugurhicyilmam.controller.request.LoginRequest;
import com.ugurhicyilmam.controller.request.RegisterRequest;
import com.ugurhicyilmam.event.OnAccountActivation;
import com.ugurhicyilmam.event.OnAccountCreation;
import com.ugurhicyilmam.event.OnAccountRecover;
import com.ugurhicyilmam.event.OnResendActivationToken;
import com.ugurhicyilmam.model.ActivationToken;
import com.ugurhicyilmam.model.RecoveryToken;
import com.ugurhicyilmam.model.RefreshToken;
import com.ugurhicyilmam.model.User;
import com.ugurhicyilmam.repository.ActivationTokenRepository;
import com.ugurhicyilmam.repository.RecoveryTokenRepository;
import com.ugurhicyilmam.repository.RefreshTokenRepository;
import com.ugurhicyilmam.repository.UserRepository;
import com.ugurhicyilmam.service.AuthService;
import com.ugurhicyilmam.service.exceptions.*;
import com.ugurhicyilmam.service.transfer.LoginTransfer;
import com.ugurhicyilmam.util.TokenUtils;
import com.ugurhicyilmam.util.enums.Language;
import org.junit.After;
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
    private long recoveryTokenLifeTime;

    private String generatedToken = "TOKEN_UTILS_GENERATED_TOKEN";
    private String base64GeneratedToken = "TOKEN_UTILS_BASE64_TOKEN";
    private String decodedGeneratedToken = generatedToken;

    private AuthService authService;

    @Before
    public void setUp() throws Exception {

        activationTokenLifetime = 100;
        accessTokenLifetime = 100;
        recoveryTokenLifeTime = 100;

        authService = new AuthServiceImpl(
                passwordEncoder,
                userRepository,
                activationTokenRepository,
                refreshTokenRepository,
                recoveryTokenRepository,
                eventPublisher,
                activationTokenLifetime,
                accessTokenLifetime,
                recoveryTokenLifeTime
        );

        PowerMockito.mockStatic(TokenUtils.class);
        Mockito.when(TokenUtils.generateToken()).thenReturn(generatedToken);
        Mockito.when(TokenUtils.encodeBase64(eq(generatedToken))).thenReturn(base64GeneratedToken);
        Mockito.when(TokenUtils.decodeBase64(eq(base64GeneratedToken))).thenReturn(decodedGeneratedToken);

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
        } catch (InvalidActivationTokenException ex) {
            return;
        }
        fail();
    }

    @Test
    public void activate_shouldThrowExceptionWhenTokenNotFound() throws Exception {
        String token = generatedToken;

        when(activationTokenRepository.findByToken(eq(token))).thenReturn(null);

        try {
            authService.activate(token);
        } catch (InvalidActivationTokenException ex) {
            return;
        }
        fail();
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

        assertEquals(generatedToken, TokenUtils.decodeBase64(loginTransfer.getTokenTransfer().getAccessToken()));
        assertEquals(generatedToken, TokenUtils.decodeBase64(loginTransfer.getTokenTransfer().getRefreshToken()));

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
        } catch (LoginFailedException ex) {
            return;
        }
        fail();
    }


    @Test
    public void login_shouldThrowExceptionWhenPasswordNull() throws Exception {
        LoginRequest request = new LoginRequest("ugur@example.com", null);

        when(userRepository.findByEmail(eq(request.getEmail()))).thenReturn(new User());

        try {
            authService.login(request);
        } catch (LoginFailedException ex) {
            return;
        }
        fail();
    }

    @Test
    public void login_shouldThrowExceptionWhenPasswordWrong() throws Exception {
        LoginRequest request = new LoginRequest("ugur@example.com", "123123");

        when(userRepository.findByEmail(eq(request.getEmail()))).thenReturn(new User());
        when(passwordEncoder.matches(eq(request.getPassword()), anyString())).thenReturn(false);

        try {
            authService.login(request);
        } catch (LoginFailedException ex) {
            return;
        }
        fail();
    }

    @Test
    public void recover_shouldThrowExceptionWhenUserNotFound() throws Exception {
        String email = "ugur@example.com";

        when(userRepository.findByEmail(eq(email))).thenReturn(null);

        try {
            authService.recover(email);
        } catch (UserNotFoundException ex) {
            return;
        }
        fail();
    }

    @Test
    public void recover_shouldCreateRecoveryTokenWhenUserFound() throws Exception {
        String email = "ugur@example.com";
        User user = new User();

        when(userRepository.findByEmail(eq(email))).thenReturn(user);

        authService.recover(email);

        ArgumentCaptor<RecoveryToken> recoveryTokenArgumentCaptor = ArgumentCaptor.forClass(RecoveryToken.class);
        verify(recoveryTokenRepository, times(1)).save(recoveryTokenArgumentCaptor.capture());
        RecoveryToken recoveryToken = recoveryTokenArgumentCaptor.getValue();

        assertEquals(user, recoveryToken.getUser());
        assertEquals(generatedToken, recoveryToken.getToken());
        assertTrue(recoveryToken.getValidUntilInEpoch() < System.currentTimeMillis() + recoveryTokenLifeTime + 50);
        assertTrue(recoveryToken.getValidUntilInEpoch() > System.currentTimeMillis() + recoveryTokenLifeTime - 50);
    }

    @Test
    public void recover_shouldPublishOnAccountRecoverEventWithCorrectUser() throws Exception {
        String email = "ugur@example.com";
        User user = new User();

        when(userRepository.findByEmail(eq(email))).thenReturn(user);

        authService.recover(email);

        ArgumentCaptor<OnAccountRecover> onAccountRecoverArgumentCaptor = ArgumentCaptor.forClass(OnAccountRecover.class);
        verify(eventPublisher, times(1)).publishEvent(onAccountRecoverArgumentCaptor.capture());
        OnAccountRecover firedEvent = onAccountRecoverArgumentCaptor.getValue();

        assertEquals(user, firedEvent.getUser());
    }

    @Test
    public void recover_shouldInvokeDeleteByUserWithCorrectUserBeforeSave() throws Exception {
        String email = "ugur@example.com";
        User user = new User();

        when(userRepository.findByEmail(eq(email))).thenReturn(user);

        authService.recover(email);

        InOrder inOrder = Mockito.inOrder(recoveryTokenRepository);

        inOrder.verify(recoveryTokenRepository, times(1)).deleteByUser(eq(user));
        inOrder.verify(recoveryTokenRepository, times(1)).save(any(RecoveryToken.class));
    }

    @Test
    public void reset_shouldSetPasswordWhenTokenValid() throws Exception {
        String token = "token";
        String password = "new_password";
        String encodedPassword = "encoded_password";

        RecoveryToken recoveryToken = new RecoveryToken();
        recoveryToken.setValidUntilInEpoch(System.currentTimeMillis() + recoveryTokenLifeTime);
        User user = new User();
        recoveryToken.setUser(user);

        when(recoveryTokenRepository.findByToken(eq(token))).thenReturn(recoveryToken);
        when(passwordEncoder.encode(eq(password))).thenReturn(encodedPassword);

        User returnedUser = authService.reset(token, password);

        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userArgumentCaptor.capture());
        User updatedUser = userArgumentCaptor.getValue();

        assertEquals(user, updatedUser);
        assertEquals(encodedPassword, updatedUser.getPassword());
        assertEquals(user, returnedUser);
        assertEquals(encodedPassword, returnedUser.getPassword());
    }

    @Test
    public void reset_shouldTokenShouldBeDeletedAfterUpdate() throws Exception {
        String token = "token";
        String password = "new_password";
        String encodedPassword = "encoded_password";

        RecoveryToken recoveryToken = new RecoveryToken();
        recoveryToken.setValidUntilInEpoch(System.currentTimeMillis() + recoveryTokenLifeTime);
        User user = new User();
        recoveryToken.setUser(user);

        when(recoveryTokenRepository.findByToken(eq(token))).thenReturn(recoveryToken);
        when(passwordEncoder.encode(eq(password))).thenReturn(encodedPassword);

        authService.reset(token, password);

        InOrder inOrder = Mockito.inOrder(userRepository, recoveryTokenRepository);

        inOrder.verify(userRepository, times(1)).save(eq(user));
        inOrder.verify(recoveryTokenRepository, times(1)).deleteByUser(eq(user));
    }

    @Test
    public void reset_shouldThrowExceptionWhenTokenNotFound() throws Exception {
        String token = "token";
        when(recoveryTokenRepository.findByToken(eq(token))).thenReturn(null);

        try {
            authService.reset(token, "password");
        } catch (RecoveryTokenInvalidException ex) {
            // passed
            return;
        }
        fail();
    }

    @Test
    public void reset_shouldThrowExceptionWhenTokenExpired() throws Exception {
        String token = "token";

        RecoveryToken recoveryToken = new RecoveryToken();
        recoveryToken.setValidUntilInEpoch(System.currentTimeMillis() - 50);

        when(recoveryTokenRepository.findByToken(eq(token))).thenReturn(recoveryToken);

        try {
            authService.reset(token, "password");
        } catch (RecoveryTokenInvalidException ex) {
            // passed
            return;
        }
        fail();
    }

    @Test
    public void refresh_shouldReturnLoginTransferWithCorrectRefreshToken() throws Exception {
        String token = "token";
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        User user = new User();
        user.setLanguage(Language.EN);
        refreshToken.setUser(user);

        when(refreshTokenRepository.findByToken(eq(token))).thenReturn(refreshToken);

        LoginTransfer loginTransfer = authService.refresh(token);

        assertEquals(generatedToken, TokenUtils.decodeBase64(loginTransfer.getTokenTransfer().getAccessToken()));
        assertEquals(generatedToken, TokenUtils.decodeBase64(loginTransfer.getTokenTransfer().getRefreshToken()));

        ArgumentCaptor<RefreshToken> refreshTokenArgumentCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository, times(1)).save(refreshTokenArgumentCaptor.capture());
        RefreshToken generatedRefreshToken = refreshTokenArgumentCaptor.getValue();

        assertEquals(generatedToken, generatedRefreshToken.getToken());
        assertEquals(user, generatedRefreshToken.getUser());
    }

    @Test
    public void refresh_shouldDeleteOldRefreshTokenTokenBeforeCreatingNew() throws Exception {
        String token = "token";
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        User user = new User();
        user.setLanguage(Language.EN);
        refreshToken.setUser(user);

        when(refreshTokenRepository.findByToken(eq(token))).thenReturn(refreshToken);

        authService.refresh(token);

        InOrder inOrder = Mockito.inOrder(refreshTokenRepository);
        inOrder.verify(refreshTokenRepository, times(1)).deleteByToken(eq(token));
        inOrder.verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    public void refresh_shouldThrowExceptionWhenTokenNotFound() throws Exception {
        String token = "token";

        when(refreshTokenRepository.findByToken(eq(token))).thenReturn(null);

        try {
            authService.refresh(token);
        } catch (RefreshTokenInvalidException ex) {
            return;
        }
        fail();
    }

    @Test
    public void getUserByValidAccessToken_shouldReturnUserWithValidAccessToken() throws Exception {
        LoginRequest request = new LoginRequest("ugur@example.com", "123456");
        User user = new User();
        user.setPassword("encoded_password");
        user.setLanguage(Language.TR);

        when(userRepository.findByEmail(eq(request.getEmail()))).thenReturn(user);
        when(passwordEncoder.matches(eq(request.getPassword()), eq(user.getPassword()))).thenReturn(true);

        LoginTransfer loginTransfer = authService.login(request);

        assertEquals(user, authService.getUserByValidAccessToken(TokenUtils.decodeBase64(loginTransfer.getTokenTransfer().getAccessToken())));
    }

    @Test
    public void getUserByValidAccessToken_shouldThrowExceptionWithInvalidToken() throws Exception {
        String accessToken = "acces_token";

        try {
            authService.getUserByValidAccessToken(accessToken);
        } catch (AccessTokenInvalidException ex) {
            return;
        }
        fail();

    }

    @Test
    public void logout_shouldRemoveRefreshTokenIfExists() throws Exception {
        String refreshToken = "refresh_token";

        authService.logout(refreshToken, "");

        verify(refreshTokenRepository, times(1)).deleteByToken(eq(refreshToken));
    }

    @Test
    public void logout_shouldRemoveAccessToken() throws Exception {
        login_shouldGenerateAccessAndRefreshTokensWhenCredentialsValid(); // this test logs in the user.

        assertNotNull(authService.getUserByValidAccessToken(generatedToken));

        authService.logout("", generatedToken);

        try {
            authService.getUserByValidAccessToken(generatedToken);
        } catch (AccessTokenInvalidException ex) {
            return;
        }
        fail();
    }


    @Test
    public void changePassword_shouldSetPasswordWhenCurrentPasswordCorrect() throws Exception {
        String currentPassword = "password";
        String newPassword = "new_password";
        String encodedCurrentPassword = "encoded_password";
        String encodedNewPassword = "encoded_new_password";

        User user = new User();
        user.setPassword(encodedCurrentPassword);

        when(passwordEncoder.matches(eq(currentPassword), eq(encodedCurrentPassword))).thenReturn(true);
        when(passwordEncoder.encode(eq(currentPassword))).thenReturn(encodedCurrentPassword);
        when(passwordEncoder.encode(eq(newPassword))).thenReturn(encodedNewPassword);

        authService.changePassword(user, currentPassword, newPassword);

        assertEquals(encodedNewPassword, user.getPassword());
    }

    @Test
    public void changePassword_shouldThrowExceptionIfCurrentPasswordWrong() throws Exception {
        String currentPassword = "password";
        String userPassword = "user_password";

        User user = new User();
        user.setPassword(userPassword);

        when(passwordEncoder.matches(eq(currentPassword), eq(userPassword))).thenReturn(false);

        try {
            authService.changePassword(user, currentPassword, "new_password");
        } catch (LoginFailedException ex) {
            return;
        }
        fail();
    }

    @Test
    public void changePassword_shouldThrowExceptionWhenCurrentPasswordNull() throws Exception {
        String currentPassword = null;

        try {
            authService.changePassword(new User(), currentPassword, "new_password");
        } catch (LoginFailedException ex) {
            return;
        }
        fail();
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