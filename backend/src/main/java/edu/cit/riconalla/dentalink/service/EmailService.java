package edu.cit.riconalla.dentalink.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * EmailService — SDD §2.4
 * Sends two emails:
 *   1. Welcome email — on successful patient registration
 *   2. Appointment confirmation email — after PayMongo webhook confirms payment
 *
 * Uses JavaMailSender (SMTP). Console print is NOT a substitute (SDD §2.4).
 * All credentials stored in environment variables (SDD §3.2).
 */
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${dentalink.mail.from}")
    private String fromAddress;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // ──────────────────────────────────────────────────────────────────────
    // Welcome Email — triggered on successful patient registration (SDD §2.4)
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Sends a welcome email to a newly registered patient.
     *
     * @param toEmail   patient's email address
     * @param firstName patient's first name
     */
    public void sendWelcomeEmail(String toEmail, String firstName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject("Welcome to DentaLink!");
            helper.setText(buildWelcomeHtml(firstName), true);

            mailSender.send(message);
        } catch (MessagingException e) {
            // Log but do not throw — email failure must not break registration flow
            System.err.println("[EmailService] Failed to send welcome email to " + toEmail + ": " + e.getMessage());
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // Confirmation Email — triggered after PayMongo webhook (SDD §2.4)
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Sends an appointment confirmation email after payment is verified.
     *
     * @param toEmail             patient's email address
     * @param firstName           patient's first name
     * @param serviceName         name of the booked dental service
     * @param dentistName         name of the assigned dentist
     * @param appointmentDatetime appointment date and time
     */
    public void sendConfirmationEmail(String toEmail,
                                      String firstName,
                                      String serviceName,
                                      String dentistName,
                                      LocalDateTime appointmentDatetime) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject("Your DentaLink Appointment is Confirmed!");
            helper.setText(buildConfirmationHtml(firstName, serviceName, dentistName, appointmentDatetime), true);

            mailSender.send(message);
        } catch (MessagingException e) {
            // Log but do not throw — email failure must not break webhook processing
            System.err.println("[EmailService] Failed to send confirmation email to " + toEmail + ": " + e.getMessage());
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // HTML Templates — developer-defined per C-8 (content is out-of-scope)
    // ──────────────────────────────────────────────────────────────────────

    private String buildWelcomeHtml(String firstName) {
        return """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;">
                  <div style="text-align: center; margin-bottom: 30px;">
                    <h1 style="color: #6ec1c3;">🦷 DentaLink</h1>
                  </div>
                  <h2>Welcome, %s!</h2>
                  <p>Thank you for registering with <strong>DentaLink</strong>. Your account is ready.</p>
                  <p>You can now browse our dental services, book appointments, and pay securely online.</p>
                  <div style="margin: 30px 0; text-align: center;">
                    <a href="http://localhost:5173/services"
                       style="background: linear-gradient(135deg,#6ec1c3,#9ad0a6); color:white; padding:12px 28px;
                              border-radius:8px; text-decoration:none; font-weight:bold;">
                      Book an Appointment
                    </a>
                  </div>
                  <p style="color:#888; font-size:12px; margin-top:40px;">
                    If you did not create this account, please ignore this email.
                  </p>
                </body>
                </html>
                """.formatted(firstName);
    }

    private String buildConfirmationHtml(String firstName,
                                         String serviceName,
                                         String dentistName,
                                         LocalDateTime appointmentDatetime) {
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("MMMM d, yyyy");
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("h:mm a");

        String date = appointmentDatetime.format(dateFmt);
        String time = appointmentDatetime.format(timeFmt);

        return """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;">
                  <div style="text-align: center; margin-bottom: 30px;">
                    <h1 style="color: #6ec1c3;">🦷 DentaLink</h1>
                  </div>
                  <h2>Appointment Confirmed! ✅</h2>
                  <p>Hi <strong>%s</strong>, your appointment has been confirmed. See the details below:</p>
                  <div style="background:#f9fafb; border:1px solid #e5e7eb; border-radius:10px; padding:20px; margin:20px 0;">
                    <table style="width:100%%; font-size:14px;">
                      <tr><td style="color:#6b7280; padding:6px 0;">Service</td>   <td style="font-weight:bold;">%s</td></tr>
                      <tr><td style="color:#6b7280; padding:6px 0;">Dentist</td>   <td style="font-weight:bold;">%s</td></tr>
                      <tr><td style="color:#6b7280; padding:6px 0;">Date</td>      <td style="font-weight:bold;">%s</td></tr>
                      <tr><td style="color:#6b7280; padding:6px 0;">Time</td>      <td style="font-weight:bold;">%s</td></tr>
                      <tr><td style="color:#6b7280; padding:6px 0;">Status</td>    <td style="color:#16a34a; font-weight:bold;">Confirmed</td></tr>
                    </table>
                  </div>
                  <p>Please arrive 10 minutes before your scheduled time.</p>
                  <div style="margin: 30px 0; text-align: center;">
                    <a href="http://localhost:5173/my-appointments"
                       style="background: linear-gradient(135deg,#6ec1c3,#9ad0a6); color:white; padding:12px 28px;
                              border-radius:8px; text-decoration:none; font-weight:bold;">
                      View My Appointments
                    </a>
                  </div>
                  <p style="color:#888; font-size:12px; margin-top:40px;">
                    This is an automated confirmation from DentaLink. Please do not reply to this email.
                  </p>
                </body>
                </html>
                """.formatted(firstName, serviceName, dentistName, date, time);
    }
}