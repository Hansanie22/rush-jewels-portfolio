package lk.dio.rush_jewels.service;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.Year;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.email.from}")                    // contact@velorajewellery.com
    private String fromEmail;

    @Value("${app.email.support}")                 // support@velorajewellery.com
    private String supportEmail;

    @Value("${app.email.info}")                    // info@velorajewellery.com
    private String infoEmail;

    @Value("${app.email.display-name:Velora Fine Jewellery}")
    private String displayName;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Sends OTP verification email via Gmail SMTP (App Password)
     */
    public void sendVerificationEmail(String toEmail, String code) {
        String subject = "RUSH JEWELS - Verification Code";

        String body = "<!DOCTYPE html>" +
                "<html lang='en' xmlns='http://www.w3.org/1999/xhtml' xmlns:o='urn:schemas-microsoft-com:office:office'>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<meta name='x-apple-disable-message-reformatting'>" +
                "<title>Velora Fine Jewellery - Verification Code</title>" +
                "<style>" +
                "table, td { border-collapse: collapse; mso-table-lspace: 0pt; mso-table-rspace: 0pt; }" +
                "body { font-family: 'Lato', sans-serif; line-height: 1.6; color: #1a1a1a; background-color: #e5e5e5; margin: 0; padding: 0; -webkit-font-smoothing: antialiased; width: 100% !important; height: 100% !important; }" +
                "img { border: 0; line-height: 100%; outline: none; text-decoration: none; -ms-interpolation-mode: bicubic; }" +
                ".wrapper { background-color: #e5e5e5; padding: 40px 0; }" +
                ".main-table { background-color: #ffffff; width: 100%; max-width: 600px; margin: 0 auto; border: 1px solid #dcdcdc; box-shadow: 0 0 20px rgba(0,0,0,0.1); }" +
                ".header { background-color: #121212; padding: 50px 20px; text-align: center; border-bottom: 4px solid #C5A059; }" +
                ".header-logo { font-family: 'Playfair Display', serif; font-size: 42px; font-weight: 700; color: #C5A059; text-transform: uppercase; letter-spacing: 2px; margin: 0; line-height: 1.2; }" +
                ".header-subtitle { color: #C5A059; font-size: 11px; letter-spacing: 4px; text-transform: uppercase; opacity: 0.9; margin-top: 10px; border-top: 1px solid rgba(197,160,89,0.3); display: inline-block; padding-top: 5px; }" +
                ".content-cell { padding: 50px 40px; text-align: center; }" +
                ".hero-title { font-family: 'Bodoni Moda', serif; font-size: 32px; color: #121212; margin: 0 0 10px 0; font-style: italic; font-weight: 700; line-height: 1.2; }" +
                ".hero-text { color: #666666; font-size: 16px; margin: 0; letter-spacing: 0.5px; }" +
                ".otp-box { background-color: #F9F8F4; border: 1px solid #C5A059; padding: 30px 0; margin: 30px 0; text-align: center; }" +
                ".otp-code { font-family: 'Playfair Display', serif; font-size: 48px; font-weight: 700; letter-spacing: 10px; color: #121212; margin: 0; display: inline-block; }" +
                ".instruction { font-size: 13px; color: #666666; margin-top: 20px; display: block; line-height: 1.5; }" +
                ".security-note { font-size: 12px; color: #888888; margin-top: 30px; border-top: 1px solid #e0e0e0; padding-top: 20px; }" +
                ".contact { margin-top: 40px; border-top: 1px solid #e0e0e0; padding-top: 20px; font-size: 12px; color: #666666; }" +
                ".footer { background-color: #111111; color: #666666; padding: 30px 20px; text-align: center; font-size: 11px; text-transform: uppercase; letter-spacing: 1px; }" +
                "@media screen and (max-width: 600px) {" +
                "  .wrapper { padding: 0 !important; }" +
                "  .main-table { width: 100% !important; border: none !important; box-shadow: none !important; }" +
                "  .header { padding: 40px 20px !important; }" +
                "  .header-logo { font-size: 32px !important; }" +
                "  .content-cell { padding: 30px 20px !important; }" +
                "  .hero-title { font-size: 26px !important; }" +
                "  .otp-code { font-size: 36px !important; letter-spacing: 5px !important; }" +
                "  .contact-item { display: block !important; margin: 5px 0 !important; }" +
                "  .contact-sep { display: none !important; }" +
                "}" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<center style='width: 100%; background-color: #e5e5e5;'>" +
                "<div class='wrapper'>" +
                "<table class='main-table' role='presentation' border='0' cellpadding='0' cellspacing='0'>" +
                "<tr><td class='header'>" +
                "<h1 class='header-logo'>Velora Fine Jewellery</h1>" +
                "<div class='header-subtitle'>Est. 2025 &bull; Kandy</div>" +
                "</td></tr>" +
                "<tr><td class='content-cell'>" +
                "<div style='border-bottom: 1px solid #e0e0e0; padding-bottom: 30px; margin-bottom: 30px;'>" +
                "<h2 class='hero-title'>Verify Your Identity</h2>" +
                "<p class='hero-text'>Use the code below to complete your login.</p>" +
                "</div>" +
                "<div class='otp-box'><span class='otp-code'>" + code + "</span></div>" +
                "<p class='instruction'>This code will remain valid for 10 minutes.<br>Do not share this code with anyone.</p>" +
                "<div class='security-note'><p style='margin:0;'>If you did not request this verification code, please ignore this email or contact our support concierge immediately.</p></div>" +
                "<div class='contact'><p style='margin:0;'>" +
                "<span class='contact-item'>" + infoEmail + "</span>" +
                "<span class='contact-sep' style='margin:0 10px; color:#e0e0e0;'>|</span>" +
                "<span class='contact-item'>+94 75 483 2960</span>" +
                "</p></div>" +
                "</td></tr>" +
                "<tr><td class='footer'><p style='margin:0;'>&copy; " + Year.now() + " Velora Fine Jewellery. All rights reserved.</p></td></tr>" +
                "</table></div></center></body></html>";

        sendHtmlEmail(toEmail, subject, body);
    }

    /**
     * Generic HTML email sender via JavaMailSender SMTP (Gmail App Password)
     */
    public void sendHtmlEmail(String toEmail, String subject, String bodyContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(new InternetAddress(infoEmail, displayName));
            helper.setReplyTo(new InternetAddress(supportEmail, displayName));
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(bodyContent, true); // true = send as HTML

            mailSender.send(message);

        } catch (Exception e) {
            System.err.println("Failed to send email to " + toEmail + ": " + e.getMessage());
        }
    }
}