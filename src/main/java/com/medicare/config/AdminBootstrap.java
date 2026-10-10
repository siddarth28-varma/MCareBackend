package com.medicare.config;

import com.medicare.model.Role;
import com.medicare.model.User;
import com.medicare.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminBootstrap {
    @Bean
    ApplicationRunner bootstrapAdmin(
            UserRepository users,
            PasswordEncoder passwordEncoder,
            @Value("${BOOTSTRAP_ADMIN_EMAIL:}") String email,
            @Value("${BOOTSTRAP_ADMIN_PASSWORD:}") String password,
            @Value("${BOOTSTRAP_ADMIN_NAME:MediCare Administrator}") String name,
            @Value("${app.demo-data.enabled:false}") boolean demoDataEnabled) {
        return args -> {
            if (email.isBlank() && password.isBlank()) {
                if (demoDataEnabled) return;
                throw new IllegalStateException("Set BOOTSTRAP_ADMIN_EMAIL and BOOTSTRAP_ADMIN_PASSWORD before starting without demo data.");
            }
            if (email.isBlank() || password.isBlank()) {
                throw new IllegalStateException("Set both BOOTSTRAP_ADMIN_EMAIL and BOOTSTRAP_ADMIN_PASSWORD, or leave both unset.");
            }
            if (password.length() < 12 || password.getBytes(StandardCharsets.UTF_8).length > 72) {
                throw new IllegalStateException("BOOTSTRAP_ADMIN_PASSWORD must be at least 12 characters and no more than 72 UTF-8 bytes.");
            }
            var existing = users.findByEmail(email);
            if (existing.isPresent()) {
                if (existing.get().getRole() != Role.ADMIN) {
                    throw new IllegalStateException("BOOTSTRAP_ADMIN_EMAIL is already used by a non-admin account.");
                }
                User admin = existing.get();
                boolean changed = false;
                if (!admin.getName().equals(name)) {
                    admin.setName(name);
                    changed = true;
                }
                if (!passwordEncoder.matches(password, admin.getPassword())) {
                    admin.setPassword(passwordEncoder.encode(password));
                    changed = true;
                }
                if (changed) users.save(admin);
                return;
            }
            users.save(new User(name, email, passwordEncoder.encode(password), Role.ADMIN));
        };
    }
}
