package org.url.urlshortenerbe.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.url.urlshortenerbe.entities.Click;

public interface ClickRepository extends JpaRepository<Click, Long> {
    Integer countByUrlId(int id);

    @Query(
            """
			SELECT
				c
			FROM
				Click c
			LEFT JOIN
				Url u
			ON
				c.url.id = u.id
			WHERE
				u.hash = :hash
				AND u.user.id = :userId
				AND u.campaign.id = :campaignId
	""")
    Page<Click> findAllByHashAndCampaignIdAndUserId(String hash, String campaignId, String userId, Pageable pageable);
}
