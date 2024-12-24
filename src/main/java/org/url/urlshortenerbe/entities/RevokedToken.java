package org.url.urlshortenerbe.entities;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class RevokedToken {
    @Id
    String id;

    Date expiryDate;
}