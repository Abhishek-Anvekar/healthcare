package com.healthcare.auth_service.controller;

import com.healthcare.auth_service.dto.AuthDtos.*;
import com.healthcare.auth_service.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;


@RestController
@RequestMapping("/auth")
@Tag(name = "Auth APIs", description = "Registration, login, refresh and profile")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) { this.authService = authService; }

    @Operation(summary = "Register doctor (orchestrated)")
    @PostMapping("/register/doctor")
    public ResponseEntity<RegisterResponse> registerDoctor(@Valid @RequestBody RegisterDoctorRequest req){
        // role checks (if any) — open for public registration; gateway can block if needed
        return ResponseEntity.ok(authService.registerDoctor(req));
    }

    @Operation(summary = "Register patient (orchestrated)")
    @PostMapping("/register/patient")
    public ResponseEntity<RegisterResponse> registerPatient(@Valid @RequestBody RegisterPatientRequest req){
        return ResponseEntity.ok(authService.registerPatient(req));
    }

    @Operation(summary = "Register admin (orchestrated)")
    @PostMapping("/register/admin")
    public ResponseEntity<RegisterResponse> registerAdmin(@Valid @RequestBody RegisterAdminRequest req){
        return ResponseEntity.ok(authService.registerAdmin(req));
    }

    @Operation(summary = "Login")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req){
        return ResponseEntity.ok(authService.login(req));
    }

    @Operation(summary = "Refresh access token")
    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(@Valid @RequestBody RefreshRequest req){
        return ResponseEntity.ok(authService.refresh(req));
    }

    @Operation(summary = "Profile - get my profile (user must be authenticated)")
    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> me(Principal auth){
        String email = auth == null ? null : String.valueOf(auth.getName());
        if (email == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(authService.profile(email));
    }

}

