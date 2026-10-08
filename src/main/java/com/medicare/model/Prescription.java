package com.medicare.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "prescriptions")
public class Prescription {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(optional = false) @JoinColumn(name = "appointment_id", unique = true)
    private Appointment appointment;
    @Column(length = 2000)
    private String consultationNotes;
    @Column(length = 2000)
    private String prescriptionDetails;
    private LocalDateTime updatedAt;

    protected Prescription() { }

    public Prescription(Appointment appointment, String consultationNotes, String prescriptionDetails) {
        this.appointment = appointment;
        this.consultationNotes = consultationNotes;
        this.prescriptionDetails = prescriptionDetails;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Appointment getAppointment() { return appointment; }
    public String getConsultationNotes() { return consultationNotes; }
    public void setConsultationNotes(String consultationNotes) { this.consultationNotes = consultationNotes; }
    public String getPrescriptionDetails() { return prescriptionDetails; }
    public void setPrescriptionDetails(String prescriptionDetails) { this.prescriptionDetails = prescriptionDetails; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
