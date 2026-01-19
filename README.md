# SAML Demo for Swedish eID

A Spring Boot application demonstrating SAML 2.0 authentication using Spring Security SAML2 Service Provider.

## Architecture

```
┌─────────────────┐         SAML 2.0          ┌─────────────────┐
│   Spring Boot   │  ◄──────────────────────► │   SSOCircle     │
│   Application   │     AuthnRequest/         │   (Test IdP)    │
│   (SP)          │     SAMLResponse          │                 │
└─────────────────┘                           └─────────────────┘
```

- **Service Provider (SP)**: This Spring Boot application
- **Identity Provider (IdP)**: SSOCircle (free test IdP)

## Prerequisites

- Java 21 or higher
- Gradle 8.x

## Quick Start

1. **Build the application**
   ```bash
   ./gradlew build
   ```

2. **Run the application**
   ```bash
   ./gradlew bootRun
   ```

3. **Access the application**
   - Home page: http://localhost:8080/
   - SP Metadata: http://localhost:8080/saml2/service-provider-metadata/ssocircle

## SSOCircle IdP Setup

To complete the SAML integration, you need to register this SP with SSOCircle:

1. Create a free account at https://idp.ssocircle.com
2. Log in and navigate to "Manage Metadata" → "Add new Service Provider"
3. Enter your SP metadata URL: `http://localhost:8080/saml2/service-provider-metadata/ssocircle`
4. Or paste the metadata XML directly (fetch from the URL above)
5. Save the configuration

After registration, you can authenticate via SAML by clicking "Login with SAML" on the home page.

## Project Structure

```
src/main/java/com/example/saml_demo/
├── SamlDemoApplication.java      # Spring Boot entry point
├── config/
│   └── SecurityConfig.java       # SAML2 security configuration
└── controller/
    ├── HomeController.java       # Public landing page
    └── UserController.java       # Protected user info endpoint

src/main/resources/
├── application.yml               # SAML2 configuration
├── credentials/
│   ├── sp.key                    # SP private key
│   └── sp.crt                    # SP certificate
└── templates/
    ├── home.html                 # Landing page template
    └── user.html                 # User attributes template
```

## Endpoints

| Endpoint | Description | Auth Required |
|----------|-------------|---------------|
| `/` or `/home` | Landing page with login/logout links | No |
| `/user` | Display authenticated user's SAML attributes | Yes |
| `/saml2/authenticate/ssocircle` | Initiate SAML login | No |
| `/logout` | Logout and end session | Yes |
| `/saml2/service-provider-metadata/ssocircle` | SP metadata XML | No |

## SAML Attributes

After successful authentication, the following attributes may be available (depending on IdP configuration):

- `EmailAddress` - User's email
- `FirstName` - User's first name
- `LastName` - User's last name

For Swedish eID integration, additional attributes would include:
- `personnummer` - Swedish personal identity number
- Additional identity attributes as configured by the IdP

## Configuration

The SAML2 configuration is in `src/main/resources/application.yml`:

```yaml
spring:
  security:
    saml2:
      relyingparty:
        registration:
          ssocircle:
            entity-id: saml-demo-sp
            signing:
              credentials:
                - private-key-location: classpath:credentials/sp.key
                  certificate-location: classpath:credentials/sp.crt
            assertingparty:
              metadata-uri: https://idp.ssocircle.com/meta-idp.xml
```

## Generating New SP Credentials

To generate new SP signing credentials:

```bash
openssl req -x509 -newkey rsa:2048 \
  -keyout src/main/resources/credentials/sp.key \
  -out src/main/resources/credentials/sp.crt \
  -days 365 -nodes \
  -subj "/CN=saml-demo-sp/O=Demo/C=SE"
```

## Running Tests

```bash
./gradlew test
```

## Using a Different IdP

To use a different Identity Provider, update `application.yml`:

1. Change the registration name (e.g., `ssocircle` → `okta`)
2. Update the `metadata-uri` to point to your IdP's metadata
3. Register this SP with your IdP using the metadata endpoint

## Troubleshooting

### "Could not find org.opensaml" error
Ensure the Shibboleth Maven repository is in `build.gradle`:
```groovy
repositories {
    mavenCentral()
    maven { url 'https://build.shibboleth.net/maven/releases/' }
}
```

### SAML Response validation fails
- Ensure system clock is synchronized (SAML assertions are time-sensitive)
- Check that SP certificate matches what's registered with the IdP
- Enable debug logging: `logging.level.org.springframework.security: DEBUG`

## License

This is a demonstration project for educational purposes.
