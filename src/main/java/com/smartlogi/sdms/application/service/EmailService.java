package com.smartlogi.sdms.application.service;

import com.smartlogi.sdms.application.dto.Email.EmailRequest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendEmail(EmailRequest request) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        // 1. Préparer le contexte Thymeleaf avec les variables du Builder
        Context context = new Context();
        if (request.getVariables() != null) {
            context.setVariables(request.getVariables());
        }

        // Ajout des variables par défaut si nécessaire
        context.setVariable("subject", request.getSubject());
        context.setVariable("year", 2025);

        // 2. Générer le HTML
        // كنستعملو templateName من الـ Request ولا كنديرو واحد par défaut
        String template = request.getTemplateName() != null ? request.getTemplateName() : "email-template.html";
        String htmlContent = templateEngine.process(template, context);

        // 3. Préparer l'email
        helper.setTo(request.getTo());
        helper.setSubject(request.getSubject());
        helper.setText(htmlContent, true);
        helper.setFrom(fromEmail);

        // 4. Envoyer
        mailSender.send(message);
    }
}