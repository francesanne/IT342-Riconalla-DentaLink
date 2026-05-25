package edu.cit.riconalla.dentalink.features.appointments.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateAppointmentRequest {

    @NotNull(message = "serviceId is required")
    private Long serviceId;

    @NotNull(message = "dentistId is required")
    private Long dentistId;

    @NotBlank(message = "appointmentDatetime is required")
    private String appointmentDatetime;

    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }

    public Long getDentistId() { return dentistId; }
    public void setDentistId(Long dentistId) { this.dentistId = dentistId; }

    public String getAppointmentDatetime() { return appointmentDatetime; }
    public void setAppointmentDatetime(String appointmentDatetime) { this.appointmentDatetime = appointmentDatetime; }
}