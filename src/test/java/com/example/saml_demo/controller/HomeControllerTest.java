package com.example.saml_demo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.saml2.provider.service.authentication.DefaultSaml2AuthenticatedPrincipal;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
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
        // create a real Saml2 principal and authentication and store in session
        DefaultSaml2AuthenticatedPrincipal principal = new DefaultSaml2AuthenticatedPrincipal(
                "testuser@example.com",
                Map.of()
        );
        principal.setRelyingPartyRegistrationId("keycloak");

        Saml2Authentication authentication = new Saml2Authentication(
                principal,
                "saml-response",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        SecurityContext securityContext = new SecurityContextImpl(authentication);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);

        mockMvc.perform(get("/").session(session))
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
}
