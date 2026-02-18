package com.example.saml_demo.controller;

import com.example.saml_demo.dto.CreateUserRequest;
import com.example.saml_demo.dto.UserResponse;
import com.example.saml_demo.service.KeycloakUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserManagementController {

    private static final Logger logger = LoggerFactory.getLogger(UserManagementController.class);

    @Autowired
    private KeycloakUserService keycloakUserService;

    /**
     * Create a new user in Keycloak
     */
    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequest request, BindingResult bindingResult) {
        logger.info("Creating user request for username: {}", request.getUsername());

        // First, test Keycloak connection
        try {
            keycloakUserService.testConnection();
            logger.info("Keycloak connection test successful");
        } catch (Exception e) {
            logger.error("Keycloak connection failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new UserResponse("Keycloak service is unavailable: " + e.getMessage()));
        }

        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining(", "));
            logger.warn("Validation errors for user creation: {}", errors);
            return ResponseEntity.badRequest().body(new UserResponse("Validation errors: " + errors));
        }

        // Validate required fields
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new UserResponse("Username is required"));
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new UserResponse("Password is required"));
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new UserResponse("Email is required"));
        }

        UserResponse response = keycloakUserService.createUser(request);

        if (response.getMessage() != null && response.getMessage().contains("Error")) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

        if (response.getMessage() != null && response.getMessage().contains("already exists")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        if (response.getUsername() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all users from the realm
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        logger.info("Retrieving all users");
        List<UserResponse> users = keycloakUserService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Get a specific user by username
     */
    @GetMapping("/{username}")
    public ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username) {
        logger.info("Retrieving user: {}", username);
        UserResponse response = keycloakUserService.getUserByUsername(username);

        if (response.getMessage() != null && response.getMessage().contains("not found")) {
            return ResponseEntity.notFound().build();
        }

        if (response.getMessage() != null && response.getMessage().contains("Error")) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Delete a user by username
     */
    @DeleteMapping("/{username}")
    public ResponseEntity<UserResponse> deleteUser(@PathVariable String username) {
        logger.info("Deleting user: {}", username);
        UserResponse response = keycloakUserService.deleteUser(username);

        if (response.getMessage() != null && response.getMessage().contains("not found")) {
            return ResponseEntity.notFound().build();
        }

        if (response.getMessage() != null && response.getMessage().contains("Error")) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("User Management API is running");
    }

    /**
     * Test Keycloak connection
     */
    @GetMapping("/test-connection")
    public ResponseEntity<String> testConnection() {
        try {
            String serverInfo = keycloakUserService.testConnection();
            return ResponseEntity.ok("Keycloak connection successful: " + serverInfo);
        } catch (Exception e) {
            logger.error("Keycloak connection test failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Keycloak connection failed: " + e.getMessage());
        }
    }
}
