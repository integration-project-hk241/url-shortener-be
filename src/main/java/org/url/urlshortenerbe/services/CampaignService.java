package org.url.urlshortenerbe.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.url.urlshortenerbe.dtos.requests.CampaignCreationRequest;
import org.url.urlshortenerbe.dtos.requests.CampaignUpdateRequest;
import org.url.urlshortenerbe.dtos.responses.CampaignResponse;
import org.url.urlshortenerbe.dtos.responses.PageResponse;
import org.url.urlshortenerbe.entities.Campaign;
import org.url.urlshortenerbe.entities.User;
import org.url.urlshortenerbe.exceptions.AppException;
import org.url.urlshortenerbe.exceptions.ErrorCode;
import org.url.urlshortenerbe.mappers.CampaignMapper;
import org.url.urlshortenerbe.repositories.CampaignRepository;
import org.url.urlshortenerbe.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CampaignService {
    private final CampaignRepository campaignRepository;
    private final UserRepository userRepository;

    private final CampaignMapper campaignMapper;

    public PageResponse<CampaignResponse> getAll(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);

        Page<Campaign> campaigns = campaignRepository.findAll(pageRequest);

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

        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOTFOUND));

        campaign.setUser(user);

        campaign = campaignRepository.save(campaign);

        return campaignMapper.toCampaignResponse(campaign);
    }

    public CampaignResponse getOneByUserIdAndCampaignId(String userId, String campaignId) {
        if (!userRepository.existsById(userId)) {
            throw new AppException(ErrorCode.USER_NOTFOUND);
        }

        Campaign campaign = campaignRepository
                .findByIdAndUserId(campaignId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.CAMPAIGN_NOTFOUND));

        return campaignMapper.toCampaignResponse(campaign);
    }

    public CampaignResponse getOneByUserIdAndName(String userId, String name) {
        if (!userRepository.existsById(userId)) {
            throw new AppException(ErrorCode.USER_NOTFOUND);
        }

        Campaign campaign = campaignRepository
                .findByNameAndUserId(name, userId)
                .orElseThrow(() -> new AppException(ErrorCode.CAMPAIGN_NOTFOUND));

        return campaignMapper.toCampaignResponse(campaign);
    }

    public PageResponse<CampaignResponse> getAllByUserId(String userId, int page, int size) {
        if (!userRepository.existsById(userId)) {
            throw new AppException(ErrorCode.USER_NOTFOUND);
        }

        PageRequest pageRequest = PageRequest.of(page, size);

        Page<Campaign> campaigns = campaignRepository.findAllByUserId(userId, pageRequest);

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
        if (!userRepository.existsById(userId)) {
            throw new AppException(ErrorCode.USER_NOTFOUND);
        }

        Campaign campaign = campaignRepository
                .findByIdAndUserId(campaignId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.CAMPAIGN_NOTFOUND));

        campaignMapper.updateCampaign(campaign, campaignUpdateRequest);

        campaign = campaignRepository.save(campaign);

        return campaignMapper.toCampaignResponse(campaign);
    }

    public void deleteByIdAndUserId(String campaignId, String userId) {
        if (!userRepository.existsById(userId)) {
            throw new AppException(ErrorCode.USER_NOTFOUND);
        }

        campaignRepository.deleteByIdAndUserId(campaignId, userId);
    }
}
