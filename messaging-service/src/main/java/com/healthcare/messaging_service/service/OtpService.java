package com.healthcare.messaging_service.service;

import com.healthcare.messaging_service.entity.OtpRecord;
import com.healthcare.messaging_service.exception.BadRequestException;
import com.healthcare.messaging_service.repository.OtpRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class OtpService {

    private final OtpRepository otpRepo;
    private final MessagingService messagingService;
    private final Random random = new Random();

    // configuration
    private final int otpLength = 6;
    private final int expiryMinutes = 5;
    private final int maxAttempts = 5;

    public OtpService(OtpRepository otpRepo, MessagingService messagingService){
        this.otpRepo = otpRepo;
        this.messagingService = messagingService;
    }

    @Transactional
    public String generateAndSendOtp(String phone, String purpose){
        String otp = String.format("%0" + otpLength + "d", random.nextInt((int)Math.pow(10, otpLength)));
        OtpRecord rec = OtpRecord.builder()
                .phone(phone)
                .otp(otp)
                .purpose(purpose == null ? "GENERAL" : purpose)
                .attempts(0)
                .verified(false)
                .createdAt(OffsetDateTime.now())
                .expiresAt(OffsetDateTime.now().plusMinutes(expiryMinutes))
                .build();
        otpRepo.save(rec);

        // send via messaging service
        String body = "Your verification code is: " + otp + ". It will expire in " + expiryMinutes + " minutes.";
        messagingService.sendSms(phone, body, "OTP");
        return rec.getOtp(); // returning OTP is useful in tests, but in prod you wouldn't return it; controller returns 200 only.
    }

    @Transactional
    public boolean verifyOtp(String phone, String otp, String purpose){
        Optional<OtpRecord> opt = otpRepo.findTopByPhoneAndPurposeOrderByCreatedAtDesc(phone, purpose == null ? "GENERAL" : purpose);
        if (opt.isEmpty()) throw new BadRequestException("No OTP found for phone/purpose");
        OtpRecord rec = opt.get();
        if (rec.isVerified()) return true;
        if (rec.getExpiresAt().isBefore(OffsetDateTime.now())) throw new BadRequestException("OTP expired");
        if (rec.getAttempts() >= maxAttempts) throw new BadRequestException("Too many attempts");
        rec.setAttempts(rec.getAttempts() + 1);
        if (rec.getOtp().equals(otp)) {
            rec.setVerified(true);
            otpRepo.save(rec);
            return true;
        } else {
            otpRepo.save(rec);
            throw new BadRequestException("Invalid OTP");
        }
    }
}
