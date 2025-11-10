package com.smartlogi.sdms.domain.model.entity.users;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.PrimaryKeyJoinColumn;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Représente un utilisateur avec le rôle de gestionnaire (superviseur logistique).
 * Hérite de BaseUser.
 */
@Entity
@Table(name = "gestionnaire")
@PrimaryKeyJoinColumn(name = "user_id")

@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class Gestionnaire extends BaseUser {
}