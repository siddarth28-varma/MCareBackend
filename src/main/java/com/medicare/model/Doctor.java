package com.medicare.model;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "doctors")
public class Doctor {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", unique = true)
    private User user;
    private String specialization;
    private String qualification;
    private String phone;
    private String availableDays;
    private LocalTime startTime;
    private LocalTime endTime;

    protected Doctor() { }

    public Doctor(User user, String specialization, String qualification, String phone, String availableDays, LocalTime startTime, LocalTime endTime) {
        this.user = user;
        this.specialization = specialization;
        this.qualification = qualification;
        this.phone = phone;
        this.availableDays = availableDays;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAvailableDays() { return availableDays; }
    public void setAvailableDays(String availableDays) { this.availableDays = availableDays; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
}
