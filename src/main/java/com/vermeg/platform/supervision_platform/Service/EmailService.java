package com.vermeg.platform.supervision_platform.Service;

import com.vermeg.platform.supervision_platform.Entity.Alert;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendAlertEmail(Alert alert, String recipientEmail) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(recipientEmail);
            helper.setSubject("[" + alert.getLevel().name() + "] Alert: " + alert.getMessage());
            helper.setText(buildEmailBody(alert), true);

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Failed to send alert email: " + e.getMessage(), e);
        }
    }

    private String buildEmailBody(Alert alert) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif;">
                    <h2 style="color: %s;">🚨 Platform Alert</h2>
                    <table border="1" cellpadding="8" cellspacing="0">
                        <tr><td><b>Server</b></td><td>%s</td></tr>
                        <tr><td><b>Level</b></td><td>%s</td></tr>
                        <tr><td><b>Message</b></td><td>%s</td></tr>
                        <tr><td><b>Time</b></td><td>%s</td></tr>
                    </table>
                    <p>Please check the supervision platform dashboard for more details.</p>
                </body>
                </html>
                """.formatted(
                getLevelColor(alert.getLevel().name()),
                alert.getServer().getName(),
                alert.getLevel().name(),
                alert.getMessage(),
                alert.getCreatedAt().toString()
        );
    }

    private String getLevelColor(String level) {
        return switch (level) {
            case "CRITICAL" -> "#FF0000";
            case "WARN" -> "#FFA500";
            default -> "#0000FF";
        };
    }
}
