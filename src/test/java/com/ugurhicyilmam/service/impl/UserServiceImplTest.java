package com.ugurhicyilmam.service.impl;

import com.ugurhicyilmam.model.User;
import com.ugurhicyilmam.repository.UserRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserServiceImplTest {


    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    private UserServiceImpl userService;

    @Before
    public void setUp() throws Exception {
        userService = new UserServiceImpl(passwordEncoder, userRepository);
    }

    @After
    public void tearDown() throws Exception {
        userService = null;
    }

    @Test
    public void findByEmail_shouldCallAndReturnRepoCorrectly() throws Exception {
        User user = new User();
        String email = "ugur@yildiz.edu.tr";
        when(userRepository.findByEmail(eq(email))).thenReturn(user);

        User response = userService.findByEmail(email);

        verify(userRepository, times(1)).findByEmail(eq(email));
        assertEquals(user, response);
    }

    @Test
    public void create_shouldCorrectlyInitializeCreateRelatedProperties() throws Exception {
        User user = new User();
        userService.create(user);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();

        assertTrue(capturedUser.getRegisteredAt() > System.currentTimeMillis() - 10 && capturedUser.getRegisteredAt() < System.currentTimeMillis() + 10);
        assertTrue(capturedUser.isAccountNonExpired());
        assertTrue(capturedUser.isAccountNonLocked());
        assertTrue(capturedUser.isCredentialsNonExpired());
        assertFalse(capturedUser.isEnabled());
    }

    @Test
    public void create_shouldUseSameUserObject() throws Exception {
        User user = new User();
        userService.create(user);
        verify(userRepository, times(1)).save(eq(user));
    }

    @Test
    public void activateUser_shouldInvokeSaveWithEnabledUser() throws Exception {
        User user = new User();
        user.setEnabled(false);
        userService.activateUser(user);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        verify(userRepository, times(1)).save(userCaptor.capture());
        assertEquals(user, userCaptor.getValue());
        assertTrue(user.isEnabled());
        assertTrue(userCaptor.getValue().isEnabled());
    }
}