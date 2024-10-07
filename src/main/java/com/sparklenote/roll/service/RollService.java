package com.sparklenote.roll.service;

import com.sparklenote.domain.entity.Roll;
import com.sparklenote.domain.entity.User;
import com.sparklenote.domain.repository.RollRepository;
import com.sparklenote.domain.repository.UserRepository;
import com.sparklenote.roll.dto.request.RollCreateRequestDto;
import com.sparklenote.roll.util.ClassCodeGenerator;
import com.sparklenote.roll.util.UrlGenerator;
import com.sparklenote.user.oAuth2.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RollService {

    private final RollRepository rollRepository;
    private final UserRepository userRepository;
    private final UrlGenerator urlGenerator;

    public Roll createRoll(RollCreateRequestDto createRequestDto) {
        int ClassCode = ClassCodeGenerator.generateClassCode();
        String Url = urlGenerator.generateUrl();


        // SecurityContextHolder에서 로그인된 사용자의 username 가져오기
        CustomOAuth2User customOAuth2User = (CustomOAuth2User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = customOAuth2User.getUsername();

        // username으로 User 조회
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // User 객체와 랜덤 클래스 코드 및 URL을 사용해 Roll 엔티티 생성
        Roll roll = Roll.fromRollCreateDto(createRequestDto, ClassCode, Url, user);

        // Roll 저장
        rollRepository.save(roll);

        // Roll 객체 반환
        return roll;
    }
}
