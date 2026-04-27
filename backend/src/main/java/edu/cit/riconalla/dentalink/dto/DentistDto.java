package edu.cit.riconalla.dentalink.dto;

import edu.cit.riconalla.dentalink.entity.Dentist;

public class DentistDto {

    private Long id;
    private String name;
    private String specialization;
    private String status;

    public DentistDto(Long id, String name, String specialization, String status) {
        this.id = id;
        this.name = name;
        this.specialization = specialization;
        this.status = status;
    }

    /** Maps from Dentist entity to DentistDto. */
    public static DentistDto from(Dentist dentist) {
        return new DentistDto(
                dentist.getDentistId(),
                dentist.getDentistName(),
                dentist.getDentistSpecialization(),
                dentist.getDentistStatus()
        );
    }

    public Long getId()                 { return id; }
    public String getName()             { return name; }
    public String getSpecialization()   { return specialization; }
    public String getStatus()           { return status; }
}