package com.ugurhicyilmam.service.impl;

import com.ugurhicyilmam.model.ActivationToken;
import com.ugurhicyilmam.model.User;
import com.ugurhicyilmam.service.EmailService;
import com.ugurhicyilmam.util.enums.Language;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.subethamail.wiser.Wiser;

import static com.ugurhicyilmam.util.WiserAssertions.assertReceivedMessage;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EmailServiceImplTest {

    @Autowired
    private EmailService emailService;

    private Wiser wiser;

    @Before
    public void setUp() throws Exception {
        wiser = new Wiser();
        wiser.start();
    }

    @After
    public void tearDown() throws Exception {
        wiser.stop();
    }

    @Test
    public void sendActivationEmail_shouldSendCorrectMailWhenLanguageTR() throws Exception {
        User user = new User();
        user.setEmail("ugur@yildiz.edu.tr");
        user.setLanguage(Language.TR);
        ActivationToken activationToken = new ActivationToken();
        activationToken.setToken("Token");
        user.setActivationToken(activationToken);
        activationToken.setUser(user);

        emailService.sendActivationEmail(user);

        assertReceivedMessage(wiser).from("auth@localhost.com")
                .to(user.getEmail())
                .withSubject("Hesap Aktivasyonu - Uninet");
    }

}