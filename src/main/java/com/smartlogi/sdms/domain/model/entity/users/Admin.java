package com.smartlogi.sdms.domain.model.entity.users;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "admins")
@PrimaryKeyJoinColumn(name = "user_id") // Kay-lsaq m3a table base_user
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
// @AllArgsConstructor // Mahtajinahch daba hit ma3ndouch champs khassin bih
@EqualsAndHashCode(callSuper = true)
public class Admin extends BaseUser {

}