package com.sparklenote.roll.util;

public class ClassCodeGenerator {

    // 랜덤한 클래스 코드를 생성하는 메서드
    public static int generateClassCode() {
        return (int) (Math.random() * 9000) + 1000; // 1000 ~ 9999 사이의 코드 생성
    }
}