package com.sparklenote.domain.enumType;

public enum Role {
    TEACHER, STUDENT;

    public String getAuthority() {
        return name();  // "TEACHER", "STUDENT" 등 Enum 값 자체를 반환
    }
}
