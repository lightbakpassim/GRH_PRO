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

    /**
     * Envoie le PDF hebdomadaire au DG (login email).
     * No-op silencieux si mail désactivé ; lève si SMTP échoue.
     */
    public void envoyerRapportPdf(String to, String nomEntreprise, String sujet,
                                  String nomFichier, byte[] pdf) {
        if (!mailEnabled) {
            log.debug("MAIL_ENABLED=false — rapport « {} » non envoyé par email", sujet);
            return;
        }
        if (to == null || to.isBlank() || !to.contains("@")) {
            log.warn("Login DG non email — rapport non envoyé par mail : {}", to);
            return;
        }
        if (pdf == null || pdf.length == 0) {
            throw new IllegalStateException("PDF vide — envoi impossible");
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to.trim());
            helper.setSubject(sujet != null ? sujet : "GRH Pro — Rapport hebdomadaire");
            helper.setText("""
                    Bonjour,

                    Veuillez trouver ci-joint le rapport hebdomadaire d'activité
                    de l'entreprise « %s ».

                    Ce document est strictement confidentiel et propre à votre organisation.

                    Cordialement,
                    Plateforme GRH Pro
                    """.formatted(nomEntreprise != null ? nomEntreprise : "votre entreprise"), false);
            helper.addAttachment(
                    nomFichier != null ? nomFichier : "rapport-hebdomadaire.pdf",
                    () -> new java.io.ByteArrayInputStream(pdf),
                    "application/pdf");
            mailSender.send(message);
            log.info("Rapport PDF envoyé à {} ({})", to, nomEntreprise);
        } catch (MessagingException | MailException e) {
            throw new IllegalStateException("Échec d'envoi du rapport à " + to + " : " + e.getMessage(), e);
        }
    }
}
