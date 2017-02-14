package com.ugurhicyilmam.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
public class HelloControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;


    @Before
    public void setUp() throws Exception {


        mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                .build();
    }

    @Test
    public void hello() throws Exception {
        this.mockMvc.perform(get("/hello")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

}