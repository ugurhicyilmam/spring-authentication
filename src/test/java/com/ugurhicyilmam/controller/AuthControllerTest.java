package com.ugurhicyilmam.controller;

import com.ugurhicyilmam.controller.request.RegisterRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@SuppressWarnings("unchecked")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
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
    // Acceptance test boilerplate.
    @Test
    public void register_shouldRegisterWhenCorrect() throws Exception {

        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Ugur");
        request.setLastName("Hicyilmam");
        request.setEmail("ugur@yildiz.edu.tr");
        request.setLanguage("TR");
        request.setPassword("123123");
        request.setPasswordConfirmation("123123");

        given().contentType("application/json").body(request)
                .when().post("/api/auth/register")
                .then().statusCode(200)
                .body("data.userInformation.firstName", equalTo(request.getFirstName()),
                        "data.userInformation.lastName", equalTo(request.getLastName()),
                        "data.userInformation.email", equalTo(request.getEmail()),
                        "data.userInformation.language", equalTo(request.getLanguage()));

    }

    @Test
    public void test() {

    }
}