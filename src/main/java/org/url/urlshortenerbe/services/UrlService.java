package org.url.urlshortenerbe.services;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.url.urlshortenerbe.dtos.requests.UrlCreationRequest;
import org.url.urlshortenerbe.dtos.requests.UrlUpdateRequest;
import org.url.urlshortenerbe.dtos.responses.ClickResponse;
import org.url.urlshortenerbe.dtos.responses.PageResponse;
import org.url.urlshortenerbe.dtos.responses.UrlResponse;
import org.url.urlshortenerbe.entities.Campaign;
import org.url.urlshortenerbe.entities.Click;
import org.url.urlshortenerbe.entities.Url;
import org.url.urlshortenerbe.entities.User;
import org.url.urlshortenerbe.exceptions.AppException;
import org.url.urlshortenerbe.exceptions.ErrorCode;
import org.url.urlshortenerbe.mappers.ClickMapper;
import org.url.urlshortenerbe.mappers.UrlMapper;
import org.url.urlshortenerbe.repositories.CampaignRepository;
import org.url.urlshortenerbe.repositories.ClickRepository;
import org.url.urlshortenerbe.repositories.UrlRepository;
import org.url.urlshortenerbe.repositories.UserRepository;
import org.url.urlshortenerbe.utils.Base62Encoder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UrlService {
    @Value("${url.length}")
    private int hashLength;

    @Value("${url.expiration-time}")
    private int expirationTime;

    private final UserRepository userRepository;
    private final CampaignRepository campaignRepository;
    private final UrlRepository urlRepository;
    private final ClickRepository clickRepository;

    private final UrlMapper urlMapper;
    private final ClickMapper clickMapper;

    private final Base62Encoder base62Encoder;

    // Create url for guest only
    public UrlResponse createForGuest(UrlCreationRequest urlCreationRequest) throws NoSuchAlgorithmException {
        // longUrl, alias, userid
        Url url = create(urlCreationRequest);
        url.setUser(null);

        // Save url
        url = urlRepository.save(url);

        UrlResponse urlResponse = urlMapper.toUrlResponse(url);
        urlResponse.setDeleted(null);

        return urlResponse;
    }

    public UrlResponse createWithUserId(String userId, UrlCreationRequest urlCreationRequest)
            throws NoSuchAlgorithmException {
        User user = getCorrectUser(userId);

        if (null == user) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        Url url = create(urlCreationRequest);
        url.setUser(user);

        url = urlRepository.save(url);

        return urlMapper.toUrlResponse(url);
    }

    public UrlResponse createWithCampaignIdAndUserId(
            String campaignId, String userId, UrlCreationRequest urlCreationRequest) throws NoSuchAlgorithmException {
        User user = getCorrectUser(userId);

        if (null == user) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        Campaign campaign = campaignRepository
                .findById(campaignId)
                .orElseThrow(() -> new AppException(ErrorCode.CAMPAIGN_NOTFOUND));

        // Handle the case when campaign is deleted
        if (campaign.getDeleted()) {
            throw new AppException(ErrorCode.CAMPAIGN_NOTFOUND);
        }

        Url url = create(urlCreationRequest);
        url.setUser(user);
        url.setCampaign(campaign);
        url.setExpiresAt(campaign.getEndDate());

        url = urlRepository.save(url);

        return urlMapper.toUrlResponse(url);
    }

    public PageResponse<UrlResponse> getAll(int page, int size, String type) {
        Pageable pageable = PageRequest.of(page - 1, size);

        Page<Url> urls =
                switch (type) {
                    case "not_deleted" -> urlRepository.findAllByDeletedIs(false, pageable);
                    case "deleted" -> urlRepository.findAllByDeletedIs(true, pageable);
                    default -> urlRepository.findAll(pageable);
                };

        List<UrlResponse> urlResponseList = urls.getContent().stream()
                .map(url -> {
                    UrlResponse urlResponse = urlMapper.toUrlResponse(url);
                    urlResponse.setClickCount(clickRepository.countByUrlId(url.getId()));
                    return urlResponse;
                })
                .toList();

        return PageResponse.<UrlResponse>builder()
                .items(urlResponseList)
                .page(page)
                .records(urls.getTotalElements())
                .totalPages(urls.getTotalPages())
                .build();
    }

    public PageResponse<UrlResponse> getAllByUserId(String userId, int page, int size, String type) {
        User user = getCorrectUser(userId);

        if (null == user) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        Pageable pageable = PageRequest.of(page - 1, size);

        Page<Url> urls =
                switch (type) {
                    case ("not_deleted") -> urlRepository.findAllByUserIdAndDeletedIs(userId, false, pageable);
                    case ("deleted") -> urlRepository.findAllByUserIdAndDeletedIs(userId, true, pageable);
                    default -> urlRepository.findAllByUserId(userId, pageable);
                };

        List<UrlResponse> urlResponseList = urls.getContent().stream()
                .map((url) -> {
                    UrlResponse urlResponse = urlMapper.toUrlResponse(url);
                    urlResponse.setUserId(null);
                    urlResponse.setCampaignId(null);

                    user.getRoles().forEach(role -> {
                        if (role.getName().equals("MANAGER") || role.getName().equals("ADMIN")) {
                            urlResponse.setClickCount(clickRepository.countByUrlId(url.getId()));
                        }
                    });

                    return urlResponse;
                })
                .toList();

        return PageResponse.<UrlResponse>builder()
                .items(urlResponseList)
                .page(page)
                .records(urls.getTotalElements())
                .totalPages(urls.getTotalPages())
                .build();
    }

    public PageResponse<UrlResponse> getAllByCampaignIdAndUserId(
            String campaignId, String userId, int page, int size, String type) {
        User user = getCorrectUser(userId);

        if (null == user) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        Pageable pageable = PageRequest.of(page - 1, size);

        Page<Url> urls =
                switch (type) {
                    case ("not_deleted") -> urlRepository.findAllByCampaignIdAndUserIdAndDeletedIs(
                            campaignId, userId, false, pageable);
                    case ("deleted") -> urlRepository.findAllByCampaignIdAndUserIdAndDeletedIs(
                            campaignId, userId, true, pageable);
                    default -> urlRepository.findAllByCampaignIdAndUserId(campaignId, userId, pageable);
                };

        List<UrlResponse> urlResponseList = urls.getContent().stream()
                .map((url) -> {
                    UrlResponse urlResponse = urlMapper.toUrlResponse(url);
                    urlResponse.setCampaignId(null);
                    urlResponse.setUserId(null);
                    urlResponse.setClickCount(clickRepository.countByUrlId(url.getId()));

                    return urlResponse;
                })
                .toList();

        return PageResponse.<UrlResponse>builder()
                .items(urlResponseList)
                .page(page)
                .records(urls.getTotalElements())
                .totalPages(urls.getTotalPages())
                .build();
    }

    public UrlResponse getOne(String hash) {
        Url url = getUrlByHash(hash);

        UrlResponse urlResponse = urlMapper.toUrlResponse(url);
        urlResponse.setClickCount(clickRepository.countByUrlId(url.getId()));

        return urlResponse;
    }

    public UrlResponse getOneByHashAndUserId(String hash, String userId) {
        User user = getCorrectUser(userId);

        if (null == user) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        Url url = urlRepository
                .findByHashAndUserId(hash, userId)
                .orElseThrow(() -> new AppException(ErrorCode.URL_NOTFOUND));

        UrlResponse urlResponse = urlMapper.toUrlResponse(url);

        // set these 2 to null because we all know its user id and campaign id in the request already
        urlResponse.setUserId(null);
        urlResponse.setCampaignId(null);

        user.getRoles().forEach(role -> {
            if (role.getName().equals("MANAGER") || role.getName().equals("ADMIN")) {
                urlResponse.setClickCount(clickRepository.countByUrlId(url.getId()));
            }
        });

        return urlResponse;
    }

    public UrlResponse getOneByIdAndCampaignIdAndUserId(String hash, String campaignId, String userId) {
        User user = getCorrectUser(userId);

        if (null == user) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if (!campaignRepository.existsByIdAndUserId(campaignId, userId)) {
            throw new AppException(ErrorCode.CAMPAIGN_NOTFOUND);
        }

        Url url = urlRepository
                .findByHashAndCampaignIdAndUserId(hash, campaignId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.URL_NOTFOUND));

        UrlResponse urlResponse = urlMapper.toUrlResponse(url);

        urlResponse.setUserId(null);
        urlResponse.setCampaignId(null);
        urlResponse.setClickCount(clickRepository.countByUrlId(url.getId()));

        return urlResponse;
    }

    public UrlResponse update(String hash, UrlUpdateRequest urlUpdateRequest) {
        Url url = getUrlByHash(hash);

        // set new alias to it
        url.setHash(urlUpdateRequest.getAlias());

        return urlMapper.toUrlResponse(urlRepository.save(url));
    }

    public UrlResponse updateOneByHashAndUserId(String hash, String userId, UrlUpdateRequest urlUpdateRequest) {
        User user = getCorrectUser(userId);

        if (null == user) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        Url url = urlRepository
                .findByHashAndUserId(hash, userId)
                .orElseThrow(() -> new AppException(ErrorCode.URL_NOTFOUND));

        urlMapper.updateUrl(url, urlUpdateRequest);
        url.setHash(urlUpdateRequest.getAlias());

        url = urlRepository.save(url);

        // set these 2 to null because we all know its user id and campaign id in the request already
        url.setUser(null);
        url.setCampaign(null);

        return urlMapper.toUrlResponse(url);
    }

    public UrlResponse updateOneByHashAndCampaignIdAndUserId(
            String hash, String campaignId, String userId, UrlUpdateRequest urlUpdateRequest) {
        User user = getCorrectUser(userId);

        if (null == user) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if (!campaignRepository.existsByIdAndUserId(campaignId, userId)) {
            throw new AppException(ErrorCode.CAMPAIGN_NOTFOUND);
        }

        Url url = urlRepository
                .findByHashAndCampaignIdAndUserId(hash, campaignId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.URL_NOTFOUND));

        urlMapper.updateUrl(url, urlUpdateRequest);
        url.setHash(urlUpdateRequest.getAlias());

        url = urlRepository.save(url);

        // set these 2 to null because we all know its user id and campaign id in the request already
        url.setUser(null);
        url.setCampaign(null);

        return urlMapper.toUrlResponse(url);
    }

    public void delete(String hash) {
        Url url = getUrlByHash(hash);
        url.setDeleted(true);

        urlRepository.save(url);
    }

    public void deleteOneByHashAndUserId(String hash, String userId) {
        User user = getCorrectUser(userId);

        if (null == user) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        Url url = urlRepository
                .findByHashAndUserId(hash, userId)
                .orElseThrow(() -> new AppException(ErrorCode.URL_NOTFOUND));

        // Soft delete the url
        url.setDeleted(true);
        urlRepository.save(url);
    }

    public void deleteOneByHashAndCampaignIdAndUserId(String hash, String campaignId, String userId) {
        User user = getCorrectUser(userId);

        if (null == user) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if (!campaignRepository.existsByIdAndUserId(campaignId, userId)) {
            throw new AppException(ErrorCode.CAMPAIGN_NOTFOUND);
        }

        // Soft delete the url
        Url url = urlRepository
                .findByHashAndCampaignIdAndUserId(hash, campaignId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.URL_NOTFOUND));
        url.setDeleted(true);

        urlRepository.save(url);
    }

    private Url getUrlByHash(String hash) {
        return urlRepository.findByHash(hash).orElseThrow(() -> new AppException(ErrorCode.URL_NOTFOUND));
    }

    private String generateHash(String longUrl) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(longUrl.getBytes());

        // Extract the first 6 bytes (12 hex characters)
        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            String hex = Integer.toHexString(0xFF & digest[i]);

            if (hex.length() == 1) {
                hexString.append('0');
            }

            hexString.append(hex);
        }

        String first6BytesHex = hexString.toString();

        // Convert hex to decimal
        long decimal = new BigInteger(first6BytesHex, 16).longValue();

        // Encode decimal to Base62
        String base62 = base62Encoder.encode(decimal);

        // Ensure the short Url meets the desired length
        if (base62.length() < hashLength) {
            base62 = String.format("%" + hashLength + "s", base62).replace(' ', '0');
        } else if (base62.length() > hashLength) {
            base62 = base62.substring(0, hashLength);
        }

        return base62;
    }

    private Url create(UrlCreationRequest urlCreationRequest) throws NoSuchAlgorithmException {
        Url url = urlMapper.toUrl(urlCreationRequest);

        String alias;

        // If the user want to custom the alias
        if (urlCreationRequest.getAlias() != null
                && !urlCreationRequest.getAlias().trim().isEmpty()) {
            alias = urlCreationRequest.getAlias().trim();

            if (urlRepository.existsByHash(alias)) {
                throw new AppException(ErrorCode.ALIAS_EXISTED);
            }
        } else {
            alias = generateHash(url.getLongUrl().trim());

            // Handle potential collisions
            while (urlRepository.existsByHash(alias)) {
                alias = generateHash(alias + getSaltString());
            }
        }

        url.setHash(alias);
        url.setCreatedAt(Date.from(Instant.now()));
        url.setExpiresAt(Date.from(Instant.now().plus(expirationTime, ChronoUnit.DAYS)));

        return url;
    }

    private String getSaltString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 56) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        return salt.toString();
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

    public Map<Object, Object> getMostClickedUrlsByCampaign(String userId, String campaignId) {
        User user = getCorrectUser(userId);

        if (null == user) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if (!campaignRepository.existsByIdAndUserId(campaignId, userId)) {
            throw new AppException(ErrorCode.CAMPAIGN_NOTFOUND);
        }

        List<Object[]> result = urlRepository.findMostClickedUrlsByCampaignIdAndUserId(campaignId, userId);

        int totalLinks = result.size();
        int totalClickCount = 0;

        Map<Object, Object> response = new HashMap<>();

        // Convert result to a more readable format
        List<Map<String, Object>> responseUrls = new ArrayList<>();
        for (Object[] row : result) {
            Url url = (Url) row[0];
            Long clickCount = (Long) row[1];

            Map<String, Object> data = new HashMap<>();
            data.put("url", urlMapper.toUrlResponse(url));
            data.put("clickCount", clickCount);

            totalClickCount += clickCount;

            responseUrls.add(data);
        }

        response.put("totalClickCount", totalClickCount);
        response.put("totalShortenedLinks", totalLinks);
        response.put("urls", responseUrls);

        return response;
    }

    public List<UrlResponse> searchForUrl(String q) {
        Pageable pageable = PageRequest.of(0, 20);
        List<Url> urls = urlRepository.searchUrls(q, pageable);

        return urls.stream().map(urlMapper::toUrlResponse).toList();
    }

    public List<UrlResponse> searchUrlsWithinUserId(String userId, String q) {
        User user = getCorrectUser(userId);

        if (null == user) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        Pageable pageable = PageRequest.of(0, 20);
        List<Url> urls = urlRepository.searchUrlsWithinUserId(userId, q, pageable);
        return urls.stream().map(urlMapper::toUrlResponse).toList();
    }

    public List<UrlResponse> searchUrlsWithinUserIdAndCampaignId(String userId, String campaignId, String q) {
        User user = getCorrectUser(userId);

        if (null == user) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if (!campaignRepository.existsByIdAndUserId(campaignId, userId)) {
            throw new AppException(ErrorCode.CAMPAIGN_NOTFOUND);
        }

        Pageable pageable = PageRequest.of(0, 20);
        List<Url> urls = urlRepository.searchUrlsWithinUserIdAndCampaignId(userId, campaignId, q, pageable);
        return urls.stream().map(urlMapper::toUrlResponse).toList();
    }

    public PageResponse<ClickResponse> getClicks(String userId, String campaignId, String hash, Pageable pageable) {
        User user = getCorrectUser(userId);

        if (null == user) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if (!campaignRepository.existsByIdAndUserId(campaignId, userId)) {
            throw new AppException(ErrorCode.CAMPAIGN_NOTFOUND);
        }

        if (!urlRepository.existsByHashAndCampaignIdAndUserId(hash, campaignId, userId)) {
            throw new AppException(ErrorCode.URL_NOTFOUND);
        }

        Page<Click> clicks = clickRepository.findAllByHashAndCampaignIdAndUserId(hash, campaignId, userId, pageable);

        List<ClickResponse> clickResponses =
                clicks.getContent().stream().map(clickMapper::toResponse).toList();

        return PageResponse.<ClickResponse>builder()
                .items(clickResponses)
                .page(pageable.getPageNumber() + 1)
                .records(clicks.getTotalElements())
                .totalPages(clicks.getTotalPages())
                .build();
    }
}
