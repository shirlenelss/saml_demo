package com.example.saml_demo.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SamlDiagnosticsController {

    private final RelyingPartyRegistrationRepository registrations;

    public SamlDiagnosticsController(RelyingPartyRegistrationRepository registrations) {
        this.registrations = registrations;
    }

    @GetMapping(value = "/saml/registrations", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> listRegistrations() {
        Map<String, Object> result = new HashMap<>();
        List<String> ids = new ArrayList<>();

        // RelyingPartyRegistrationRepository does not expose an iterator in the interface,
        // but many implementations are Iterable. We attempt to iterate if possible.
        if (registrations instanceof Iterable) {
            for (Object r : (Iterable<?>) registrations) {
                if (r instanceof RelyingPartyRegistration) {
                    ids.add(((RelyingPartyRegistration) r).getRegistrationId());
                } else if (r != null) {
                    ids.add(r.toString());
                }
            }
        } else {
            // fallback: try to probe the known registration id 'keycloak'
            RelyingPartyRegistration rp = registrations.findByRegistrationId("keycloak");
            if (rp != null) {
                ids.add(rp.getRegistrationId());
            }
        }

        result.put("registrations", ids);
        return result;
    }

    @GetMapping(value = "/saml/metadata/{id}", produces = MediaType.TEXT_PLAIN_VALUE)
    public String showMetadata(@PathVariable String id) {
        // Provide a simple hinted URL where metadata should be available if registration exists
        return "/saml2/service-provider-metadata/" + id;
    }
}

