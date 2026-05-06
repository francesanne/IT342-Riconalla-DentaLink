package edu.cit.riconalla.dentalink.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", unique = true, nullable = false)
    private Appointment appointment;

    @Column(name = "paymongo_payment_id")
    private String paymongoPaymentId;

    @Column(name = "paymongo_intent_id")
    private String paymongoIntentId;

    @Column(name = "payment_amount")
    private BigDecimal paymentAmount;

    @Column(name = "payment_status")
    private String paymentStatus;

    @Column(name = "payment_created_at")
    private LocalDateTime paymentCreatedAt;
}