package org.url.urlshortenerbe.repositories;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.url.urlshortenerbe.entities.Url;

public interface UrlRepository extends JpaRepository<Url, Integer> {
    boolean existsByHash(String hash);

    Page<Url> findAllByUserId(String userId, Pageable pageable);

    Page<Url> findAllByDeletedIs(boolean deleted, Pageable pageable);

    Page<Url> findAllByUserIdAndDeletedIs(String userId, boolean deleted, Pageable pageable);

    Page<Url> findAllByCampaignIdAndUserId(String campaignId, String userId, Pageable pageable);

    Page<Url> findAllByCampaignIdAndUserIdAndDeletedIs(
            String campaignId, String userId, boolean deleted, Pageable pageable);

    Optional<Url> findByHash(String hash);

    Optional<Url> findByHashAndUserId(String hash, String userId);

    Optional<Url> findByHashAndCampaignIdAndUserId(String urlId, String campaignId, String userId);

    @Query(
            """
		SELECT
			u,
			COUNT(c.id) AS clickCount
		FROM
			Url u
		JOIN
			Click c ON u.id = c.url.id
		WHERE
			u.campaign.id = :campaignId
			AND u.user.id = :userId
			AND c.clickedAt BETWEEN :startDate AND :endDate
		GROUP BY
			u.id
		ORDER BY
			clickCount DESC
	""")
    List<Object[]> findMostClickedUrlsByCampaignIdAndUserIdAndDateRange(
            @Param("campaignId") String campaignId,
            @Param("userId") String userId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);

    List<Url> findAllByCampaignIdAndUserId(String campaignId, String userId);
}
