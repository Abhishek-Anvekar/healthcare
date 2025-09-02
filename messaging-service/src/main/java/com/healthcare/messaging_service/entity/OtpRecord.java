package com.healthcare.messaging_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "otp_records", indexes = {
        @Index(name = "idx_phone_purpose", columnList = "phone,purpose")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OtpRecord {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String otp;

    @Column(nullable = false)
    private String purpose; // LOGIN, BOOKING_VERIFY, etc.

    private int attempts = 0;

    private boolean verified = false;

    private OffsetDateTime createdAt = OffsetDateTime.now();

    private OffsetDateTime expiresAt;
}
