package com.example.saml_demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;

@SpringBootTest
class SamlDemoApplicationTests {

    @MockBean
    private RelyingPartyRegistrationRepository relyingPartyRegistrationRepository;

    @Test
    void contextLoads() {
    }
}
