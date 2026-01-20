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

@WebMvcTest(HomeController.class)
@Import(TestSecurityConfig.class)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void homePageIsAccessibleAnonymously() throws Exception {
        mockMvc.perform(get("/").with(anonymous()))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("authenticated", false));
    }

    @Test
    void homePageShowsAuthenticatedUser() throws Exception {
        Saml2AuthenticatedPrincipal principal = createMockPrincipal("testuser@example.com");

        mockMvc.perform(get("/")
                        .with(SecurityMockMvcRequestPostProcessors.saml2Authentication(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("authenticated", true))
                .andExpect(model().attribute("name", "testuser@example.com"));
    }

    @Test
    void homePathAliasWorks() throws Exception {
        mockMvc.perform(get("/home").with(anonymous()))
                .andExpect(status().isOk())
                .andExpect(view().name("home"));
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
