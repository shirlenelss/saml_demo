# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Run Commands

```bash
# Run the application
mvn spring-boot:run

# Run all tests
mvn test

# Build the project
mvn clean package

# Start Keycloak IdP (required for SAML authentication)
docker-compose up
```

## Architecture

This is a Spring Boot 3.2.5 SAML2 Service Provider (SP) that authenticates against a Keycloak Identity Provider (IdP).

**Authentication Flow:**
1. User visits protected endpoint → redirected to `/saml2/authenticate/keycloak`
2. Spring Security generates SAML AuthnRequest → redirects to Keycloak (port 8081)
3. User authenticates with Keycloak
4. Keycloak posts SAMLResponse back to the app
5. Spring Security validates response, creates authenticated session

**Key Components:**
- `SecurityConfig` - Configures SAML2 login/logout, defines public endpoints (`/`, `/home`, `/error`) vs protected (everything else)
- `HomeController` - Landing page at `/` and `/home`
- `UserController` - Protected `/user` endpoint displaying SAML attributes from `Saml2AuthenticatedPrincipal`
- `application.yml` - SAML configuration: entity ID (`spring-sp-demo`), Keycloak metadata URI, signing/decryption credentials

**Auto-generated SAML Endpoints:**
- `/saml2/authenticate/keycloak` - Initiate SAML login
- `/saml2/service-provider-metadata/keycloak` - SP metadata XML
- `/logout` - SAML single logout

## Testing

Tests use `@WebMvcTest` with MockMvc. SAML authentication is mocked using `SecurityMockMvcRequestPostProcessors.saml2Authentication()` which creates a mock `Saml2AuthenticatedPrincipal`.

Run a single test class:
```bash
mvn test -Dtest=HomeControllerTest
```

## Configuration

- **SP credentials**: `src/main/resources/credentials/sp.key` and `sp.crt`
- **Keycloak**: Runs on port 8081, admin credentials in docker-compose.yml
- **SAML attributes**: EmailAddress, FirstName, LastName (configured in Keycloak)

Regenerate SP credentials:
```bash
openssl req -newkey rsa:2048 -nodes -keyout sp.key -x509 -days 365 -out sp.crt
```
