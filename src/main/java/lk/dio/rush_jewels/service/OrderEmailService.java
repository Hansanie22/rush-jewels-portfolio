package lk.dio.rush_jewels.service;

import lk.dio.rush_jewels.model.*;
import lk.dio.rush_jewels.model.Collection;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

@Service
public class OrderEmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String smtpUsername;  // contact@velorajewellery.com

    @Value("${app.email.noreply:noreply@velorajewellery.com}")
    private String noreplyEmail;     // noreply@velorajewellery.com

    @Value("${app.email.support}")
    private String supportEmail;  // support@velorajewellery.com

    @Value("${app.email.info}")
    private String infoEmail;     // info@velorajewellery.com

    @Value("${app.email.display-name:Velora Fine Jewellery}")
    private String displayName;

    @Value("${app.base-url:https://velorajewellery.com}")
    private String baseUrl;

    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,##0.00");

    public OrderEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * @Async මඟින් මෙය පසුබිමේ ක්‍රියාත්මක කරයි (App එක හිර නොවී).
     * @Retryable මඟින් Error එකක් ආවොත් 3 වතාවක් උත්සාහ කරයි.
     */
    @Retryable(
            value = { Exception.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 5000)
    )
    @Async
    public void sendOrderConfirmationEmail(Orders order, List<OrderItems> orderItems, Payment payment) {
        try {
            User user = order.getUser();
            String toEmail = user.getEmail();
            if (toEmail == null || toEmail.isEmpty()) return;

            String subject = "Order Confirmation - " + order.getId() + " - Velora Fine Jewellery";

            // ✅ HTML Design එක කිසිම වෙනසක් නැතුව ජනනය කරයි
            String htmlContent = buildOrderConfirmationEmailHtml(order, orderItems, payment);

            // ✅ අලුත් HTTP ක්‍රමයට ඊමේල් යවයි
            sendHtmlEmail(toEmail, subject, htmlContent);

        } catch (Exception e) {
            System.err.println("Failed to send order confirmation email for Order ID " + order.getId() + ": " + e.getMessage());
        }
    }

    /**
     * HTML email sender via JavaMailSender SMTP (Gmail App Password)
     */
    private void sendHtmlEmail(String toEmail, String subject, String bodyContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(new InternetAddress(noreplyEmail, displayName));
            helper.setReplyTo(new InternetAddress(supportEmail, displayName));
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(bodyContent, true); // true = HTML

            mailSender.send(message);

        } catch (Exception e) {
            System.err.println("Failed to send email via SMTP: " + e.getMessage());
            throw new RuntimeException("Email sending failed", e); // triggers @Retryable
        }
    }


    @Retryable(value = { Exception.class }, maxAttempts = 3, backoff = @Backoff(delay = 5000))
    @Async
    public void sendGenericNotificationEmail(Orders order, String subject, String title, String subtitle, String mainMessage) {
        try {
            User user = order.getUser();
            if (user == null || user.getEmail() == null || user.getEmail().isEmpty()) return;

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            InternetAddress from = new InternetAddress(noreplyEmail, displayName);
            helper.setFrom(from);
            helper.setTo(user.getEmail());
            helper.setSubject(subject);
            helper.setReplyTo(supportEmail);

            String htmlContent = buildGenericNotificationEmailHtml(order, title, subtitle, mainMessage);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String buildGenericNotificationEmailHtml(Orders order, String title, String subtitle, String mainMessage) {
        User user = order.getUser();
        String userName = (user != null && user.getFname() != null) ? user.getFname() : "Customer";
        String trackUrl = baseUrl + "/account.html?tab=orders";

        return String.format("""
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <title>Velora Fine Jewellery Update</title>
                <link href="https://fonts.googleapis.com/css2?family=Playfair+Display:ital,wght@0,400;0,600;0,700;1,400&family=Bodoni+Moda:ital,wght@0,400;0,700;1,400&family=Lato:wght@300;400;700&display=swap" rel="stylesheet">
                <style>
                    body { font-family: 'Lato', sans-serif; line-height: 1.6; color: #1a1a1a; background-color: #e5e5e5; margin: 0; padding: 0; -webkit-font-smoothing: antialiased; width: 100%% !important; }
                    .wrapper { width: 100%%; min-height: 100vh; background-color: #e5e5e5; padding: 40px 0; display: table; text-align: center; }
                    .container { width: 100%%; max-width: 600px; background: #ffffff; margin: 0 auto; border: 1px solid #dcdcdc; text-align: left; }
                    .header { background: #121212; color: #C5A059; padding: 50px 20px; text-align: center; border-bottom: 4px solid #C5A059; }
                    .header-logo { font-family: 'Playfair Display', serif; font-size: 42px; font-weight: 700; letter-spacing: 2px; margin: 0; text-transform: uppercase; color: #C5A059; line-height: 1.2; }
                    .header-subtitle { font-size: 11px; letter-spacing: 4px; text-transform: uppercase; opacity: 0.9; margin-top: 10px; font-weight: 400; border-top: 1px solid rgba(197, 160, 89, 0.3); display: inline-block; padding-top: 5px; color: #C5A059; }
                    .content { padding: 50px 40px; text-align: center; }
                    .hero h2 { font-family: 'Bodoni Moda', serif; font-size: 32px; color: #121212; margin: 0 0 10px 0; font-style: italic; font-weight: 700; }
                    .hero p { font-size: 16px; margin-bottom: 30px; }
                    .main-msg { font-size: 15px; color: #333; line-height: 1.8; margin-bottom: 40px; padding: 20px; background: #f9f9f9; border-left: 4px solid #C5A059; text-align: left; }
                    .button { display: inline-block; padding: 15px 40px; background: #121212; color: #C5A059; text-decoration: none; font-size: 13px; text-transform: uppercase; letter-spacing: 2px; }
                    .footer { background: #111; color: #666; padding: 40px 20px; text-align: center; font-size: 11px; text-transform: uppercase; }
                </style>
            </head>
            <body>
                <div class="wrapper">
                    <center>
                    <div class="container">
                        <div class="header">
                            <img src="https://velorajewellery.com/favicon.png" alt="Velora Fine Jewellery" width="60" style="display:block; margin: 0 auto 15px auto;">
                            <h1 class="header-logo"><span style="color: #C5A059">Rush</span><span style="color: #ffffff">Jewels</span></h1>
                            <div class="header-subtitle">%s</div>
                        </div>
                        <div class="content">
                            <div class="hero">
                                <h2>%s</h2>
                                <p>Hello, <strong>%s</strong></p>
                            </div>
                            <div class="main-msg">
                                %s
                            </div>
                            <a href="%s" class="button">View Order Status</a>
                        </div>
                        <div class="footer">
                            <p>&copy; 2025 Velora Fine Jewellery. All rights reserved.</p>
                        </div>
                    </div>
                    </center>
                </div>
            </body>
            </html>
            """, subtitle, title, userName, mainMessage, trackUrl);
    }

    // ✅ ඔබේ HTML Design එක 100% ක් එලෙසම ඇත (No Changes)
    private String buildOrderConfirmationEmailHtml(Orders order, List<OrderItems> orderItems, Payment payment) {
        User user = order.getUser();
        DeliveryAddress address = order.getDeliveryAddress();

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");
        String orderDate = dateFormat.format(order.getOrderedAt());

        StringBuilder itemsHtml = new StringBuilder();

        for (OrderItems item : orderItems) {
            String productName;
            String meta = "";
            double price;

            String imageUrl = "https://velorajewellery.com/assets/default-product.png";

            if (item.getProductVariance() != null) {
                ProductVariance variant = item.getProductVariance();
                Product product = variant.getProduct();
                productName = product.getName();
                
                if (product.getImage1() != null && !product.getImage1().isEmpty()) {
                    imageUrl = product.getImage1();
                } else if (product.getImage2() != null && !product.getImage2().isEmpty()) {
                    imageUrl = product.getImage2();
                }

                double regular = variant.getRegularPrice();
                double discount = variant.getDiscountPercentage() != null ? variant.getDiscountPercentage() : 0.0;
                price = (discount > 0) ? regular * (1 - discount / 100) : regular;

                if (variant.getSize() != null && variant.getSize().getSize() != null) meta += "Size: " + variant.getSize().getSize() + " ";
                if (variant.getColor() != null && variant.getColor().getColor() != null) meta += "Color: " + variant.getColor().getColor() + " ";
                if (variant.getGemstone() != null && variant.getGemstone().getGemStone() != null) meta += "Gem: " + variant.getGemstone().getGemStone();

            } else if (item.getCollection() != null) {
                Collection col = item.getCollection();
                productName = "Collection: " + col.getTitle();
                
                if (col.getImage1() != null && !col.getImage1().isEmpty()) {
                    imageUrl = col.getImage1();
                }

                double regular = col.getRegularPrice();
                double discount = col.getDiscountPercentage();
                price = (discount > 0) ? regular * (1 - discount / 100) : col.getPrice();

                meta = "Exclusive Bundle";
            } else {
                productName = "Unknown Item";
                price = 0.0;
            }

            if (!imageUrl.startsWith("http")) {
                imageUrl = baseUrl + (imageUrl.startsWith("/") ? imageUrl : "/" + imageUrl);
            }

            double lineTotal = price * item.getQty();

            itemsHtml.append(String.format("""
                <tr class="item-row">
                    <td>
                        <table width="100%%" cellpadding="0" cellspacing="0" border="0">
                            <tr>
                                <td width="70" valign="top">
                                    <img src="%s" alt="Item" width="60" height="60" style="display:block; border-radius:4px; background:#f4f4f4; object-fit:cover; margin-right: 10px;">
                                </td>
                                <td valign="top">
                                    <div class="item-name">%s</div>
                                    <div class="item-meta">Qty: %d • %s</div>
                                </td>
                            </tr>
                        </table>
                    </td>
                    <td class="item-price">LKR %s</td>
                </tr>
                """, imageUrl, productName, item.getQty(), meta, CURRENCY_FORMAT.format(lineTotal)));
        }

        double shippingCost = order.getShipping() != null ? order.getShipping().getValue() : 0.0;
        String shippingMethodName = order.getShipping() != null ? order.getShipping().getShippingMethod() : "";
        boolean isStorePickup = "POS".equalsIgnoreCase(order.getOrderSource()) || shippingMethodName.toLowerCase().contains("pickup");
        
        double subtotal = payment.getSubTotal();
        double finalTotal = payment.getFinalTotal();

        StringBuilder summaryHtml = new StringBuilder();
        summaryHtml.append(String.format("<div class=\"summary-row\"><span class=\"summary-left\">Subtotal</span><span class=\"summary-right\">LKR %s</span></div>", CURRENCY_FORMAT.format(subtotal)));
        if (!isStorePickup) {
            summaryHtml.append(String.format("<div class=\"summary-row\"><span class=\"summary-left\">Shipping</span><span class=\"summary-right\">LKR %s</span></div>", CURRENCY_FORMAT.format(shippingCost)));
        }
        summaryHtml.append(String.format("<div class=\"summary-row total\"><span class=\"summary-left\">Total Paid</span><span class=\"summary-right\">LKR %s</span></div>", CURRENCY_FORMAT.format(finalTotal)));

        String trackUrl = baseUrl + "/account.html?tab=orders";
        String userName = (user.getFname() != null) ? user.getFname() : "Customer";

        String shippingTitle = isStorePickup ? "Pickup Details" : "Shipping To";
        String addressBlock;
        if (isStorePickup) {
            addressBlock = String.format("""
                <strong>Velora Fine Jewellery (Kandy Store)</strong><br>
                454/5 Daulagala Road<br>
                Pilimathalawa, Sri Lanka<br>
                T: 075 483 2960<br><br>
                <em>Customer: %s %s</em><br>
                <em>Mobile: %s</em>
                """, address.getFirstName(), address.getLastName(), address.getContactNo());
        } else {
            addressBlock = String.format("""
                <strong>%s %s</strong><br>
                %s<br>
                %s
                %s, %s, %s<br>
                T: %s
                """,
                    address.getFirstName(), address.getLastName(),
                    address.getLine1(),
                    (address.getLine2() != null && !address.getLine2().isEmpty()) ? address.getLine2() + "<br>" : "",
                    (address.getCity() != null) ? address.getCity().getCity() : address.getCityText(),
                    (address.getProvince() != null) ? address.getProvince().getProvince() : address.getStateText(),
                    (address.getCountry() != null) ? address.getCountry().getCountry() : "Sri Lanka",
                    address.getContactNo()
            );
        }

        return String.format("""
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <meta http-equiv="X-UA-Compatible" content="IE=edge">
                <title>Velora Fine Jewellery - Order Confirmation</title>
                <link href="https://fonts.googleapis.com/css2?family=Playfair+Display:ital,wght@0,400;0,600;0,700;1,400&family=Bodoni+Moda:ital,wght@0,400;0,700;1,400&family=Lato:wght@300;400;700&display=swap" rel="stylesheet">
                <link href="https://fonts.googleapis.com/css2?family=Libre+Barcode+39+Text&display=swap" rel="stylesheet">
                
                <style>
                    body { font-family: 'Lato', sans-serif; line-height: 1.6; color: #1a1a1a; background-color: #e5e5e5; margin: 0; padding: 0; -webkit-font-smoothing: antialiased; width: 100%% !important; }
                    .wrapper { width: 100%%; min-height: 100vh; background-color: #e5e5e5; padding: 40px 0; display: table; text-align: center; }
                    .container { width: 100%%; max-width: 600px; background: #ffffff; margin: 0 auto; border: 1px solid #dcdcdc; text-align: left; }
                    .header { background: #121212; color: #C5A059; padding: 50px 20px; text-align: center; border-bottom: 4px solid #C5A059; }
                    .header-logo { font-family: 'Playfair Display', serif; font-size: 42px; font-weight: 700; letter-spacing: 2px; margin: 0; text-transform: uppercase; color: #C5A059; line-height: 1.2; }
                    .header-subtitle { font-size: 11px; letter-spacing: 4px; text-transform: uppercase; opacity: 0.9; margin-top: 10px; font-weight: 400; border-top: 1px solid rgba(197, 160, 89, 0.3); display: inline-block; padding-top: 5px; color: #C5A059; }
                    .content { padding: 50px 40px; }
                    .hero { text-align: center; margin-bottom: 40px; border-bottom: 1px solid #e0e0e0; padding-bottom: 30px; }
                    .hero h2 { font-family: 'Bodoni Moda', serif; font-size: 38px; color: #121212; margin: 0 0 10px 0; font-style: italic; font-weight: 700; }
                    .order-meta { display: table; width: 100%%; background: #fff; padding: 20px 0; margin-bottom: 40px; border-bottom: 1px solid #e0e0e0; }
                    .meta-group { display: table-cell; text-align: center; width: 33.33%%; border-right: 1px solid #e0e0e0; vertical-align: middle; }
                    .meta-group:last-child { border-right: none; }
                    .meta-label { font-size: 10px; text-transform: uppercase; color: #666; font-weight: 700; letter-spacing: 1px; display: block; margin-bottom: 5px; }
                    .meta-value { font-size: 14px; color: #121212; font-weight: 600; font-family: 'Playfair Display', serif; }
                    .items-table { width: 100%%; border-collapse: collapse; margin-bottom: 30px; }
                    .items-table th { text-align: left; font-size: 11px; text-transform: uppercase; color: #121212; letter-spacing: 1px; padding: 10px 0; border-bottom: 2px solid #121212; }
                    .item-row td { vertical-align: top; padding: 20px 0; border-bottom: 1px solid #e0e0e0; }
                    .item-name { color: #121212; font-weight: 700; font-size: 16px; font-family: 'Playfair Display', serif; }
                    .item-meta { color: #666; font-size: 12px; margin-top: 5px; text-transform: uppercase; }
                    .item-price { font-weight: 500; color: #121212; text-align: right; font-size: 15px; }
                    .summary-section { margin-top: 20px; width: 100%%; }
                    .summary-row { width: 100%%; display: block; margin-bottom: 10px; font-size: 13px; color: #555; text-transform: uppercase; overflow: hidden; }
                    .summary-left { float: left; }
                    .summary-right { float: right; }
                    .summary-row.total { font-family: 'Playfair Display', serif; font-size: 24px; color: #121212; font-weight: 700; border-top: 2px solid #121212; border-bottom: 2px solid #121212; padding: 15px 0; margin-top: 20px; }
                    .shipping-section { margin-top: 50px; background: #f8f8f8; padding: 30px; border-left: 4px solid #121212; }
                    .section-title { font-family: 'Playfair Display', serif; font-size: 18px; color: #121212; margin-top: 0; margin-bottom: 15px; text-transform: uppercase; }
                    .address-text { font-size: 14px; color: #333; line-height: 1.8; }
                    .contact-strip { margin-top: 40px; border-top: 1px solid #e0e0e0; padding-top: 20px; text-align: center; font-size: 12px; color: #666; }
                    .cta-container { margin-top: 50px; text-align: center; }
                    .button { display: inline-block; padding: 20px 60px; background: #121212; color: #C5A059; text-decoration: none; font-size: 13px; text-transform: uppercase; letter-spacing: 2px; }
                    .footer { background: #111; color: #666; padding: 40px 20px; text-align: center; font-size: 11px; text-transform: uppercase; }
                    .barcode { font-family: 'Libre Barcode 39 Text', cursive; font-size: 40px; color: #fff; opacity: 0.4; margin-top: 20px; display: block; }
                    @media only screen and (max-width: 600px) {
                        .wrapper { padding: 0 !important; }
                        .container { width: 100%% !important; border: none !important; }
                        .header { padding: 30px 15px !important; }
                        .content { padding: 30px 20px !important; }
                        .order-meta { display: block !important; }
                        .meta-group { display: block !important; width: 100%% !important; border-right: none !important; border-bottom: 1px solid #eee !important; padding: 15px 0 !important; }
                        .button { display: block !important; width: 100%% !important; box-sizing: border-box !important; }
                    }
                </style>
            </head>
            <body>
                <div class="wrapper">
                    <center>
                    <div class="container">
                        <div class="header">
                            <img src="https://velorajewellery.com/favicon.png" alt="Velora Fine Jewellery" width="60" style="display:block; margin: 0 auto 15px auto;">
                            <h1 class="header-logo"><span style="color: #C5A059">Rush</span><span style="color: #ffffff">Jewels</span></h1>
                            <div class="header-subtitle">Est. 2025 • Kandy</div>
                        </div>
                        <div class="content">
                            <div class="hero">
                                <h2>Order Confirmed</h2>
                                <p>Thank you, <strong>%s</strong>. Your selection is secured.</p>
                            </div>
                            <div class="order-meta">
                                <div class="meta-group"><span class="meta-label">Order No.</span><span class="meta-value">%s</span></div>
                                <div class="meta-group"><span class="meta-label">Date</span><span class="meta-value">%s</span></div>
                                <div class="meta-group"><span class="meta-label">Total</span><span class="meta-value">LKR %s</span></div>
                            </div>
                            <table class="items-table">
                                <thead><tr><th>Description</th><th style="text-align: right;">Amount</th></tr></thead>
                                <tbody>%s</tbody>
                            </table>
                            <div class="summary-section">
                                %s
                            </div>
                            <div class="shipping-section">
                                <h3 class="section-title">%s</h3>
                                <div class="address-text">%s</div>
                            </div>
                            <div class="contact-strip">
                                <span>contact@velorajewellery.com</span> &nbsp;|&nbsp; <span>+94 75 483 2960</span>
                            </div>
                            <div class="cta-container">
                                <a href="%s" class="button">Track Order Status</a>
                            </div>
                        </div>
                        <div class="footer">
                            <p>&copy; 2025 Velora Fine Jewellery. All rights reserved.</p>
                            <div class="barcode">%s</div>
                        </div>
                    </div>
                    </center>
                </div>
            </body>
            </html>
            """,
                userName, order.getId(), orderDate, CURRENCY_FORMAT.format(finalTotal), itemsHtml.toString(),
                summaryHtml.toString(), shippingTitle, addressBlock, trackUrl, order.getId()
        );
    }

    @Async
    public void sendSmartCampaign(String title, String subtitle, List<lk.dio.rush_jewels.model.ProductVariance> products, List<lk.dio.rush_jewels.model.User> subscribers) {
        if (subscribers == null || subscribers.isEmpty()) return;

        // Build HTML Grid
        StringBuilder gridHtml = new StringBuilder();
        for (lk.dio.rush_jewels.model.ProductVariance pv : products) {
            String imageUrl = pv.getProduct().getImage1();
            if (imageUrl != null && !imageUrl.startsWith("http")) {
                imageUrl = baseUrl + "/" + imageUrl.replace("\\", "/");
            }
            gridHtml.append(String.format("""
                <div style="width: 48%%; display: inline-block; margin-bottom: 20px; text-align: center; background: #fff; padding: 10px; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.05); box-sizing: border-box; vertical-align: top;">
                    <img src="%s" alt="%s" style="width: 100%%; max-height: 180px; object-fit: cover; border-radius: 6px; margin-bottom: 10px;">
                    <h4 style="margin: 0 0 5px 0; font-size: 14px; color: #1f2937;">%s</h4>
                    <p style="margin: 0 0 10px 0; font-size: 14px; font-weight: bold; color: #d4af37;">LKR %s</p>
                    <a href="%s/shop.html" style="display: inline-block; padding: 8px 15px; background: #000; color: #fff; text-decoration: none; font-size: 12px; border-radius: 4px; font-weight: bold;">SHOP NOW</a>
                </div>
            """, imageUrl, pv.getProduct().getName(), pv.getProduct().getName(), CURRENCY_FORMAT.format(pv.getPrice()), baseUrl));
        }

        String htmlContent = String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { margin: 0; padding: 0; font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; background-color: #f3f4f6; color: #333; }
                    .container { max-width: 600px; margin: 40px auto; background: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,0.08); }
                    .header { background-color: #000000; padding: 30px; text-align: center; color: #ffffff; border-bottom: 3px solid #d4af37; }
                    .header img { display: block; margin: 0 auto 15px auto; width: 60px; }
                    .header h1 { margin: 0; font-size: 24px; font-weight: 700; letter-spacing: 2px; text-transform: uppercase; }
                    .header p { margin: 10px 0 0 0; color: #d4af37; font-size: 14px; }
                    .content { padding: 40px 30px; text-align: center; }
                    .title { font-size: 22px; font-weight: bold; margin-bottom: 10px; color: #000; }
                    .subtitle { font-size: 16px; color: #666; margin-bottom: 30px; line-height: 1.5; }
                    .grid-container { display: flex; flex-wrap: wrap; justify-content: space-between; text-align: left; }
                    .footer { background: #f9fafb; padding: 20px; text-align: center; font-size: 12px; color: #9ca3af; border-top: 1px solid #e5e7eb; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <img src="https://velorajewellery.com/favicon.png" alt="Velora Fine Jewellery">
                        <h1><span style="color: #d4af37;">Rush</span> Jewels</h1>
                        <p>Fine Jewelry & Watches</p>
                    </div>
                    <div class="content">
                        <div class="title">%s</div>
                        <div class="subtitle">%s</div>
                        <div class="grid-container" style="text-align: center;">
                            %s
                        </div>
                    </div>
                    <div class="footer">
                        <p>You received this email because you are subscribed to Velora Fine Jewellery updates.</p>
                        <p>&copy; 2025 Velora Fine Jewellery. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """, title, subtitle, gridHtml.toString());

        for (lk.dio.rush_jewels.model.User user : subscribers) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                helper.setFrom(new jakarta.mail.internet.InternetAddress(infoEmail, displayName));
                helper.setTo(user.getEmail());
                helper.setSubject(title);
                helper.setText(htmlContent, true);
                mailSender.send(message);
            } catch (Exception e) {
                System.err.println("Failed to send marketing email to: " + user.getEmail() + " - " + e.getMessage());
            }
        }
    }
}
