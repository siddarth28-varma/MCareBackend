package com.medicare.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.medicare.dto.AuthResponse;
import com.medicare.dto.LoginRequest;
import com.medicare.dto.RegisterRequest;
import com.medicare.model.Role;
import com.medicare.model.User;
import com.medicare.repository.PatientRepository;
import com.medicare.repository.UserRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {
    @Mock private UserRepository users;
    @Mock private PatientRepository patients;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtEncoder jwtEncoder;

    private AuthController controller;

    @BeforeEach
    void setUp() {
        controller = new AuthController(users, patients, passwordEncoder, authenticationManager, jwtEncoder);
    }

    @Test
    void registerStoresAnEncodedPasswordAndReturnsOnlyPublicUserFields() {
        RegisterRequest request = new RegisterRequest("Asha Kumar", "asha@example.com", "a-long-demo-password", "123", "2000-01-01", "Female");
        when(users.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("$2a$12$encoded");
        when(users.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = controller.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(users).save(userCaptor.capture());
        assertEquals("$2a$12$encoded", userCaptor.getValue().getPassword());
        assertEquals(request.email(), response.email());
        assertEquals(Role.PATIENT, response.role());
    }

    @Test
    void loginReturnsASignedTokenAndUserWithoutThePassword() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(user.getName()).thenReturn("Asha Kumar");
        when(user.getEmail()).thenReturn("asha@example.com");
        when(user.getRole()).thenReturn(Role.PATIENT);
        when(users.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        Instant now = Instant.now();
        Jwt jwt = Jwt.withTokenValue("signed.jwt.token")
                .header("alg", "HS256")
                .subject("1")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .build();
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);

        AuthResponse response = controller.login(new LoginRequest(user.getEmail(), "a-long-demo-password"));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        assertEquals("signed.jwt.token", response.token());
        assertEquals(user.getEmail(), response.user().email());
        assertEquals(Role.PATIENT, response.user().role());
        assertNotNull(response.user());
    }
}
