package org.url.urlshortenerbe.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.url.urlshortenerbe.entities.RevokedToken;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, String> {}
