package com.healthcare.auth_service.dto;

import com.healthcare.auth_service.entity.Role;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

public class AuthDtos {

    // Common nested domain objects:
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class DoctorDomain {
        @NotBlank private String fullName;
        private String specialization;
        private String city;
        private Integer consultationFee;
        private String phone;
        private String email;
        private String licenseNumber;
        private List<String> clinicAddresses;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class PatientDomain {
        @NotBlank private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private Integer age;
        private String gender;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class AdminDomain {
        @NotBlank private String name;
        private String email;
        private String phone;
        private Integer age;
        private String gender;
    }

    // Register doctor request at auth-service (orchestrates)
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class RegisterDoctorRequest {
        @NotBlank private String email;
        @NotBlank private String password;
        private Role role; // ROLE_DOCTOR
        @NotNull private DoctorDomain doctor;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class RegisterPatientRequest {
        @NotBlank private String email;
        @NotBlank private String password;
        private Role role; // ROLE_PATIENT
        @NotNull private PatientDomain patient;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class RegisterAdminRequest {
        @NotBlank private String email;
        @NotBlank private String password;
        private Role role; // ROLE_ADMIN
        @NotNull private AdminDomain admin;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank private String email;
        @NotBlank private String password;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class LoginResponse {
        private String jwtToken;
        private String refreshToken;
        private String tokenType = "Bearer";
        private long expiresIn;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class RefreshRequest {
        @NotBlank private String refreshToken;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class RefreshResponse {
        private String jwtToken;
        private long expiresIn;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class RegisterResponse {
        private String message;
        private String userId;
        private String domainId;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class ProfileResponse {
        private String userId;
        private String email;
        private String role;
        private Object domainProfile; // Map returned from doctor/patient service
    }
}

