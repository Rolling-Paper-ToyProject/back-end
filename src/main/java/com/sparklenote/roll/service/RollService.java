package com.sparklenote.roll.service;

import com.sparklenote.domain.entity.Roll;
import com.sparklenote.domain.repository.RollRepository;
import com.sparklenote.roll.dto.request.RollCreateRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RollService {
    private final RollRepository rollRepository;

    public Roll createRoll(RollCreateRequestDto createRequestDto) {
        int randomClassCode = generateRandomClassCode();
        String randomUrl = generateRandomUrl();

        //팩토리 메서드를 통해 Roll 객체 생성 (학급 코드 & URl 설정)
        Roll roll = Roll.fromRollCreateDto(createRequestDto, randomClassCode, randomUrl);
        rollRepository.save(roll);
        return roll;
    }

    private int generateRandomClassCode() {
        return (int) (Math.random() * 9000) + 1000;
    }

    private String generateRandomUrl() {
        return UUID.randomUUID().toString();
    }
}