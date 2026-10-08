package com.medicare.model;

import jakarta.persistence.*;

@Entity
@Table(name = "patients")
public class Patient {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", unique = true)
    private User user;
    private String phone;
    private String dateOfBirth;
    private String gender;

    protected Patient() { }

    public Patient(User user, String phone, String dateOfBirth, String gender) {
        this.user = user;
        this.phone = phone;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
}
