package com.amansia.amansiabackend.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;

@Service
public class SmtpEmailService implements EmailService {
    private final ObjectProvider<JavaMailSender> senders;
    private final String host;
    private final String from;
    private final String frontendUrl;

    public SmtpEmailService(ObjectProvider<JavaMailSender> senders,
            @Value("${spring.mail.host:}") String host,
            @Value("${app.mail.from:}") String from,
            @Value("${app.frontend-url:}") String frontendUrl) {
        this.senders = senders;
        this.host = host;
        this.from = from;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void verifierConfiguration() {
        if (host.isBlank() || from.isBlank() || frontendUrl.isBlank() || senders.getIfAvailable() == null)
            throw new EmailIndisponibleException();
    }

    @Override
    public void envoyerReinitialisation(String destinataire, String token, long dureeMinutes) {
        verifierConfiguration();
        String lien = frontendUrl.replaceAll("/+$", "") + "/reinitialiser-mot-de-passe?token=" + token;
        JavaMailSender sender = senders.getObject();
        MimeMessage message = sender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(from, "Amansia");
            helper.setTo(destinataire);
            helper.setSubject("Amansia — réinitialisation de votre mot de passe");
            String texte = "Nous avons reçu une demande de réinitialisation de votre mot de passe.\n"
                    + "Ouvrez ce lien : " + lien + "\nCe lien expire dans " + dureeMinutes + " minutes.\n"
                    + "Si vous n'êtes pas à l'origine de cette demande, ignorez cet email.";
            String html = "<div style=\"font-family:Arial,sans-serif;max-width:560px;margin:auto;color:#263c30\">"
                    + "<h1 style=\"font-family:Georgia,serif\">Amansia</h1>"
                    + "<h2>Réinitialisation de votre mot de passe</h2>"
                    + "<p>Nous avons reçu une demande de réinitialisation du mot de passe de votre compte.</p>"
                    + "<p><a href=\"" + lien + "\" style=\"display:inline-block;padding:13px 20px;background:#344e41;color:white;text-decoration:none\">"
                    + "Réinitialiser mon mot de passe</a></p>"
                    + "<p>Ce lien expire dans " + dureeMinutes + " minutes.</p>"
                    + "<p>Si vous n'êtes pas à l'origine de cette demande, vous pouvez ignorer cet email.</p></div>";
            helper.setText(texte, html);
            sender.send(message);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            throw new MailSendException("L'email de réinitialisation n'a pas pu être envoyé.", e);
        }
    }

    @Override
    public void envoyerTransactionnel(String destinataire, String sujet, String texte, String html) {
        verifierConfiguration();
        JavaMailSender sender = senders.getObject();
        MimeMessage message = sender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(from, "Amansia");
            helper.setTo(destinataire);
            helper.setSubject(sujet);
            helper.setText(texte, html);
            sender.send(message);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            throw new MailSendException("L'email transactionnel n'a pas pu être envoyé.", e);
        }
    }
}
