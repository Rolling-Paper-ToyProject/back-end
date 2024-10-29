package com.sparklenote.user.controller;

import com.sparklenote.common.response.SnResponse;
import com.sparklenote.user.dto.request.TokenRequestDTO;
import com.sparklenote.user.dto.response.TokenResponseDTO;
import com.sparklenote.user.dto.response.UserInfoResponseDTO;
import com.sparklenote.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.sparklenote.common.code.GlobalSuccessCode.SUCCESS;

@RequiredArgsConstructor
@RequestMapping("/user")
@RestController
@Tag(name = "User Controller", description = "소셜로그인, 토큰 관련 API")
public class UserController {

    private final UserService userService;

    @GetMapping("/my")
    public String myAPI() {

        return "my route";
    }

    @GetMapping("/")
    public String mainAPI() {

        return "main route";
    }

    @PostMapping("/tokenRefresh")
    @Operation(summary = "/user/tokenRefresh", description = "Refresh Token을 통해 Access Token을 재발급하는 메소드")
    public ResponseEntity<SnResponse<TokenResponseDTO>> refreshToken(@RequestHeader String refreshToken, HttpServletResponse response) {
        TokenResponseDTO newAccessToken = userService.refreshToken(refreshToken);
        response.addCookie(userService.createCookie("Authorization", newAccessToken.getAccessToken()));
        return ResponseEntity.status(SUCCESS.getStatus())
                .body(new SnResponse<>(SUCCESS, newAccessToken));
    }

    @GetMapping("/info")
    @Operation(summary = "/user/info", description = "userId와 name을 클라이언트로 전달")
    public ResponseEntity<SnResponse<UserInfoResponseDTO>> info(@RequestHeader("Authorization") String token) {
        UserInfoResponseDTO userInfo = userService.getUserInfo(token);
        return ResponseEntity.status(SUCCESS.getStatus())
                .body(new SnResponse<>(SUCCESS, userInfo));

    }
}