package edu.cit.riconalla.dentalink.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "dentists")
@Getter
@Setter
public class Dentist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dentist_id")
    private Long dentistId;

    @Column(name = "dentist_name")
    private String dentistName;

    @Column(name = "dentist_specialization")
    private String dentistSpecialization;

    @Column(name = "dentist_status")
    private String dentistStatus;
}