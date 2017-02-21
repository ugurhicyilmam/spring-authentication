package com.ugurhicyilmam.service.impl;

import com.ugurhicyilmam.model.ActivationToken;
import com.ugurhicyilmam.model.User;
import com.ugurhicyilmam.repository.ActivationTokenRepository;
import com.ugurhicyilmam.service.ActivationTokenService;
import com.ugurhicyilmam.service.exceptions.InvalidActivationTokenException;
import com.ugurhicyilmam.util.TokenUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TokenUtils.class)
@PowerMockRunnerDelegate(SpringRunner.class)
public class ActivationTokenServiceImplTest {

    @MockBean
    private ActivationTokenRepository activationTokenRepository;
    private long tokenLifeLength = 1;

    private ActivationTokenService activationTokenService;

    private final String generatedActivationToken = "SOME_GIBBERISH_TOKEN";

    @Before
    public void setUp() throws Exception {
        activationTokenService = new ActivationTokenServiceImpl(activationTokenRepository, tokenLifeLength);

        PowerMockito.mockStatic(TokenUtils.class);
        Mockito.when(TokenUtils.generateToken()).thenReturn(generatedActivationToken);
    }

    @After
    public void tearDown() throws Exception {
        activationTokenService = null;
    }

    @Test
    public void issueNewToken_shouldRemoveAndIssueNewTokenWhenUserHasToken() throws Exception {
        User user = new User();
        ActivationToken activationToken = new ActivationToken();
        when(activationTokenRepository.findByUser(eq(user))).thenReturn(activationToken);

        activationTokenService.issueNewToken(user);

        // verify invocation of delete
        verify(activationTokenRepository, times(1)).delete(eq(activationToken));

        //verify generated activation token
        ArgumentCaptor<ActivationToken> activationTokenArgumentCaptor = ArgumentCaptor.forClass(ActivationToken.class);
        verify(activationTokenRepository, times(1)).save(activationTokenArgumentCaptor.capture());
        verifyActivationTokenForUser(activationTokenArgumentCaptor.getValue(), user);
    }

    @Test
    public void issueNewToken_shouldIssueNewTokenWhenUserHasNoToken() throws Exception {
        User user = new User();
        when(activationTokenRepository.findByUser(eq(user))).thenReturn(null);

        activationTokenService.issueNewToken(user);

        // verify non invocation
        verify(activationTokenRepository, times(0)).delete(any(ActivationToken.class));

        //verify generated activation token
        ArgumentCaptor<ActivationToken> activationTokenArgumentCaptor = ArgumentCaptor.forClass(ActivationToken.class);
        verify(activationTokenRepository, times(1)).save(activationTokenArgumentCaptor.capture());
        verifyActivationTokenForUser(activationTokenArgumentCaptor.getValue(), user);
    }

    private void verifyActivationTokenForUser(ActivationToken token, User user) throws Exception {
        assertEquals(user, token.getUser());
        assertEquals(generatedActivationToken, token.getToken());
        assertTrue(token.getValidUntil() > System.currentTimeMillis() + hoursToMillis(1) - 10);
        assertTrue(token.getValidUntil() < System.currentTimeMillis() + hoursToMillis(1) + 10);
    }

    private long hoursToMillis(long hours) {
        return TimeUnit.HOURS.toMillis(hours);
    }

    @Test
    public void test_hoursToMillis() throws Exception {

        assertEquals(60 * 60 * 1000, hoursToMillis(1));
        assertEquals(2 * 60 * 60 * 1000, hoursToMillis(2));
        assertEquals(3 * 60 * 60 * 1000, hoursToMillis(3));
        assertEquals(4 * 60 * 60 * 1000, hoursToMillis(4));
        assertEquals(-1 * 60 * 60 * 1000, hoursToMillis(-1));
        assertEquals(0, hoursToMillis(0));
    }


    @Test
    public void findByUser_shouldInvokeRepoWithCorrectAttr() throws Exception {
        User user = new User();
        activationTokenService.findByUser(user);
        verify(activationTokenRepository, times(1)).findByUser(eq(user));
    }

    @Test
    public void findByUser_shouldReturnWhatRepoReturns() throws Exception {
        User user = new User();
        ActivationToken activationToken = new ActivationToken();
        when(activationTokenRepository.findByUser(eq(user))).thenReturn(activationToken);
        ActivationToken response = activationTokenService.findByUser(user);
        assertEquals(activationToken, response);
    }

    @Test
    public void removeIfExistsForUser_shouldInvokeDeleteIfExists() throws Exception {
        User user = new User();
        ActivationToken activationToken = new ActivationToken();
        when(activationTokenRepository.findByUser(eq(user))).thenReturn(activationToken);

        activationTokenService.removeIfExistsForUser(user);

        verify(activationTokenRepository, times(1)).delete(eq(activationToken));
    }

    @Test
    public void removeIfExistsForUser_shouldNotInvokeDeleteIfNotExists() throws Exception {
        User user = new User();
        when(activationTokenRepository.findByUser(eq(user))).thenReturn(null);

        activationTokenService.removeIfExistsForUser(user);

        verify(activationTokenRepository, times(0)).delete(any(ActivationToken.class));
    }

    @Test
    public void findValidToken_shouldReturnIfTokenValid() throws Exception {
        String token = "Valid_Token";
        ActivationToken activationToken = new ActivationToken();
        activationToken.setValidUntil(System.currentTimeMillis() + 1000); // valid
        when(activationTokenRepository.findByToken(token)).thenReturn(activationToken);

        assertEquals(activationToken, activationTokenService.findValidToken(token));
    }

    @Test
    public void isValid_shouldThrowExceptionIfTokenExpired() throws Exception {
        String token = "Token";
        ActivationToken activationToken = new ActivationToken();
        activationToken.setValidUntil(System.currentTimeMillis() - 1000); // invalid
        when(activationTokenRepository.findByToken(token)).thenReturn(activationToken);

        try {
            activationTokenService.findValidToken(token);
        } catch (InvalidActivationTokenException ex) {
            return;
        }

        fail();
    }

    @Test
    public void isValid_shouldThrowExceptionIfTokenNotFound() throws Exception {
        String token = "Token";
        when(activationTokenRepository.findByToken(token)).thenReturn(null);

        try {
            activationTokenService.findValidToken(token);
        } catch (InvalidActivationTokenException ex) {
            return;
        }

        fail();
    }

}