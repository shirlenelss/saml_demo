package com.example.saml_demo.controller;

import org.springframework.http.MediaType;
import org.springframework.security.saml2.provider.service.metadata.OpenSamlMetadataResolver;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.web.DefaultRelyingPartyRegistrationResolver;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class MetadataController {

    private final DefaultRelyingPartyRegistrationResolver registrationResolver;
    private final OpenSamlMetadataResolver metadataResolver;

    public MetadataController(RelyingPartyRegistrationRepository registrations) {
        this.registrationResolver = new DefaultRelyingPartyRegistrationResolver(registrations);
        this.metadataResolver = new OpenSamlMetadataResolver();
    }

    @GetMapping(value = "/saml/metadata/{registrationId}", produces = MediaType.APPLICATION_XML_VALUE)
    public String metadata(@PathVariable String registrationId, HttpServletRequest request) {
        RelyingPartyRegistration registration = registrationResolver.resolve(request, registrationId);
        if (registration == null) {
            throw new IllegalArgumentException("Registration not found: " + registrationId);
        }
        return metadataResolver.resolve(registration);
    }
}
