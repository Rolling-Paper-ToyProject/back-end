package com.sparklenote.user.oAuth2;

import com.sparklenote.domain.entity.User;
import com.sparklenote.domain.enumType.Role;
import com.sparklenote.domain.enumType.SocialType;
import com.sparklenote.domain.repository.UserRepository;
import com.sparklenote.user.dto.request.UserRequestDTO;
import com.sparklenote.user.dto.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response;

        SocialType socialType;
        if (registrationId.equals("naver")) {
            oAuth2Response = new NaverResponse(oAuth2User.getAttributes());
            socialType = SocialType.NAVER;
        }
        else if (registrationId.equals("kakao")) {
            oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());
            socialType = SocialType.KAKAO;
        }
        else {

            return null;
        }

        String username = oAuth2Response.getProvider()+" "+oAuth2Response.getProviderId();
        Optional<User> optionalUser = userRepository.findByUsername(username);


        if (optionalUser.isEmpty()) {

            // 기존 회원이 없으면 DB에 저장
            UserRequestDTO userRequestDTO = UserRequestDTO.builder()
                    .username(username)
                    .email(oAuth2Response.getEmail())
                    .name(oAuth2Response.getName())
                    .role(Role.TEACHER)
                    .socialType(socialType)
                    .build();

            // DTO를 User 객체로 변환
            User user = userRequestDTO.toEntity();
            userRepository.save(user);

            // CustomOAuth2User 객체로 변환해서 return
            UserResponseDTO userResponseDTO = UserResponseDTO.builder()
                    .username(username)
                    .email(oAuth2Response.getEmail())
                    .name(oAuth2Response.getName())
                    .role(Role.TEACHER)
                    .socialType(socialType)
                    .build();

            return new CustomOAuth2User(userResponseDTO);
        }
        else {
            // 이미 회원이 존재하는 경우
            User existUser = optionalUser.get();

            existUser.updateFromDTO(new UserRequestDTO(oAuth2Response.getEmail(), oAuth2Response.getName()));

            userRepository.save(existUser); // 기존 엔티티 업데이트

            UserResponseDTO userResponseDTO = new UserResponseDTO(username, oAuth2Response.getName(), oAuth2Response.getEmail(), Role.TEACHER, socialType);
            return new CustomOAuth2User(userResponseDTO);
        }
    }
}