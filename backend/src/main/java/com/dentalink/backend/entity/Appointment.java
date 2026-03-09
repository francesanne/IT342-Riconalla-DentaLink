package com.dentalink.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "appointment",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"dentistId", "appointmentDatetime"}
                )
        }
)
@Getter
@Setter
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long appointmentId;

    private Long patientId;

    private Long serviceId;

    private Long dentistId;

    private LocalDateTime appointmentDatetime;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus appointmentStatus;
}