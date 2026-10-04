package com.eventvenue.dto;

import jakarta.validation.constraints.NotNull;

public class StudentRegistrationRequest {
    @NotNull
    private Long studentId;

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }
}
