package com.example.saml_demo.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class UserController {

    @GetMapping("/user")
    public String user(@AuthenticationPrincipal Saml2AuthenticatedPrincipal principal, Model model) {
        model.addAttribute("name", principal.getName());
        model.addAttribute("emailAddress", principal.getFirstAttribute("EmailAddress"));
        model.addAttribute("firstName", principal.getFirstAttribute("FirstName"));
        model.addAttribute("lastName", principal.getFirstAttribute("LastName"));

        // Get all attributes for display
        Map<String, String> attributes = principal.getAttributes().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream()
                                .map(Object::toString)
                                .collect(Collectors.joining(", "))
                ));
        model.addAttribute("attributes", attributes);

        return "user";
    }
}
