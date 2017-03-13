package com.ugurhicyilmam.controller;

import com.jayway.jsonpath.JsonPath;
import com.ugurhicyilmam.controller.request.RegisterRequest;
import com.ugurhicyilmam.response.Response;
import com.ugurhicyilmam.service.transfer.LoginTransfer;
import com.ugurhicyilmam.service.transfer.UserTransfer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@SuppressWarnings("unchecked")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void register_shouldRegisterWhenCorrect() throws Exception {

        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Ugur");
        request.setLastName("Hicyilmam");
        request.setEmail("ugur@yildiz.edu.tr");
        request.setLanguage("TR");
        request.setPassword("123123");
        request.setPasswordConfirmation("123123");

        String response = restTemplate.postForObject("/api/auth/register", request, String.class);

        assertEquals(request.getFirstName(), JsonPath.read(response, "$.data.userInformation.firstName"));
        assertEquals(request.getLastName(), JsonPath.read(response, "$.data.userInformation.lastName"));
    }

    @Test
    public void test() {

    }
}