package com.healthcare.messaging_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "message_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MessageLog {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String toPhone;
    @Column(length = 2000)
    private String body;
    private String status;
    private String providerMessageId;
    private String eventType; // APPOINTMENT_CREATED, OTP, REMINDER, etc.

    private OffsetDateTime createdAt = OffsetDateTime.now();
}
