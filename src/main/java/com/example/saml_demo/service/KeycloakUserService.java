package com.example.saml_demo.service;

import com.example.saml_demo.dto.CreateUserRequest;
import com.example.saml_demo.dto.UserResponse;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class KeycloakUserService {

    private static final Logger logger = LoggerFactory.getLogger(KeycloakUserService.class);

    @Autowired
    private Keycloak keycloakAdminClient;

    @Value("${keycloak.target-realm}")
    private String targetRealm;

    public UserResponse createUser(CreateUserRequest request) {
        logger.info("Attempting to create user: {}", request.getUsername());

        try {
            // Validate input parameters
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                logger.error("Username is null or empty");
                return new UserResponse("Username cannot be null or empty");
            }

            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                logger.error("Password is null or empty");
                return new UserResponse("Password cannot be null or empty");
            }

            // Test Keycloak connection first
            logger.info("Testing Keycloak connection...");
            keycloakAdminClient.serverInfo();
            logger.info("Keycloak connection successful");

            RealmResource realmResource = keycloakAdminClient.realm(targetRealm);
            logger.info("Successfully connected to realm: {}", targetRealm);

            UsersResource usersResource = realmResource.users();

            // Check if user already exists
            logger.info("Checking if user {} already exists", request.getUsername());
            List<UserRepresentation> existingUsers = usersResource.search(request.getUsername(), true);
            if (!existingUsers.isEmpty()) {
                logger.warn("User with username {} already exists", request.getUsername());
                return new UserResponse("User with username " + request.getUsername() + " already exists");
            }

            // Create user representation
            UserRepresentation user = new UserRepresentation();
            user.setUsername(request.getUsername().trim());
            user.setEmail(request.getEmail() != null ? request.getEmail().trim() : null);
            user.setFirstName(request.getFirstName() != null ? request.getFirstName().trim() : "");
            user.setLastName(request.getLastName() != null ? request.getLastName().trim() : "");
            user.setEnabled(request.isEnabled());
            user.setEmailVerified(request.getEmail() != null && !request.getEmail().trim().isEmpty());

            logger.info("Creating user representation for: {}", request.getUsername());

            // Create user
            Response response = usersResource.create(user);
            logger.info("Create user response status: {} - {}", response.getStatus(), response.getStatusInfo().getReasonPhrase());

            if (response.getStatus() == 201) {
                // Get the created user ID from location header
                String location = response.getLocation() != null ? response.getLocation().toString() : "";
                if (location.isEmpty()) {
                    logger.error("No location header in response");
                    return new UserResponse("User created but could not retrieve user ID");
                }

                String userId = extractUserIdFromLocationHeader(location);
                logger.info("User created with ID: {}", userId);

                // Set password
                CredentialRepresentation credential = new CredentialRepresentation();
                credential.setType(CredentialRepresentation.PASSWORD);
                credential.setValue(request.getPassword());
                credential.setTemporary(request.isTemporary());

                logger.info("Setting password for user: {}", userId);
                usersResource.get(userId).resetPassword(credential);

                // Assign default role
                assignDefaultRole(realmResource, userId);

                logger.info("User {} created successfully with ID: {}", request.getUsername(), userId);

                return new UserResponse(userId, request.getUsername(), request.getEmail(),
                                      request.getFirstName(), request.getLastName(), request.isEnabled());
            } else {
                String errorBody = "";
                try {
                    if (response.hasEntity()) {
                        errorBody = response.readEntity(String.class);
                    }
                } catch (Exception e) {
                    logger.warn("Could not read error response body", e);
                }

                logger.error("Failed to create user {}, response status: {} - {}, body: {}",
                           request.getUsername(), response.getStatus(), response.getStatusInfo().getReasonPhrase(), errorBody);
                return new UserResponse("Failed to create user: " + response.getStatusInfo().getReasonPhrase() +
                                      (errorBody.isEmpty() ? "" : " - " + errorBody));
            }

        } catch (Exception e) {
            logger.error("Error creating user {}: {} - {}", request.getUsername(), e.getClass().getSimpleName(), e.getMessage(), e);
            return new UserResponse("Error creating user: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    public List<UserResponse> getAllUsers() {
        try {
            RealmResource realmResource = keycloakAdminClient.realm(targetRealm);
            UsersResource usersResource = realmResource.users();

            List<UserRepresentation> users = usersResource.list();

            return users.stream()
                    .map(user -> new UserResponse(
                            user.getId(),
                            user.getUsername(),
                            user.getEmail(),
                            user.getFirstName(),
                            user.getLastName(),
                            user.isEnabled()
                    ))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("Error retrieving users: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public UserResponse getUserByUsername(String username) {
        try {
            RealmResource realmResource = keycloakAdminClient.realm(targetRealm);
            UsersResource usersResource = realmResource.users();

            List<UserRepresentation> users = usersResource.search(username, true);

            if (users.isEmpty()) {
                return new UserResponse("User not found: " + username);
            }

            UserRepresentation user = users.get(0);
            return new UserResponse(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.isEnabled()
            );

        } catch (Exception e) {
            logger.error("Error retrieving user {}: {}", username, e.getMessage(), e);
            return new UserResponse("Error retrieving user: " + e.getMessage());
        }
    }

    public UserResponse deleteUser(String username) {
        try {
            RealmResource realmResource = keycloakAdminClient.realm(targetRealm);
            UsersResource usersResource = realmResource.users();

            List<UserRepresentation> users = usersResource.search(username, true);

            if (users.isEmpty()) {
                return new UserResponse("User not found: " + username);
            }

            String userId = users.get(0).getId();
            usersResource.delete(userId);

            logger.info("User {} deleted successfully", username);
            return new UserResponse("User " + username + " deleted successfully");

        } catch (Exception e) {
            logger.error("Error deleting user {}: {}", username, e.getMessage(), e);
            return new UserResponse("Error deleting user: " + e.getMessage());
        }
    }

    private String extractUserIdFromLocationHeader(String location) {
        String[] parts = location.split("/");
        return parts[parts.length - 1];
    }

    public String testConnection() {
        try {
            logger.info("Testing Keycloak connection...");
            var serverInfo = keycloakAdminClient.serverInfo().getInfo();
            logger.info("Keycloak server info: {}", serverInfo);

            // Test realm access
            RealmResource realmResource = keycloakAdminClient.realm(targetRealm);
            var realmInfo = realmResource.toRepresentation();
            logger.info("Successfully connected to realm: {} - {}", realmInfo.getRealm(), realmInfo.getDisplayName());

            return String.format("Server: %s, Realm: %s (%s)",
                                serverInfo.getSystemInfo().getVersion(),
                                realmInfo.getRealm(),
                                realmInfo.getDisplayName());
        } catch (Exception e) {
            logger.error("Keycloak connection test failed: {}", e.getMessage(), e);
            throw new RuntimeException("Connection test failed: " + e.getMessage(), e);
        }
    }

    private void assignDefaultRole(RealmResource realmResource, String userId) {
        try {
            // Assign the "user" role to the created user
            var roleRepresentation = realmResource.roles().get("user").toRepresentation();
            realmResource.users().get(userId).roles().realmLevel().add(Collections.singletonList(roleRepresentation));
        } catch (Exception e) {
            logger.warn("Could not assign default role to user: {}", e.getMessage());
        }
    }
}
