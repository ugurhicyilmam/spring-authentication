package com.ugurhicyilmam.controller;

import com.ugurhicyilmam.controller.request.RegisterRequest;
import com.ugurhicyilmam.response.Response;
import com.ugurhicyilmam.service.transfer.LoginTransfer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.fail;

@SuppressWarnings("unchecked")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

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

        Response<LoginTransfer> response = restTemplate.postForObject("/api/auth/register", request, Response.class);

        System.out.println("#######");
        System.out.println(response);
        fail();
    }

    @Test
    public void test() {

    }
}