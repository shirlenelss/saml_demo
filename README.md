# SAML Demo with Keycloak

A Spring Boot application demonstrating SAML 2.0 authentication using Spring Security SAML2 Service Provider with Keycloak as the Identity Provider, including a User Management API.

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
   - Login with `testuser` / `password`

## Default Credentials

**Keycloak Admin Console** (http://localhost:8081):
- Username: `admin`
- Password: `admin`

**Test User**:
- Username: `testuser`
- Password: `password`
- Email: `testuser@example.com`

## Endpoints

| Endpoint | Description | Auth Required |
|----------|-------------|---------------|
| `/` or `/home` | Landing page with login/logout links | No |
| `/user` | Display authenticated user's SAML attributes | Yes |
| `/keycloak/attributes` | All SAML attributes as JSON | Yes |
| `/keycloak/roles` | Keycloak roles as JSON | Yes |
| `/keycloak/groups` | Keycloak groups as JSON | Yes |
| `/api/users` | User management API | No |
| `/api/users/health` | API health check | No |
| `/saml2/authenticate/keycloak` | Initiate SAML login | No |
| `/logout` | Logout and end session | Yes |

## User Management API

### Create User
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "password": "password123",
    "email": "newuser@example.com",
    "firstName": "New",
    "lastName": "User",
    "enabled": true,
    "temporary": false
  }'
```

### Get All Users
```bash
curl http://localhost:8080/api/users
```

### Get User by Username
```bash
curl http://localhost:8080/api/users/testuser
```

### Delete User
```bash
curl -X DELETE http://localhost:8080/api/users/newuser
```

### Test Keycloak Connection
```bash
curl http://localhost:8080/api/users/test-connection
```

## SAML Attributes

After successful authentication, the following attributes are available (configured in Keycloak):

- `EmailAddress` - User's email
- `FirstName` - User's first name
- `LastName` - User's last name
- `Role` - Realm roles
- `member` - Group membership

## Project Structure

```
src/main/java/com/example/saml_demo/
├── SamlDemoApplication.java      # Spring Boot entry point
├── config/
│   ├── SecurityConfig.java       # SAML2 security configuration
│   └── KeycloakAdminConfig.java  # Keycloak admin client config
├── controller/
│   ├── HomeController.java       # Public landing page
│   ├── UserController.java       # Protected user info endpoint
│   ├── KeycloakAttributesController.java  # SAML attributes JSON API
│   └── UserManagementController.java      # User CRUD API
├── dto/
│   ├── CreateUserRequest.java    # User creation request
│   └── UserResponse.java         # User response
└── service/
    └── KeycloakUserService.java  # Keycloak admin operations

src/main/resources/
├── application.yml               # SAML2 and Keycloak configuration
├── realm-import.json             # Keycloak realm auto-import
├── credentials/
│   ├── sp.key                    # SP private key
│   ├── sp.crt                    # SP certificate
│   └── idp-metadata.xml          # Keycloak IdP metadata (fallback)
└── templates/
    ├── home.html                 # Landing page template
    └── user.html                 # User attributes template
```

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
              metadata-uri: http://localhost:8081/realms/SAML-realm/protocol/saml/descriptor
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

```bash
openssl req -x509 -newkey rsa:2048 \
  -keyout src/main/resources/credentials/sp.key \
  -out src/main/resources/credentials/sp.crt \
  -days 365 -nodes \
  -subj "/CN=saml-demo-sp/O=Demo/C=SE"
```

After regenerating, upload the new certificate to Keycloak:
1. Go to **Clients** → **spring-sp-demo** → **Keys**
2. Import the new `sp.crt` certificate

Note: Both `sp.key` and `sp.crt` have encrypted counterparts (`sp.key.enc`, `sp.crt.enc`) managed with sops/age. For production use, decrypt these before running.

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
- Verify that `saml.server.signature` and `saml.assertion.signature` are `true` in the Keycloak client config
- Enable debug logging: `logging.level.org.springframework.security: DEBUG`

### Attributes not showing on user page
- Verify SAML attribute mappers exist in Keycloak under **Clients** → **spring-sp-demo**
- Ensure the test user has email, firstName, lastName filled in

### Keycloak not starting
```bash
docker-compose logs keycloak
```

To fully reset Keycloak (reimports realm):
```bash
docker-compose down -v
docker-compose up -d
```

## License

This is a demonstration project for educational purposes.
