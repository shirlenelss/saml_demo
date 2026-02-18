package com.example.saml_demo.controller;

import com.example.saml_demo.dto.CreateUserRequest;
import com.example.saml_demo.service.KeycloakUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserManagementController.class)
@Import(TestSecurityConfig.class)
public class UserManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private KeycloakUserService keycloakUserService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/users/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("User Management API is running"));
    }

    @Test
    public void testCreateUserValidationError() throws Exception {
        CreateUserRequest invalidRequest = new CreateUserRequest();
        invalidRequest.setUsername("ab"); // Too short
        invalidRequest.setPassword("123"); // Too short

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
