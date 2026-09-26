package com.example.gestion_rh.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:${spring.mail.username:noreply@grh.local}}")
    private String from;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    public boolean isEnabled() {
        return mailEnabled;
    }

    /**
     * Envoie login + mot de passe temporaire.
     * @throws IllegalStateException si mail désactivé ou échec SMTP
     */
    public void envoyerIdentifiants(String to, String nomComplet, String login, String motDePasse) {
        if (!mailEnabled) {
            throw new IllegalStateException(
                    "Envoi email désactivé (MAIL_ENABLED=false). Configurez SMTP pour livrer le mot de passe.");
        }
        if (to == null || to.isBlank() || !to.contains("@")) {
            throw new IllegalStateException("Adresse email destinataire invalide : " + to);
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to.trim());
            helper.setSubject("GRH Pro — vos identifiants de connexion");
            helper.setText("""
                    Bonjour %s,

                    Votre compte GRH Pro a été créé.

                    Identifiant : %s
                    Mot de passe temporaire : %s

                    Connectez-vous puis changez votre mot de passe dès que possible.

                    Cordialement,
                    Service RH — GRH Pro
                    """.formatted(
                    nomComplet != null && !nomComplet.isBlank() ? nomComplet : login,
                    login,
                    motDePasse), false);
            mailSender.send(message);
            log.info("Email identifiants envoyé à {}", to);
        } catch (MessagingException | MailException e) {
            throw new IllegalStateException("Échec d'envoi de l'email à " + to + " : " + e.getMessage(), e);
        }
    }
}
