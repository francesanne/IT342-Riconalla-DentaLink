package edu.cit.riconalla.dentalink.dto;

import edu.cit.riconalla.dentalink.entity.User;

public class PatientSummary {

    private Long id;
    private String firstName;
    private String lastName;

    public PatientSummary(Long id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public static PatientSummary from(User user) {
        return new PatientSummary(
                user.getUserId(),
                user.getFirstName(),
                user.getLastName()
        );
    }

    public Long getId()             { return id; }
    public String getFirstName()    { return firstName; }
    public String getLastName()     { return lastName; }
}