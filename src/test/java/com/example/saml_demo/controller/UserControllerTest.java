package com.example.saml_demo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void userPageRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/user").with(anonymous()))
                .andExpect(status().isForbidden());
    }

    @Test
    void userPageDisplaysAttributes() throws Exception {
        Saml2AuthenticatedPrincipal principal = createMockPrincipal(
                "testuser@example.com",
                Map.of(
                        "EmailAddress", List.of("testuser@example.com"),
                        "FirstName", List.of("Test"),
                        "LastName", List.of("User")
                )
        );

        mockMvc.perform(get("/user")
                        .with(SecurityMockMvcRequestPostProcessors.saml2Authentication(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("user"))
                .andExpect(model().attribute("name", "testuser@example.com"))
                .andExpect(model().attribute("emailAddress", "testuser@example.com"))
                .andExpect(model().attribute("firstName", "Test"))
                .andExpect(model().attribute("lastName", "User"));
    }

    @Test
    void userPageHandlesMissingAttributes() throws Exception {
        Saml2AuthenticatedPrincipal principal = createMockPrincipal(
                "user123",
                Map.of()
        );

        mockMvc.perform(get("/user")
                        .with(SecurityMockMvcRequestPostProcessors.saml2Authentication(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("user"))
                .andExpect(model().attribute("name", "user123"))
                .andExpect(model().attributeDoesNotExist("emailAddress"))
                .andExpect(model().attributeDoesNotExist("firstName"))
                .andExpect(model().attributeDoesNotExist("lastName"));
    }

    private Saml2AuthenticatedPrincipal createMockPrincipal(String name, Map<String, List<Object>> attributes) {
        return new Saml2AuthenticatedPrincipal() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public Map<String, List<Object>> getAttributes() {
                return attributes;
            }
        };
    }
}
