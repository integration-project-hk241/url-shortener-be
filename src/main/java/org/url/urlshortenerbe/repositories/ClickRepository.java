package org.url.urlshortenerbe.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.url.urlshortenerbe.entities.Click;

public interface ClickRepository extends JpaRepository<Click, Long> {}
