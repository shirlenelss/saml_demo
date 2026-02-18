package com.example.saml_demo.controller;

import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/keycloak")
public class KeycloakAttributesController {

    @GetMapping(value = "/attributes", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getKeycloakAttributes(@AuthenticationPrincipal Saml2AuthenticatedPrincipal principal) {
        Map<String, Object> result = new HashMap<>();

        // Basic user info
        result.put("username", principal.getName());
        result.put("relyingPartyRegistrationId", principal.getRelyingPartyRegistrationId());

        // SAML attributes as configured in Keycloak protocol mappers
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", principal.getFirstAttribute("EmailAddress"));
        attributes.put("firstName", principal.getFirstAttribute("FirstName"));
        attributes.put("lastName", principal.getFirstAttribute("LastName"));
        attributes.put("username", principal.getFirstAttribute("Username"));

        // Groups and roles from SAML
        List<Object> roles = principal.getAttribute("Role");
        List<Object> groups = principal.getAttribute("member");
        attributes.put("roles", roles);
        attributes.put("groups", groups);

        // Additional attributes if mapped
        attributes.put("organization", principal.getFirstAttribute("organization"));
        attributes.put("department", principal.getFirstAttribute("department"));
        attributes.put("employeeId", principal.getFirstAttribute("employee_id"));

        result.put("attributes", attributes);

        // All raw attributes for debugging
        result.put("allAttributes", principal.getAttributes());

        return result;
    }

    @GetMapping(value = "/roles", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getKeycloakRoles(@AuthenticationPrincipal Saml2AuthenticatedPrincipal principal) {
        Map<String, Object> result = new HashMap<>();

        // Extract Keycloak roles
        List<Object> realmRoles = principal.getAttribute("realm_roles");
        List<Object> clientRoles = principal.getAttribute("client_roles");
        List<Object> resourceAccess = principal.getAttribute("resource_access");

        result.put("realmRoles", realmRoles);
        result.put("clientRoles", clientRoles);
        result.put("resourceAccess", resourceAccess);

        return result;
    }

    @GetMapping(value = "/groups", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getKeycloakGroups(@AuthenticationPrincipal Saml2AuthenticatedPrincipal principal) {
        Map<String, Object> result = new HashMap<>();

        // Extract Keycloak groups
        List<Object> groups = principal.getAttribute("groups");
        List<Object> groupMembership = principal.getAttribute("group_membership");

        result.put("groups", groups);
        result.put("groupMembership", groupMembership);

        return result;
    }
}
