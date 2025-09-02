package com.healthcare.messaging_service.controller;

import com.healthcare.messaging_service.dto.MessageDtos.*;
import com.healthcare.messaging_service.dto.OtpDtos.*;
import com.healthcare.messaging_service.service.MessagingService;
import com.healthcare.messaging_service.service.OtpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/messaging")
@Tag(name = "Messaging APIs", description = "APIs to send SMS and manage OTP")
public class MessagingController {

    private final MessagingService messagingService;
    private final OtpService otpService;

    public MessagingController(MessagingService messagingService, OtpService otpService){
        this.messagingService = messagingService;
        this.otpService = otpService;
    }

    @Operation(summary = "Send SMS (internal/admin usage)",
            description = "Sends plain SMS to a phone. ROLE checks should be enforced at API Gateway.")
    @PostMapping("/send-sms")
    public ResponseEntity<SendSmsResponse> sendSms(@Valid @RequestBody SendSmsRequest req){
        // Role check: ADMIN / SYSTEM (enforced at Gateway)
        String sid = messagingService.sendSms(req.getTo(), req.getBody(), "MANUAL");
        return ResponseEntity.ok(new SendSmsResponse(sid, "SENT"));
    }

    @Operation(summary = "Send OTP to phone (for login/verification)")
    @PostMapping("/otp/send")
    public ResponseEntity<?> sendOtp(@Valid @RequestBody SendOtpRequest req){
        // No role required - public API
        otpService.generateAndSendOtp(req.getPhone(), req.getPurpose());
        return ResponseEntity.ok(Map.of("sent", true));
    }

    @Operation(summary = "Verify OTP")
    @PostMapping("/otp/verify")
    public ResponseEntity<VerifyOtpResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest req){
        boolean ok = otpService.verifyOtp(req.getPhone(), req.getOtp(), req.getPurpose());
        return ResponseEntity.ok(new VerifyOtpResponse(ok, ok ? "Verified" : "Invalid"));
    }
}
