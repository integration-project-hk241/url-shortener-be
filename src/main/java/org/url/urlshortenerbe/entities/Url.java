package org.url.urlshortenerbe.entities;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Url {
    @Id
    private String shortUrl;

    @Column(nullable = false)
    private String longUrl;

    @Column(nullable = false)
    private Date createdAt;

    @Column(nullable = false)
    private Date expiresAt;

    private Date lastVisit;

    @ManyToOne
    private User user;
}