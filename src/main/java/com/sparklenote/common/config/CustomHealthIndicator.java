package com.sparklenote.common.config;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

@Component
public class CustomHealthIndicator extends AbstractHealthIndicator {

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        try {
            // 정상적으로 동작할 때
            builder.up()
                    .withDetail("app", "정상적으로 동작합니다.")
                    .withDetail("error", "없습니다.");
        } catch (Exception e) {
            // 예외 발생 시
            builder.down()
                    .withDetail("app", "이슈가 발생했습니다.")
                    .withDetail("error", e.getMessage());  // 예외 메시지를 추가
        }
    }
}
