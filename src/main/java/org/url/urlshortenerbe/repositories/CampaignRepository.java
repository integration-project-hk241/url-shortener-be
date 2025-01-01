package org.url.urlshortenerbe.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.url.urlshortenerbe.entities.Campaign;

public interface CampaignRepository extends JpaRepository<Campaign, String> {
    boolean existsByIdAndUserId(String campaignId, String userId);

    Page<Campaign> findAllByDeletedIs(boolean deleted, Pageable pageable);

    Page<Campaign> findAllByUserIdAndDeletedIs(String userId, boolean deleted, Pageable pageable);

    Optional<Campaign> findByIdAndUserId(String campaignId, String userId);

    Optional<Campaign> findByNameAndUserId(String name, String userId);
}
