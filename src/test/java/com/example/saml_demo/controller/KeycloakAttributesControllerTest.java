package com.example.saml_demo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(KeycloakAttributesController.class)
@Import(TestSecurityConfig.class)
public class KeycloakAttributesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void keycloakAttributesRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/keycloak/attributes"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void keycloakRolesRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/keycloak/roles"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void keycloakGroupsRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/keycloak/groups"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void authenticatedUserCanAccessKeycloakAttributes() throws Exception {
        Saml2AuthenticatedPrincipal principal = createMockPrincipal(
                "test-user",
                Map.of(
                        "EmailAddress", List.of("test@example.com"),
                        "FirstName", List.of("Test"),
                        "LastName", List.of("User"),
                        "Role", List.of("user", "admin"),
                        "member", List.of("/Developers", "/Admins")
                )
        );

        mockMvc.perform(get("/keycloak/attributes")
                        .with(SecurityMockMvcRequestPostProcessors.saml2Authentication(principal)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.username").value("test-user"))
                .andExpect(jsonPath("$.attributes.email").value("test@example.com"))
                .andExpect(jsonPath("$.attributes.roles").isArray());
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
