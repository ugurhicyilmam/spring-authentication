package com.ugurhicyilmam.service.impl;

import com.ugurhicyilmam.controller.request.RegisterRequest;
import com.ugurhicyilmam.event.OnAccountCreation;
import com.ugurhicyilmam.model.User;
import com.ugurhicyilmam.service.UserService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AuthServiceImplTest {

    @MockBean
    private UserService userService;

    @MockBean
    private ApplicationEventPublisher eventPublisher;

    private AuthServiceImpl authService;

    private RegisterRequest sampleRegisterRequest;

    @Before
    public void setUp() throws Exception {
        authService = new AuthServiceImpl(userService, eventPublisher);

        this.sampleRegisterRequest = new RegisterRequest();
        this.sampleRegisterRequest.setFirstName("Ugur");
        this.sampleRegisterRequest.setLastName("Hicyilmam");
        this.sampleRegisterRequest.setEmail("ugurhicyilmam@gmail.com");
        this.sampleRegisterRequest.setPassword("password");
        this.sampleRegisterRequest.setPasswordConfirmation("password");
    }

    @After
    public void tearDown() throws Exception {
        authService = null;
    }

    @Test
    public void register_shouldInvokeSaveWithCorrectlyMappedUser() throws Exception {
        authService.register(sampleRegisterRequest);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService, times(1)).create(userCaptor.capture());

        ensureEqualityOfRequestAndUser(sampleRegisterRequest, userCaptor.getValue());
    }

    @Test
    public void register_shouldInvokePublishWithCorrectlyMappedUser() throws Exception {
        authService.register(sampleRegisterRequest);

        ArgumentCaptor<OnAccountCreation> eventCaptor = ArgumentCaptor.forClass(OnAccountCreation.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

        ensureEqualityOfRequestAndUser(sampleRegisterRequest, eventCaptor.getValue().getUser());
    }

    @Test
    public void register_shouldUserSameUserObject() throws Exception {
        User user = authService.register(sampleRegisterRequest);

        verify(userService, times(1)).create(eq(user));

        ArgumentCaptor<OnAccountCreation> eventCaptor = ArgumentCaptor.forClass(OnAccountCreation.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        assertEquals(user, eventCaptor.getValue().getUser());
    }

    private void ensureEqualityOfRequestAndUser(RegisterRequest registerRequest, User user) throws Exception {
        assertEquals(registerRequest.getEmail(), user.getEmail());
        assertEquals(registerRequest.getFirstName(), user.getFirstName());
        assertEquals(registerRequest.getLastName(), user.getLastName());
        assertEquals(registerRequest.getPassword(), user.getPassword());
    }


}