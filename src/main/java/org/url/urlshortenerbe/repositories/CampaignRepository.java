package org.url.urlshortenerbe.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.url.urlshortenerbe.entities.Campaign;

public interface CampaignRepository extends JpaRepository<Campaign, String> {
    boolean existsByIdAndUserId(String campaignId, String userId);

    Page<Campaign> findAllByDeletedIs(boolean deleted, Pageable pageable);

    Page<Campaign> findAllByUserIdAndDeletedIs(String userId, boolean deleted, Pageable pageable);

    Optional<Campaign> findByIdAndUserId(String campaignId, String userId);

    @Query(
            """
			SELECT c
			FROM
				Campaign c
			WHERE
				c.user.id = :userId
				AND (
					LOWER(c.name) LIKE LOWER(CONCAT('%', c.name, '%'))
				)
			""")
    List<Campaign> searchCampaignsByNameWithinUserId(String name, String userId, Pageable pageable);
}
