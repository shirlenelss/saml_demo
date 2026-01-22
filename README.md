# SAML Demo for Swedish eID

A Spring Boot application demonstrating SAML 2.0 authentication using Spring Security SAML2 Service Provider with Keycloak as the Identity Provider.

## Architecture

```
┌─────────────────┐         SAML 2.0          ┌─────────────────┐
│   Spring Boot   │  ◄──────────────────────► │    Keycloak     │
│   Application   │     AuthnRequest/         │    (IdP)        │
│   (SP)          │     SAMLResponse          │    Port 8081    │
│   Port 8080     │                           │                 │
└─────────────────┘                           └─────────────────┘
```

- **Service Provider (SP)**: This Spring Boot application (`spring-sp-demo`)
- **Identity Provider (IdP)**: Keycloak running in Docker

## Prerequisites

- Java 21 or higher
- Maven 3.6+
- Docker & Docker Compose

## Quick Start

1. **Start Keycloak IdP**
   ```bash
   docker-compose up -d
   ```
   Wait for Keycloak to start (check http://localhost:8081)

2. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

3. **Access the application**
   - Home page: http://localhost:8080/
   - Click "Login with SAML" to authenticate

## Keycloak Setup

Keycloak admin console: http://localhost:8081/admin
- **Username:** `admin`
- **Password:** ``

### Configure SAML Client

1. Select **SAML-realm** from the realm dropdown
2. Go to **Clients** → **spring-sp-demo**
3. Ensure the client is enabled with these settings:
   - **Root URL:** `http://localhost:8080`
   - **Valid redirect URIs:** `http://localhost:8080/*`

### Add Attribute Mappers

To send user attributes in SAML assertions:

1. Go to **Clients** → **spring-sp-demo** → **Client scopes** → **spring-sp-demo-dedicated**
2. Click **Add mapper** → **By configuration** → **User Property**
3. Create these mappers:

| Name | Property | SAML Attribute Name | NameFormat |
|------|----------|---------------------|------------|
| EmailAddress | email | EmailAddress | Basic |
| FirstName | firstName | FirstName | Basic |
| LastName | lastName | LastName | Basic |

### Create Test User

1. Go to **Users** → **Add user**
2. Fill in username, email, first name, last name
3. Go to **Credentials** tab → Set password (disable Temporary)

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
│   ├── sp.crt                    # SP certificate
│   └── idp-metadata.xml          # Keycloak IdP metadata
└── templates/
    ├── home.html                 # Landing page template
    └── user.html                 # User attributes template
```

## Endpoints

| Endpoint | Description | Auth Required |
|----------|-------------|---------------|
| `/` or `/home` | Landing page with login/logout links | No |
| `/user` | Display authenticated user's SAML attributes | Yes |
| `/saml2/authenticate/keycloak` | Initiate SAML login | No |
| `/logout` | Logout and end session | Yes |
| `/saml/metadata/keycloak` | SP metadata XML | No |

## SAML Attributes

After successful authentication, the following attributes are available (if configured in Keycloak):

- `EmailAddress` - User's email
- `FirstName` - User's first name
- `LastName` - User's last name

## Configuration

The SAML2 configuration is in `src/main/resources/application.yml`:

```yaml
spring:
  security:
    saml2:
      relyingparty:
        registration:
          keycloak:
            entity-id: spring-sp-demo
            assertingparty:
              metadata-uri: classpath:credentials/idp-metadata.xml
            signing:
              credentials:
                - private-key-location: classpath:credentials/sp.key
                  certificate-location: classpath:credentials/sp.crt
            decryption:
              credentials:
                - private-key-location: classpath:credentials/sp.key
                  certificate-location: classpath:credentials/sp.crt
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

After regenerating, update the certificate in Keycloak:
1. Go to **Clients** → **spring-sp-demo** → **Keys**
2. Import the new `sp.crt` certificate
3. Currently, both key and crt is encrypted by sop age, so if this is production code,
the sp.key and sp.crt (excluded fro security) should be decrypted from sp.key.enc and sp.crt.enc before use.
But since this is a demo project, I skip that step here for simplicity.

## Updating IdP Metadata

If Keycloak configuration changes, refresh the IdP metadata:

```bash
curl http://localhost:8081/realms/SAML-realm/protocol/saml/descriptor \
  -o src/main/resources/credentials/idp-metadata.xml
```

## Running Tests

```bash
mvn test
```

Run a specific test class:
```bash
mvn test -Dtest=HomeControllerTest
```

## Troubleshooting

### "Invalid requester" error from Keycloak
- Ensure the SP certificate in Keycloak matches `sp.crt`
- Check that `spring-sp-demo` client exists in SAML-realm

### SAML Response validation fails
- Ensure system clock is synchronized (SAML assertions are time-sensitive)
- Check that SP certificate matches what's registered with the IdP
- Enable debug logging: `logging.level.org.springframework.security: DEBUG`

### Attributes not showing on user page
- Add SAML attribute mappers in Keycloak (see "Add Attribute Mappers" section)
- Ensure the test user has email, firstName, lastName filled in

### Keycloak not starting
```bash
docker-compose logs keycloak
```

## License

This is a demonstration project for educational purposes.
