package edu.cit.riconalla.dentalink.dto;

import edu.cit.riconalla.dentalink.entity.Payment;
import edu.cit.riconalla.dentalink.entity.User;

import java.math.BigDecimal;

public class PaymentDto {

    private Long id;
    private Long appointmentId;
    private String paymongoPaymentId;   // C-6: admin list field name
    private BigDecimal paymentAmount;
    private String paymentStatus;
    private String paymentCreatedAt;
    private PatientSummary patient;

    public PaymentDto(Long id, Long appointmentId, String paymongoPaymentId,
                      BigDecimal paymentAmount, String paymentStatus,
                      String paymentCreatedAt, PatientSummary patient) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.paymongoPaymentId = paymongoPaymentId;
        this.paymentAmount = paymentAmount;
        this.paymentStatus = paymentStatus;
        this.paymentCreatedAt = paymentCreatedAt;
        this.patient = patient;
    }

    /**
     * Maps Payment entity to PaymentDto.
     * Requires the patient User resolved by service layer from appointment.patientId.
     */
    public static PaymentDto from(Payment payment, User patient) {
        return new PaymentDto(
                payment.getPaymentId(),
                payment.getAppointment().getAppointmentId(),
                payment.getPaymongoPaymentId(),
                payment.getPaymentAmount(),
                payment.getPaymentStatus(),
                payment.getPaymentCreatedAt() != null
                        ? payment.getPaymentCreatedAt().toString()
                        : null,
                PatientSummary.from(patient)
        );
    }

    public Long getId()                     { return id; }
    public Long getAppointmentId()          { return appointmentId; }
    public String getPaymongoPaymentId()    { return paymongoPaymentId; }
    public BigDecimal getPaymentAmount()    { return paymentAmount; }
    public String getPaymentStatus()        { return paymentStatus; }
    public String getPaymentCreatedAt()     { return paymentCreatedAt; }
    public PatientSummary getPatient()      { return patient; }
}