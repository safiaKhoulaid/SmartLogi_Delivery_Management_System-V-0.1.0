package com.smartlogi.sdms.domain.model.entity.users;


import com.smartlogi.sdms.domain.model.entity.Colis;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "client_expediteur")

@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@PrimaryKeyJoinColumn(name = "user_id")
public class ClientExpediteur extends BaseUser {
    @Column(unique = true, nullable = false)
    private String codeClient;



    @OneToMany(mappedBy = "clientExpediteur",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE}, // Gère l'ajout/modification
            fetch = FetchType.LAZY)
    private List<Colis> colisExpedies;
}