package edu.cit.riconalla.dentalink.dto;

public class CreateIntentResponse {

    private String checkoutUrl;
    private String paymentIntentId;

    public CreateIntentResponse(String checkoutUrl, String paymentIntentId) {
        this.checkoutUrl = checkoutUrl;
        this.paymentIntentId = paymentIntentId;
    }

    public String getCheckoutUrl()      { return checkoutUrl; }
    public String getPaymentIntentId()  { return paymentIntentId; }
}