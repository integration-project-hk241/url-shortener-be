package org.url.urlshortenerbe.services;

import java.time.Instant;
import java.util.Date;

import org.springframework.stereotype.Service;
import org.url.urlshortenerbe.entities.Click;
import org.url.urlshortenerbe.entities.Url;
import org.url.urlshortenerbe.exceptions.AppException;
import org.url.urlshortenerbe.exceptions.ErrorCode;
import org.url.urlshortenerbe.repositories.ClickRepository;
import org.url.urlshortenerbe.repositories.UrlRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedirectionService {
    private final ClickRepository clickRepository;
    private final UrlRepository urlRepository;

    public String getUrlAndCountClick(String hash, String referer, String userAgent) {
        Url url = urlRepository.findByHash(hash).orElseThrow(() -> new AppException(ErrorCode.URL_NOTFOUND));

        Click click = Click.builder()
                .clickedAt(Date.from(Instant.now()))
                .userAgent(userAgent)
                .platform(referer)
                .url(url)
                .build();

        clickRepository.save(click);

        return url.getLongUrl();
    }
}
