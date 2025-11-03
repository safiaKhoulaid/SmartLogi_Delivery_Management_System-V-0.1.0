package com.smartlogi.sdms.domain.model.entity.users;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "client")
@Getter
@Setter
@SuperBuilder
@DiscriminatorValue("CLIENT")
public class Client extends BaseUser {
    // Additional client-specific fields can be added here
    
    public Client() {
        super();
    }
}
