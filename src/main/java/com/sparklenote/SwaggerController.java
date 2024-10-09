package com.sparklenote;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/swagger")
@RestController
@Tag(name = "Swagger 적용 API", description = "컨트롤러에 대한 설명입니다.")
public class SwaggerController {

    @GetMapping("/hello")
    @Operation(summary = "Swagger 메시지 반환", description = "간단한 Swagger 메시지를 반환하는 API.")

    public String helloAPI() {
        return "Hello, Swagger!";
    }
}
