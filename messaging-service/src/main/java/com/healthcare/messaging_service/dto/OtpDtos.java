package com.healthcare.messaging_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

public class OtpDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class SendOtpRequest {
        @NotBlank private String phone;
        private String purpose; // eg: LOGIN, BOOKING_VERIFY
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class VerifyOtpRequest {
        @NotBlank private String phone;
        @NotBlank private String otp;
        private String purpose;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class VerifyOtpResponse {
        private boolean success;
        private String message;
    }
}
