package com.ugurhicyilmam.event.listener;

import com.ugurhicyilmam.event.OnAccountCreation;
import com.ugurhicyilmam.model.User;
import com.ugurhicyilmam.service.EmailService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AuthEventListenerTest {

    @MockBean
    private EmailService emailService;

    private AuthEventListener authEventListener;

    @Before
    public void setUp() throws Exception {
        authEventListener = new AuthEventListener(emailService);
    }

    @After
    public void tearDown() throws Exception {
        authEventListener = null;
    }

    @Test
    public void handleOnAccountCreation() throws Exception {
        User user = new User();
        OnAccountCreation onAccountCreation = new OnAccountCreation(user);
        authEventListener.handleOnAccountCreation(onAccountCreation);
        verify(emailService, times(1)).sendActivationEmail(eq(user));
    }

}