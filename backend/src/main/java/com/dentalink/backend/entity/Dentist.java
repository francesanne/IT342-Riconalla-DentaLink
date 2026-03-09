package com.dentalink.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "dentist")
@Getter
@Setter
public class Dentist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dentistId;

    private String dentistName;

    private String dentistSpecialization;

    private String dentistStatus;
}