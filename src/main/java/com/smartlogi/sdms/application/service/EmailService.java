package com.smartlogi.sdms.application.service;

import com.smartlogi.sdms.domain.model.entity.Mission;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public void sendTemplateEmail(String to, String subject, String name, String messageText) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        Context context = new Context();
        context.setVariable("subject", subject);
        context.setVariable("title", "Notification Importante");
        context.setVariable("greeting", "Bonjour " + name + ",");
        context.setVariable("message", messageText);
        context.setVariable("footerText", "Vous recevez cet email car vous êtes inscrit à notre service.");
        context.setVariable("year", 2025);

        // Assure-toi que le template est bien dans src/main/resources/templates/
        String htmlContent = templateEngine.process("email-template.html", context);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        helper.setFrom("ton.email@gmail.com");

        mailSender.send(message);
    }


}
