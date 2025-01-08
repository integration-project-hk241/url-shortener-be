package org.url.urlshortenerbe.services;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.url.urlshortenerbe.dtos.requests.CampaignCreationRequest;
import org.url.urlshortenerbe.dtos.requests.CampaignUpdateRequest;
import org.url.urlshortenerbe.dtos.responses.CampaignResponse;
import org.url.urlshortenerbe.dtos.responses.PageResponse;
import org.url.urlshortenerbe.entities.Campaign;
import org.url.urlshortenerbe.entities.Url;
import org.url.urlshortenerbe.entities.User;
import org.url.urlshortenerbe.exceptions.AppException;
import org.url.urlshortenerbe.exceptions.ErrorCode;
import org.url.urlshortenerbe.mappers.CampaignMapper;
import org.url.urlshortenerbe.repositories.CampaignRepository;
import org.url.urlshortenerbe.repositories.UrlRepository;
import org.url.urlshortenerbe.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CampaignService {
    private final UserRepository userRepository;
    private final CampaignRepository campaignRepository;
    private final UrlRepository urlRepository;

    private final CampaignMapper campaignMapper;

    public PageResponse<CampaignResponse> getAll(int page, int size, String type) {
        PageRequest pageRequest = PageRequest.of(page - 1, size);

        Page<Campaign> campaigns =
                switch (type) {
                    case ("not_deleted") -> campaignRepository.findAllByDeletedIs(false, pageRequest);
                    case ("deleted") -> campaignRepository.findAllByDeletedIs(true, pageRequest);
                    default -> campaignRepository.findAll(pageRequest);
                };

        List<CampaignResponse> campaignResponses = campaigns.getContent().stream()
                .map(campaignMapper::toCampaignResponse)
                .toList();

        return PageResponse.<CampaignResponse>builder()
                .items(campaignResponses)
                .page(page)
                .records(campaigns.getTotalElements())
                .totalPages(campaigns.getTotalPages())
                .build();
    }

    public CampaignResponse getOneById(String campaignId) {
        Campaign campaign = campaignRepository
                .findById(campaignId)
                .orElseThrow(() -> new AppException(ErrorCode.CAMPAIGN_NOTFOUND));

        return campaignMapper.toCampaignResponse(campaign);
    }

    public CampaignResponse createByUserId(String userId, CampaignCreationRequest campaignCreationRequest) {
        Campaign campaign = campaignMapper.toCampaign(campaignCreationRequest);

        User user = getCorrectUser(userId);

        if (null == user) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        campaign.setUser(user);
        campaign.setStartDate(Date.from(Instant.now()));
        campaign.setDeleted(false);

        CampaignResponse campaignResponse = campaignMapper.toCampaignResponse(campaignRepository.save(campaign));
        campaignResponse.setUserId(null);

        return campaignResponse;
    }

    public CampaignResponse getOneByUserIdAndCampaignId(String userId, String campaignId) {
        User user = getCorrectUser(userId);

        if (null == user) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        Campaign campaign = campaignRepository
                .findByIdAndUserId(campaignId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.CAMPAIGN_NOTFOUND));

        return campaignMapper.toCampaignResponse(campaign);
    }

    public List<CampaignResponse> searchCampaignByNameWithinUserId(String userId, String name) {
        User user = getCorrectUser(userId);

        if (null == user) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        Pageable pageable = PageRequest.of(0, 20);
        List<Campaign> campaigns = campaignRepository.searchCampaignsByNameWithinUserId(name, userId, pageable);

        return campaigns.stream().map(campaignMapper::toCampaignResponse).toList();
    }

    public PageResponse<CampaignResponse> getAllByUserId(String userId, int page, int size, String type) {
        User user = getCorrectUser(userId);

        if (null == user) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        PageRequest pageRequest = PageRequest.of(page - 1, size);

        Page<Campaign> campaigns =
                switch (type) {
                    case ("not_deleted") -> campaignRepository.findAllByUserIdAndDeletedIs(userId, false, pageRequest);
                    case ("deleted") -> campaignRepository.findAllByUserIdAndDeletedIs(userId, true, pageRequest);
                    default -> campaignRepository.findAll(pageRequest);
                };

        List<CampaignResponse> campaignResponses = campaigns.getContent().stream()
                .map(campaignMapper::toCampaignResponse)
                .toList();

        return PageResponse.<CampaignResponse>builder()
                .items(campaignResponses)
                .page(page)
                .records(campaigns.getTotalElements())
                .totalPages(campaigns.getTotalPages())
                .build();
    }

    public CampaignResponse updateOneWithIdAndUserId(
            String campaignId, String userId, CampaignUpdateRequest campaignUpdateRequest) {
        User user = getCorrectUser(userId);

        if (null == user) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        Campaign campaign = campaignRepository
                .findByIdAndUserId(campaignId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.CAMPAIGN_NOTFOUND));

        if (campaignUpdateRequest.getEndDate().after(campaign.getEndDate())
                || campaignUpdateRequest.getEndDate().before(campaign.getStartDate())) {
            List<Url> urls = urlRepository.findAllByCampaignIdAndUserId(campaignId, userId);
            urls.forEach(url -> {
                url.setExpiresAt(campaignUpdateRequest.getEndDate());
                urlRepository.save(url);
            });
        }

        campaignMapper.updateCampaign(campaign, campaignUpdateRequest);

        campaign = campaignRepository.save(campaign);

        return campaignMapper.toCampaignResponse(campaign);
    }

    public void deleteByIdAndUserId(String campaignId, String userId) {
        User user = getCorrectUser(userId);

        if (null == user) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        Campaign campaign = campaignRepository
                .findByIdAndUserId(campaignId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.CAMPAIGN_NOTFOUND));

        // Soft delete the campaign
        campaign.setDeleted(true);
        campaignRepository.save(campaign);
    }

    public void deleteOneById(String campaignId) {
        Campaign campaign = campaignRepository
                .findById(campaignId)
                .orElseThrow(() -> new AppException(ErrorCode.CAMPAIGN_NOTFOUND));
        campaign.setDeleted(true);

        campaignRepository.save(campaign);
    }

    public CampaignResponse updateOneById(String campaignId, CampaignUpdateRequest campaignUpdateRequest) {
        Campaign campaign = campaignRepository
                .findById(campaignId)
                .orElseThrow(() -> new AppException(ErrorCode.CAMPAIGN_NOTFOUND));

        campaignMapper.updateCampaign(campaign, campaignUpdateRequest);

        campaign = campaignRepository.save(campaign);

        return campaignMapper.toCampaignResponse(campaign);
    }

    private User getCorrectUser(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOTFOUND));

        SecurityContext securityContext = SecurityContextHolder.getContext();
        String email = securityContext.getAuthentication().getName();

        if (!user.getEmail().equals(email)) {
            return null;
        }

        return user;
    }
}
