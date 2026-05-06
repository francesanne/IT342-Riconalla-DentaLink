package edu.cit.riconalla.dentalink.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cit.riconalla.dentalink.dto.CreateIntentResponse;
import edu.cit.riconalla.dentalink.dto.PaymentDto;
import edu.cit.riconalla.dentalink.entity.*;
import edu.cit.riconalla.dentalink.exception.ResourceNotFoundException;
import edu.cit.riconalla.dentalink.repository.AppointmentRepository;
import edu.cit.riconalla.dentalink.repository.DentistRepository;
import edu.cit.riconalla.dentalink.repository.PaymentRepository;
import edu.cit.riconalla.dentalink.repository.ServiceRepository;
import edu.cit.riconalla.dentalink.repository.UserRepository;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private static final String PAYMONGO_API_BASE = "https://api.paymongo.com/v1";

    private final AppointmentRepository appointmentRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final DentistRepository dentistRepository;
    private final EmailService emailService;

    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${paymongo.secret-key}")
    private String paymongoSecretKey;

    @Value("${paymongo.webhook-secret}")
    private String paymongoWebhookSecret;

    @Value("${paymongo.success-url}")
    private String successUrl;

    @Value("${paymongo.cancel-url}")
    private String cancelUrl;

    public PaymentService(AppointmentRepository appointmentRepository,
                          PaymentRepository paymentRepository,
                          UserRepository userRepository,
                          ServiceRepository serviceRepository,
                          DentistRepository dentistRepository,
                          EmailService emailService) {
        this.appointmentRepository = appointmentRepository;
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;
        this.dentistRepository = dentistRepository;
        this.emailService = emailService;
    }

    // ============================================================
    // CREATE PAYMENT INTENT — SDD §5.3 POST /payments/create-intent
    // ============================================================

    /**
     * Creates a PayMongo Checkout Session for the given appointment.
     * Returns the hosted checkout URL the patient must visit to pay.
     *
     * Validation order (per execution plan):
     *   1. Caller exists           -> 404
     *   2. Appointment exists      -> 404
     *   3. Caller owns appointment -> 403
     *   4. payment_status UNPAID   -> 400
     *   5. status PENDING_PAYMENT  -> 400
     *   6. PayMongo call succeeds  -> 500 if external failure
     */
    @Transactional
    public CreateIntentResponse createPaymentIntent(String callerEmail, Long appointmentId) {

        // 1. Caller exists
        User caller = userRepository.findByEmail(callerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 2. Appointment exists
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        // 3. Caller owns appointment
        if (!appointment.getPatientId().equals(caller.getUserId())) {
            throw new IllegalArgumentException("Forbidden: appointment does not belong to caller");
        }

        // 4. payment_status must be UNPAID
        if (appointment.getPaymentStatus() != PaymentStatus.UNPAID) {
            throw new IllegalArgumentException("Appointment already paid");
        }

        // 5. status must be PENDING_PAYMENT
        if (appointment.getAppointmentStatus() != AppointmentStatus.PENDING_PAYMENT) {
            throw new IllegalArgumentException("Invalid appointment status for payment");
        }

        // Resolve service (for amount + name)
        edu.cit.riconalla.dentalink.entity.Service service =
                serviceRepository.findById(appointment.getServiceId())
                        .orElseThrow(() -> new ResourceNotFoundException("Service not found"));

        // PayMongo expects amount in centavos (smallest currency unit)
        long amountInCentavos = service.getServicePrice()
                .multiply(BigDecimal.valueOf(100))
                .longValueExact();

        // 6. Call PayMongo to create checkout session
        String checkoutUrl;
        String paymentIntentId;
        try {
            JsonNode result = createPaymongoCheckoutSession(
                    amountInCentavos,
                    service.getServiceName(),
                    appointmentId
            );
            checkoutUrl = result.path("data").path("attributes").path("checkout_url").asText();
            paymentIntentId = result.path("data").path("id").asText();
        } catch (Exception e) {
            throw new RuntimeException("PayMongo checkout session creation failed: " + e.getMessage());
        }

        // Persist the intent ID on the appointment for webhook correlation
        appointment.setPaymongoIntentId(paymentIntentId);
        appointmentRepository.save(appointment);

        return new CreateIntentResponse(checkoutUrl, paymentIntentId);
    }

    /**
     * Calls PayMongo /checkout_sessions endpoint.
     * Auth: Basic <base64(secretKey:)>
     */
    private JsonNode createPaymongoCheckoutSession(long amountInCentavos, String serviceName, Long appointmentId)
            throws Exception {

        String authHeader = "Basic " + Base64.getEncoder()
                .encodeToString((paymongoSecretKey + ":").getBytes(StandardCharsets.UTF_8));

        // Build PayMongo Checkout Session request body
        String requestBody = String.format("""
                {
                  "data": {
                    "attributes": {
                      "send_email_receipt": false,
                      "show_description": true,
                      "show_line_items": true,
                      "line_items": [
                        {
                          "currency": "PHP",
                          "amount": %d,
                          "name": "%s",
                          "quantity": 1
                        }
                      ],
                      "payment_method_types": ["card", "gcash", "paymaya"],
                      "description": "DentaLink appointment #%d",
                      "reference_number": "appt_%d",
                      "success_url": "%s?appointmentId=%d",
                      "cancel_url": "%s?appointmentId=%d"
                    }
                  }
                }
                """,
                amountInCentavos,
                escapeJson(serviceName),
                appointmentId,
                appointmentId,
                successUrl, appointmentId,
                cancelUrl, appointmentId
        );

        RequestBody body = RequestBody.create(
                requestBody, MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(PAYMONGO_API_BASE + "/checkout_sessions")
                .header("Authorization", authHeader)
                .header("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                throw new RuntimeException("PayMongo error " + response.code() + ": " + responseBody);
            }
            return objectMapper.readTree(responseBody);
        }
    }

    // ============================================================
    // WEBHOOK HANDLER — SDD §5.3 POST /payments/webhook
    // ============================================================

    /**
     * Processes PayMongo webhook events.
     *
     * CRITICAL: this is the ONLY place appointment_status -> CONFIRMED
     * is allowed (locked invariant #1).
     *
     * Returns silently (200) for invalid signatures, non-paid events,
     * unknown appointments, and already-paid appointments. PayMongo retries
     * non-200 responses indefinitely, so we never throw here.
     */
    @Transactional
    public void handleWebhook(String rawBody, String signatureHeader) {

        // 1. Verify signature — silent return if invalid
        if (!verifyWebhookSignature(rawBody, signatureHeader)) {
            return;
        }

        // 2. Parse event
        JsonNode event;
        String eventType;
        try {
            event = objectMapper.readTree(rawBody);
            eventType = event.path("data").path("attributes").path("type").asText();
        } catch (Exception e) {
            return; // malformed body — silent ack
        }

        // 3. Only handle checkout_session.payment.paid
        // We ignore payment.paid to avoid race condition — both events fire simultaneously.
        // checkout_session.payment.paid is the authoritative event for checkout-session-based flows.
        if (!"checkout_session.payment.paid".equals(eventType)) {
            return;
        }

        // 4. Extract fields from checkout_session.payment.paid payload
        // Payload structure: data.attributes.data = checkout session object
        //   data.attributes.data.id                              = cs_... (checkout session ID)
        //   data.attributes.data.attributes.payments[0].id      = pay_... (actual payment ID) — C-6
        //   data.attributes.data.attributes.payments[0].attributes.amount = centavos
        JsonNode dataAttrs = event.path("data").path("attributes").path("data").path("attributes");
        if (dataAttrs.isMissingNode()) {
            return;
        }

        // Extract actual payment ID (pay_...) from first payment in payments array — C-6
        JsonNode firstPayment = dataAttrs.path("payments").path(0);
        String paymongoPaymentId = firstPayment.path("id").asText(null);
        if (paymongoPaymentId == null || paymongoPaymentId.isBlank()) {
            // Fallback: use checkout session id if payments array is absent
            paymongoPaymentId = event.path("data").path("attributes").path("data").path("id").asText();
        }

        // Extract amount from first payment object (centavos → pesos)
        long amountCentavos = firstPayment.path("attributes").path("amount").asLong();
        BigDecimal amountPesos = amountCentavos > 0
                ? BigDecimal.valueOf(amountCentavos).divide(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        // Checkout session ID — stored in appointment.paymongo_intent_id at create-intent time
        String checkoutSessionId = event.path("data").path("attributes").path("data").path("id").asText(null);

        if (checkoutSessionId == null || checkoutSessionId.isBlank()) {
            return; // can't correlate — silent ack
        }

        Optional<Appointment> appointmentOpt = appointmentRepository.findByPaymongoIntentId(checkoutSessionId);
        if (appointmentOpt.isEmpty()) {
            return; // unknown appointment — silent ack (still 200 to PayMongo)
        }

        Appointment appointment = appointmentOpt.get();

        // 5. Idempotency — silent ack if already paid
        if (appointment.getPaymentStatus() == PaymentStatus.PAID) {
            return;
        }

        // 6. Create or update Payment row
        Payment payment = paymentRepository.findByAppointmentAppointmentId(appointment.getAppointmentId())
                .orElseGet(Payment::new);

        payment.setAppointment(appointment);
        payment.setPaymongoPaymentId(paymongoPaymentId);
        payment.setPaymongoIntentId(checkoutSessionId);
        payment.setPaymentAmount(amountPesos);
        payment.setPaymentStatus("PAID");
        payment.setPaymentCreatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // 7. Update appointment — ONLY place CONFIRMED is set
        appointment.setPaymentStatus(PaymentStatus.PAID);
        appointment.setAppointmentStatus(AppointmentStatus.CONFIRMED);
        appointmentRepository.save(appointment);

        // 8. Send confirmation email — SDD §2.4 (mandatory, non-blocking)
        userRepository.findById(appointment.getPatientId()).ifPresent(patient -> {
            String serviceName = serviceRepository.findById(appointment.getServiceId())
                    .map(s -> s.getServiceName()).orElse("Dental Service");
            String dentistName = dentistRepository.findById(appointment.getDentistId())
                    .map(d -> d.getDentistName()).orElse("Your dentist");

            emailService.sendConfirmationEmail(
                    patient.getEmail(),
                    patient.getFirstName(),
                    serviceName,
                    dentistName,
                    appointment.getAppointmentDatetime()
            );
        });
    }

    /**
     * Verifies PayMongo webhook signature.
     * Header format: "t=<timestamp>,te=<sig>,li=<sig>"
     * Signed payload: "<timestamp>.<rawBody>" using HMAC-SHA256 with webhook secret.
     */
    private boolean verifyWebhookSignature(String rawBody, String signatureHeader) {
        if (signatureHeader == null || signatureHeader.isBlank()
                || paymongoWebhookSecret == null || paymongoWebhookSecret.isBlank()) {
            return false;
        }

        try {
            String timestamp = null;
            String testSignature = null;
            String liveSignature = null;

            for (String part : signatureHeader.split(",")) {
                String[] kv = part.split("=", 2);
                if (kv.length != 2) continue;
                switch (kv[0].trim()) {
                    case "t":  timestamp     = kv[1].trim(); break;
                    case "te": testSignature = kv[1].trim(); break;
                    case "li": liveSignature = kv[1].trim(); break;
                }
            }

            if (timestamp == null) return false;

            String signedPayload = timestamp + "." + rawBody;

            Mac hmac = Mac.getInstance("HmacSHA256");
            hmac.init(new SecretKeySpec(
                    paymongoWebhookSecret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"));
            byte[] computed = hmac.doFinal(signedPayload.getBytes(StandardCharsets.UTF_8));
            String computedHex = bytesToHex(computed);

            // Sandbox uses 'te' (test event); live uses 'li'
            return computedHex.equalsIgnoreCase(testSignature)
                    || computedHex.equalsIgnoreCase(liveSignature);
        } catch (Exception e) {
            return false;
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    // ============================================================
    // GET ALL PAYMENTS — SDD §5.3 GET /payments (ADMIN)
    // ============================================================

    public List<PaymentDto> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(payment -> {
                    User patient = userRepository.findById(
                            payment.getAppointment().getPatientId()
                    ).orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
                    return PaymentDto.from(payment, patient);
                })
                .collect(Collectors.toList());
    }
}