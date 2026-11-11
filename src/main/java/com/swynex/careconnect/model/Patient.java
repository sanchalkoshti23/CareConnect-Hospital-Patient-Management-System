package com.swynex.careconnect.model;

import java.time.OffsetDateTime;

public record Patient(
        Long patientId,
        String fullName,
        Integer age,
        String gender,
        String phone,
        String email,
        String address,
        String bloodGroup,
        OffsetDateTime createdAt) {
}
