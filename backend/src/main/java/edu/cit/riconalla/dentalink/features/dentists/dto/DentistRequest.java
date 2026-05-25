package edu.cit.riconalla.dentalink.features.dentists.dto;

import jakarta.validation.constraints.NotBlank;

public class DentistRequest {

    @NotBlank(message = "Dentist name is required")
    private String name;

    private String specialization;
    private String status;

    public String getName()                     { return name; }
    public void setName(String name)            { this.name = name; }

    public String getSpecialization()                           { return specialization; }
    public void setSpecialization(String specialization)        { this.specialization = specialization; }

    public String getStatus()                   { return status; }
    public void setStatus(String status)        { this.status = status; }
}