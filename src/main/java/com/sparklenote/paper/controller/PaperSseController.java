package com.sparklenote.paper.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/paper")
@RequiredArgsConstructor
public class PaperSseController {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    /**
     * 클라이언트가 SSE 구독을 시작할 때 호출되는 엔드포인트
     * @memo : emitter List 안에 클라이언트가 들어가면 바뀐 이벤트 정보를 수신하고 실시간 UI 반영
     */

    @GetMapping("/sse")
    public SseEmitter subscribeToPaperEvents() {
        SseEmitter emitter = new SseEmitter(60 * 1000L); // 1분 동안 유지
        emitters.add(emitter); // emitters 리스트에 추가

        emitter.onCompletion(() -> emitters.remove(emitter)); // 오류나면 emitter 제거
        emitter.onTimeout(() -> emitters.remove(emitter)); // 만료되면 emitter 제거

        try {
            // 처음 연결 시 초기화 데이터 전송
            emitter.send(SseEmitter.event().name("INIT").data("SSE 연결이 시작되었습니다."));
        } catch (Exception e) {
            emitter.completeWithError(e);  // 연결 오류 시 처리
        }
        return emitter;
    }
}
