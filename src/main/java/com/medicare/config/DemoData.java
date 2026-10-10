package com.medicare.config;

import com.medicare.model.*;
import com.medicare.repository.DoctorRepository;
import com.medicare.repository.PatientRepository;
import com.medicare.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalTime;

@Configuration
@ConditionalOnProperty(name = "app.demo-data.enabled", havingValue = "true")
public class DemoData {
    @Bean
    CommandLineRunner seedDemoData(UserRepository users, PatientRepository patients, DoctorRepository doctors, PasswordEncoder passwordEncoder) {
        return args -> {
            if (users.existsByEmail("admin@medicare.com")) return;
            users.save(new User("MediCare Admin", "admin@medicare.com", passwordEncoder.encode("admin123"), Role.ADMIN));
            User patientUser = users.save(new User("Aarav Sharma", "patient@medicare.com", passwordEncoder.encode("patient123"), Role.PATIENT));
            patients.save(new Patient(patientUser, "9876543210", "2002-06-15", "Male"));
            addDoctor(users, doctors, passwordEncoder, "Dr. Ananya Rao", "doctor@medicare.com", "doctor123", "Cardiology", "MBBS, MD", "9876543211");
            addDoctor(users, doctors, passwordEncoder, "Dr. Kabir Mehta", "kabir@medicare.com", "doctor123", "Dermatology", "MBBS, MD", "9876543212");
            addDoctor(users, doctors, passwordEncoder, "Dr. Nisha Iyer", "nisha@medicare.com", "doctor123", "Pediatrics", "MBBS, DCH", "9876543213");
            addDoctor(users, doctors, passwordEncoder, "Dr. Rohan Das", "rohan@medicare.com", "doctor123", "General Medicine", "MBBS, MD", "9876543214");
        };
    }

    private void addDoctor(UserRepository users, DoctorRepository doctors, PasswordEncoder passwordEncoder, String name, String email, String password, String specialization, String qualification, String phone) {
        User user = users.save(new User(name, email, passwordEncoder.encode(password), Role.DOCTOR));
        doctors.save(new Doctor(user, specialization, qualification, phone, "MON,TUE,WED,THU,FRI", LocalTime.of(9, 0), LocalTime.of(17, 0)));
    }
}
