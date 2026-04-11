package edu.cit.riconalla.dentalink.dto;

import edu.cit.riconalla.dentalink.entity.AppointmentStatus;
import edu.cit.riconalla.dentalink.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public class AppointmentResponse {

    private Long id;
    private Long serviceId;
    private Long dentistId;

    private String serviceName;
    private BigDecimal servicePrice;
    private String serviceImageUrl;

    private String dentistName;
    private String dentistSpecialization;

    private Map<String, Object> patient;

    private LocalDateTime appointmentDatetime;
    private AppointmentStatus status;
    private PaymentStatus paymentStatus;

    // getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }

    public Long getDentistId() { return dentistId; }
    public void setDentistId(Long dentistId) { this.dentistId = dentistId; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public BigDecimal getServicePrice() { return servicePrice; }
    public void setServicePrice(BigDecimal servicePrice) { this.servicePrice = servicePrice; }

    public String getServiceImageUrl() { return serviceImageUrl; }
    public void setServiceImageUrl(String serviceImageUrl) { this.serviceImageUrl = serviceImageUrl; }

    public String getDentistName() { return dentistName; }
    public void setDentistName(String dentistName) { this.dentistName = dentistName; }

    public String getDentistSpecialization() { return dentistSpecialization; }
    public void setDentistSpecialization(String dentistSpecialization) { this.dentistSpecialization = dentistSpecialization; }

    public Map<String, Object> getPatient() { return patient; }
    public void setPatient(Map<String, Object> patient) { this.patient = patient; }

    public LocalDateTime getAppointmentDatetime() { return appointmentDatetime; }
    public void setAppointmentDatetime(LocalDateTime appointmentDatetime) { this.appointmentDatetime = appointmentDatetime; }

    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
}