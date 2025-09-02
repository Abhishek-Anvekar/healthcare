package com.healthcare.messaging_service.repository;

import com.healthcare.messaging_service.entity.OtpRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<OtpRecord, Long> {
    Optional<OtpRecord> findTopByPhoneAndPurposeOrderByCreatedAtDesc(String phone, String purpose);
}
