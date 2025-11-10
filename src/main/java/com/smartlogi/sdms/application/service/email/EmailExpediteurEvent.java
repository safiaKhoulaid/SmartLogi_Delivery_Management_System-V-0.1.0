package com.smartlogi.sdms.application.service.email;

import com.smartlogi.sdms.application.service.EmailService;
import com.smartlogi.sdms.domain.model.entity.Mission;
import com.smartlogi.sdms.domain.model.entity.users.Livreur;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class EmailExpediteurEvent {

    private final EmailService emailService;

    public void notifyCollecte(Livreur livreur, Mission mission) throws MessagingException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        String subject = "Collecte de votre colis";
        String message = String.format(
                "Bonjour %s , Notre livreur qui s 'applle  %s  %s va passer chez vous pour collecter la colis , le %s",
                getNomComplet(mission),
                livreur.getLastName(),
                livreur.getFirstName(),
                mission.getDatePrevue().format(formatter));

        emailService.sendTemplateEmail(mission.getColis().getClientExpediteur().getEmail(), subject, getNomComplet(mission), message);
    }

    public String getNomComplet(Mission mission) {
        return mission.getColis().getClientExpediteur().getFirstName() + mission.getColis().getClientExpediteur().getLastName();

    }

}
