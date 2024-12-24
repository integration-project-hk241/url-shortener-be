package org.url.urlshortenerbe.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.url.urlshortenerbe.entities.Url;

public interface UrlRepository extends JpaRepository<Url, String> {}
