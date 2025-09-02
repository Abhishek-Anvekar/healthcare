package com.healthcare.auth_service.service;

import com.healthcare.auth_service.dto.AuthDtos.*;
import com.healthcare.auth_service.entity.RefreshToken;
import com.healthcare.auth_service.entity.Role;
import com.healthcare.auth_service.entity.User;
import com.healthcare.auth_service.exception.BadRequestException;
import com.healthcare.auth_service.exception.NotFoundException;
import com.healthcare.auth_service.repository.RefreshTokenRepository;
import com.healthcare.auth_service.repository.UserRepository;
import com.healthcare.auth_service.security.JwtHelper;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepo;
    private final PasswordEncoder passwordEncoder;
    private final ExternalDomainClient external;
    private final JwtHelper jwtHelper;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final ModelMapper modelMapper;


    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepo,
                       PasswordEncoder passwordEncoder,
                       ExternalDomainClient external,
                       JwtHelper jwtHelper, AuthenticationManager authenticationManager, UserDetailsService userDetailsService, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.refreshTokenRepo = refreshTokenRepo;
        this.passwordEncoder = passwordEncoder;
        this.external = external;
        this.jwtHelper = jwtHelper;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.modelMapper = modelMapper;
    }

    @Transactional
    public RegisterResponse registerDoctor(RegisterDoctorRequest req){
        if (userRepository.existsByEmail(req.getEmail())) throw new BadRequestException("Email already registered");
        // Create domain first
        String doctorId = external.registerDoctor(req.getDoctor());
        if (doctorId == null) throw new BadRequestException("Failed to create doctor domain");

        // create auth user
        User u = User.builder()
                .domainId(doctorId)
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(Role.ROLE_DOCTOR)
                .build();
        userRepository.save(u);
        return new RegisterResponse("Doctor registered", u.getId(), doctorId);
    }

    @Transactional
    public RegisterResponse registerPatient(RegisterPatientRequest req){
        if (userRepository.existsByEmail(req.getEmail())) throw new BadRequestException("Email already registered");
        String patientId = external.registerPatient(req.getPatient());
        if (patientId == null) throw new BadRequestException("Failed to create patient domain");


        User u = User.builder()
                .domainId(patientId)
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(Role.ROLE_PATIENT)
                .build();
        userRepository.save(u);
        return new RegisterResponse("Patient registered", u.getId(), patientId);
    }

    @Transactional
    public RegisterResponse registerAdmin(RegisterAdminRequest req){
        if (userRepository.existsByEmail(req.getEmail())) throw new BadRequestException("Email already registered");
        String adminId = external.registerAdmin(req.getAdmin());
        if (adminId == null) throw new BadRequestException("Failed to create admin domain");


        User u = User.builder()
                .domainId(adminId)
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(Role.ROLE_ADMIN)
                .build();
        userRepository.save(u);
        return new RegisterResponse("Admin registered", u.getId(), adminId);
    }


    @Transactional
    public LoginResponse login(LoginRequest req){
        User u = userRepository.findByEmail(req.getEmail()).orElseThrow(() -> new NotFoundException("Invalid credentials"));
        this.doAuthenticate(req.getEmail(),req.getPassword());
        UserDetails userDetails = userDetailsService.loadUserByUsername(req.getEmail());
        String token = jwtHelper.generateToken(userDetails);
        String refresh = jwtHelper.createRefreshToken(userDetails);// if we pass userdetails then we can set roles in refreshToken, so thet if jwt token expired we can use refresh token at API Gateway side
//        String refresh = jwtHelper.createRefreshToken(u.getId());

        // persist refresh
        RefreshToken rt = RefreshToken.builder()
                .userId(u.getId())
                .token(refresh)
                .expiresAt(OffsetDateTime.now().plusSeconds( jwtHelper.refreshTtlSec() )) // need getter; we will add a helper
                .build();
        refreshTokenRepo.save(rt);

        return new LoginResponse(token, refresh, "Bearer", jwtHelper.accessTtlSec());
    }

    private void doAuthenticate(String email, String password) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email,password);
        try {
            authenticationManager.authenticate(authenticationToken);
        }catch (BadCredentialsException e){
            throw new BadRequestException("Invalid Username or Password !!");
        }
    }

    @Transactional
    public RefreshResponse refresh(RefreshRequest req){
        RefreshToken token = refreshTokenRepo.findByToken(req.getRefreshToken())
                .orElseThrow(() -> new NotFoundException("Refresh token not found"));
        if (token.isRevoked() || token.getExpiresAt().isBefore(OffsetDateTime.now()))
            throw new BadRequestException("Refresh token expired or revoked");

        String userId = jwtHelper.getAllClaimsFromToken(req.getRefreshToken()).getSubject();
        User u = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(u.getEmail());
        String jwtToken = jwtHelper.generateToken(userDetails);
        return new RefreshResponse(jwtToken, jwtHelper.accessTtlSec());
    }

    public ProfileResponse profile(String email){
        User u = modelMapper.map(userDetailsService.loadUserByUsername(email),User.class);
        Map<String,Object> domainProfile = null;
        if (u.getRole() == Role.ROLE_DOCTOR) {
            domainProfile = external.fetchDoctorProfile(u.getDomainId());
        } else if (u.getRole() == Role.ROLE_PATIENT){
            domainProfile = external.fetchPatientProfile(u.getDomainId());
        } else if (u.getRole() == Role.ROLE_ADMIN){
            domainProfile = external.fetchAdminProfile(u.getDomainId());
        }
        return new ProfileResponse(u.getId(), u.getEmail(), u.getRole().name(), domainProfile);
    }

}

