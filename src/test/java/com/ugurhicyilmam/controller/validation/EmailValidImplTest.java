package com.ugurhicyilmam.controller.validation;

import com.ugurhicyilmam.model.User;
import com.ugurhicyilmam.service.UserService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EmailValidImplTest {

    @MockBean
    private UserService userService;

    private EmailValidImpl emailValid;

    @Before
    public void setUp() throws Exception {
        emailValid = new EmailValidImpl(userService, true);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void isValid_shouldReturnTrueWhenEmailValidAndDomainValidAndEmailUnique() throws Exception {
        when(userService.findByEmail(anyString())).thenReturn(null);

        String user = "ugur";

        for (String domain : emailValid.validDomains()) {
            String email = user + "@" + domain;
            assertTrue(emailValid.isValid(email, null));
        }
    }

    @Test
    public void isValid_shouldReturnFalseWhenEmailValidAndDomainValidButEmailNotUnique() throws Exception {
        when(userService.findByEmail(anyString())).thenReturn(new User());

        String user = "ugur";

        for (String domain : emailValid.validDomains()) {
            String email = user + "@" + domain;
            assertFalse(emailValid.isValid(email, null));
        }
    }

    @Test
    public void isValid_shouldReturnFalseWhenEmailValidButDomainInvalidAndEmailUnique() throws Exception {
        when(userService.findByEmail(anyString())).thenReturn(null);

        String user = "ugur";

        for (String domain : emailValid.validDomains()) {
            String email = user + "@" + "invalid" + domain;
            assertFalse(emailValid.isValid(email, null));
        }
    }

    @Test
    public void isValid_shouldReturnFalseWhenEmailInvalidAndDomainNotValidated() throws Exception {
        emailValid = new EmailValidImpl(userService, false);

        when(userService.findByEmail(anyString())).thenReturn(null);

        assertFalse(emailValid.isValid("ugur", null));
        assertFalse(emailValid.isValid("", null));
        assertFalse(emailValid.isValid(null, null));
        assertFalse(emailValid.isValid("ugur@.", null));
        assertFalse(emailValid.isValid("ugur@%*.com", null));
        assertFalse(emailValid.isValid("ugur@    ", null));
        assertFalse(emailValid.isValid("ugur@ugur@ugur", null));
    }

    @Test
    public void isValid_shouldReturnTrueWhenEmailValidAndUniqueAndDomainNotValidated() throws Exception {
        emailValid = new EmailValidImpl(userService, false);

        when(userService.findByEmail(anyString())).thenReturn(null);

        assertTrue(emailValid.isValid("ugur@somedomainthatnotexistindomainmap.com", null));
    }
}