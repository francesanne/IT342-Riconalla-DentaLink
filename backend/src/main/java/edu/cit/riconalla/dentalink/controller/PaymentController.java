package edu.cit.riconalla.dentalink.controller;

import edu.cit.riconalla.dentalink.dto.ApiResponse;
import edu.cit.riconalla.dentalink.dto.CreateIntentRequest;
import edu.cit.riconalla.dentalink.dto.CreateIntentResponse;
import edu.cit.riconalla.dentalink.dto.PaymentDto;
import edu.cit.riconalla.dentalink.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * POST /api/v1/payments/create-intent — PATIENT only — SDD §5.3
     * Creates a PayMongo checkout session for the given appointment.
     * Returns checkoutUrl and paymentIntentId.
     */
    @PostMapping("/create-intent")
    @PreAuthorize("hasAuthority('PATIENT')")
    public ResponseEntity<ApiResponse<CreateIntentResponse>> createIntent(
            @RequestBody CreateIntentRequest request,
            Authentication auth
    ) {
        CreateIntentResponse response = paymentService.createPaymentIntent(
                auth.getName(),
                request.getAppointmentId()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * POST /api/v1/payments/webhook — Public (no JWT) — SDD §5.3
     * Receives PayMongo webhook events.
     * Signature is verified inside PaymentService before any state mutation.
     * Must always return 200 — PayMongo retries non-200 responses.
     */
    @PostMapping("/webhook")
    public ResponseEntity<ApiResponse<String>> webhook(
            @RequestBody String rawBody,
            @RequestHeader(value = "paymongo-signature", required = false) String signatureHeader
    ) {
        paymentService.handleWebhook(rawBody, signatureHeader);
        return ResponseEntity.ok(ApiResponse.success("Webhook received"));
    }

    /**
     * GET /api/v1/payments — ADMIN only — SDD §5.3
     * Returns all payment records for admin monitoring.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<List<PaymentDto>>> getAllPayments() {
        List<PaymentDto> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(ApiResponse.success(payments));
    }
}