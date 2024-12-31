package org.url.urlshortenerbe.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.url.urlshortenerbe.entities.Url;

public interface UrlRepository extends JpaRepository<Url, Integer> {
    Page<Url> findAllByDeletedIs(boolean deleted, Pageable pageable);

    Page<Url> findAllByUserId(String userId, Pageable pageable);

    Page<Url> findAllByCampaignIdAndUserId(String campaignId, String userId, Pageable pageable);

    Optional<Url> findByHash(String hash);

    Optional<Url> findByHashAndUserId(String hash, String userId);

    Optional<Url> findByHashAndCampaignIdAndUserId(String urlId, String campaignId, String userId);

    void deleteByHashAndUserId(String hash, String userId);

    void deleteByHashAndCampaignIdAndUserId(String hash, String campaignId, String userId);

    boolean existsByHash(String hash);
}
