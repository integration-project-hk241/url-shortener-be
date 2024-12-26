package org.url.urlshortenerbe.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.url.urlshortenerbe.entities.Url;

public interface UrlRepository extends JpaRepository<Url, String> {
    Page<Url> findAllByUserId(String userId, Pageable pageable);

    Page<Url> findAllByCampaignIdAndUserId(String campaignId, String userId, Pageable pageable);

    Optional<Url> findByShortUrlAndUserId(String shortUrl, String userId);

    Optional<Url> findByShortUrlAndCampaignIdAndUserId(String urlId, String campaignId, String userId);

    void deleteByShortUrlAndUserId(String shortUrl, String userId);

    void deleteByShortUrlAndCampaignIdAndUserId(String shortUrl, String campaignId, String userId);
}
