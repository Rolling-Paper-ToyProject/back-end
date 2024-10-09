package com.sparklenote.user.controller;

import com.sparklenote.common.response.SnResponse;
import com.sparklenote.user.dto.request.TokenRequestDTO;
import com.sparklenote.user.dto.response.BlacklistResponseDTO;
import com.sparklenote.user.dto.response.TokenResponseDTO;
import com.sparklenote.user.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.sparklenote.common.code.GlobalSuccessCode.SUCCESS;

@RequiredArgsConstructor
@RequestMapping("/user")
@RestController
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
    public ResponseEntity<SnResponse<TokenResponseDTO>> refreshToken(@RequestBody TokenRequestDTO tokenRequestDTO, HttpServletResponse response) {
        TokenResponseDTO newAccessToken = userService.refreshToken(tokenRequestDTO);
        response.addCookie(userService.createCookie("Authorization", newAccessToken.getAccessToken()));
        return ResponseEntity.status(SUCCESS.getStatus())
                .body(new SnResponse<>(SUCCESS, newAccessToken));
    }
}