package com.healthcare.messaging_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

public class MessageDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class SendSmsRequest {
        @NotBlank private String to;
        @NotBlank private String body;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class SendSmsResponse {
        private String messageSid;
        private String status;
    }
}
