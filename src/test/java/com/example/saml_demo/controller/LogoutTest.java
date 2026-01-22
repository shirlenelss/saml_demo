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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HomeController.class)
@Import(TestSecurityConfig.class)
class LogoutTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void logoutRedirectsToRoot() throws Exception {
        Saml2AuthenticatedPrincipal principal = createMockPrincipal("testuser@example.com");

        mockMvc.perform(get("/logout")
                        .with(SecurityMockMvcRequestPostProcessors.saml2Authentication(principal)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"));
    }

    private Saml2AuthenticatedPrincipal createMockPrincipal(String name) {
        return new Saml2AuthenticatedPrincipal() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public Map<String, List<Object>> getAttributes() {
                return Map.of();
            }
        };
    }
}

