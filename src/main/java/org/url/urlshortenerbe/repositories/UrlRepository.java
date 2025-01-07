package org.url.urlshortenerbe.repositories;

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
						LEFT JOIN
							Click c ON u.id = c.url.id
						WHERE
							u.campaign.id = :campaignId
							AND u.user.id = :userId
						GROUP BY
							u.id
						ORDER BY
							clickCount DESC
					""")
    List<Object[]> findMostClickedUrlsByCampaignIdAndUserId(
            @Param("campaignId") String campaignId, @Param("userId") String userId);

    List<Url> findAllByCampaignIdAndUserId(String campaignId, String userId);

    @Query(
            """
			SELECT u
			FROM
				Url u
			WHERE
				LOWER(u.hash) LIKE LOWER(CONCAT('%', :q, '%'))
				OR LOWER(u.longUrl) LIKE LOWER(CONCAT('%', :q, '%'))
				OR LOWER(u.user.id) LIKE LOWER(CONCAT('%', :q, '%'))
			""")
    List<Url> searchUrls(String q, Pageable pageable);

    @Query(
            """
			SELECT u
			FROM
				Url u
			WHERE
				u.user.id = :userId
				AND (
					LOWER(u.hash) LIKE LOWER(CONCAT('%', :q, '%'))
					OR LOWER(u.longUrl) LIKE LOWER(CONCAT('%', :q, '%'))
				)
			""")
    List<Url> searchUrlsWithinUserId(String userId, String q, Pageable pageable);

    @Query(
            """
			SELECT u
			FROM
				Url u
			WHERE
				u.user.id = :userId
				AND u.campaign.id = :campaignId
				AND (
					LOWER(u.hash) LIKE LOWER(CONCAT('%', :q, '%'))
					OR LOWER(u.longUrl) LIKE LOWER(CONCAT('%', :q, '%'))
				)
			""")
    List<Url> searchUrlsWithinUserIdAndCampaignId(String userId, String campaignId, String q, Pageable pageable);

    boolean existsByHashAndCampaignIdAndUserId(String hash, String campaignId, String userId);
}
