package com.example.saml_demo.controller;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.saml2.provider.service.authentication.DefaultSaml2AuthenticatedPrincipal;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

public class SecurityMockMvcRequestPostProcessors {

    public static RequestPostProcessor saml2Authentication(Saml2AuthenticatedPrincipal principal) {
        DefaultSaml2AuthenticatedPrincipal defaultPrincipal = new DefaultSaml2AuthenticatedPrincipal(
                principal.getName(),
                principal.getAttributes()
        );
        defaultPrincipal.setRelyingPartyRegistrationId("ssocircle");

        Saml2Authentication saml2Authentication = new Saml2Authentication(
                defaultPrincipal,
                "saml-response",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        return authentication(saml2Authentication);
    }
}
