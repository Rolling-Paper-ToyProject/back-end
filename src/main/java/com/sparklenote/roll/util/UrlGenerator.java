package com.sparklenote.roll.util;

import com.sparklenote.domain.repository.RollRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UrlGenerator {

    private final RollRepository rollRepository;

    public String generateUrl() {
        String randomUrl;
        do {
            randomUrl = "www.sparklenote.com/roll/" + UUID.randomUUID().toString().substring(0, 8);
        } while (rollRepository.existsByUrl(randomUrl)); // URL 중복 체크
        return randomUrl;
    }
}