package com.medicare.config;

import com.medicare.model.User;
import com.medicare.repository.UserRepository;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordMigration {
    private static final String BCRYPT_PATTERN = "^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$";
    private static final Map<String, String> DEMO_CREDENTIALS = Map.of(
            "admin@medicare.com", "admin123",
            "patient@medicare.com", "patient123",
            "doctor@medicare.com", "doctor123",
            "kabir@medicare.com", "doctor123",
            "nisha@medicare.com", "doctor123",
            "rohan@medicare.com", "doctor123");
    private static final SecureRandom RANDOM = new SecureRandom();

    @Bean
    ApplicationRunner migratePlaintextPasswords(
            UserRepository users,
            PasswordEncoder passwordEncoder,
            @Value("${app.demo-data.enabled:false}") boolean demoDataEnabled) {
        return args -> migrate(users, passwordEncoder, demoDataEnabled);
    }

    void migrate(UserRepository users, PasswordEncoder passwordEncoder, boolean demoDataEnabled) {
        List<User> plaintextUsers = users.findAll().stream()
                .filter(user -> !user.getPassword().matches(BCRYPT_PATTERN)
                        || (!demoDataEnabled && matchesKnownDemoPassword(user, passwordEncoder)))
                .toList();
        plaintextUsers.forEach(user -> {
            String knownDemoPassword = demoDataEnabled ? null : DEMO_CREDENTIALS.get(user.getEmail());
            boolean isDemoAccount = knownDemoPassword != null
                    && (user.getPassword().equals(knownDemoPassword) || passwordEncoder.matches(knownDemoPassword, user.getPassword()));
            String password = isDemoAccount ? randomPassword() : user.getPassword();
            user.setPassword(passwordEncoder.encode(password));
        });
        if (!plaintextUsers.isEmpty()) users.saveAll(plaintextUsers);
    }

    private boolean matchesKnownDemoPassword(User user, PasswordEncoder passwordEncoder) {
        String knownPassword = DEMO_CREDENTIALS.get(user.getEmail());
        return knownPassword != null
                && (user.getPassword().equals(knownPassword) || passwordEncoder.matches(knownPassword, user.getPassword()));
    }

    private String randomPassword() {
        byte[] secret = new byte[32];
        RANDOM.nextBytes(secret);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(secret);
    }
}
