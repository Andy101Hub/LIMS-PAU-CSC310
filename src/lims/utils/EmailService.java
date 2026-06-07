package lims.utils;

import java.util.Properties;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class EmailService {

    /*
     * IMPORTANT:
     * Replace these with your sender email details.
     * Do not push your real email password to GitHub.
     *
     * If using Gmail, use an App Password, not your normal Gmail password.
     */
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";

    private static final String SENDER_EMAIL = "convyandy19@gmail.com";
    private static final String SENDER_PASSWORD = "qsuhlunonkdcnwjc";

    public static void sendVerificationCode(String recipientEmail,
                                            String recipientName,
                                            String verificationCode) throws Exception {

        Properties properties = new Properties();

        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", SMTP_HOST);
        properties.put("mail.smtp.port", SMTP_PORT);

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });

        Message message = new MimeMessage(session);

        message.setFrom(new InternetAddress(SENDER_EMAIL));
        message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(recipientEmail)
        );

        message.setSubject("Santé Diagnostics Email Verification Code");

        String emailBody =
                "Hello " + recipientName + ",\n\n"
                + "Thank you for registering with Santé Diagnostics.\n\n"
                + "Your email verification code is:\n\n"
                + verificationCode + "\n\n"
                + "This code will expire soon.\n\n"
                + "If you did not create this account, please ignore this email.\n\n"
                + "Santé Diagnostics LIMS";

        message.setText(emailBody);

        Transport.send(message);
    }
}