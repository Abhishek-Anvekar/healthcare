package com.healthcare.auth_service.service;

import com.healthcare.auth_service.dto.AuthDtos.DoctorDomain;
import com.healthcare.auth_service.dto.AuthDtos.PatientDomain;
import com.healthcare.auth_service.dto.AuthDtos.AdminDomain;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
public class ExternalDomainClient {

    private final WebClient doctorClient;
    private final WebClient patientClient;
    private final WebClient adminClient;

    public ExternalDomainClient(WebClient doctorClient, WebClient patientClient, WebClient adminClient) {
        this.doctorClient = doctorClient;
        this.patientClient = patientClient;
        this.adminClient = adminClient;
    }

    // register doctor -> returns domain id (string)
    public String registerDoctor(DoctorDomain req) {
        Map resp = doctorClient.post()
                .uri("/doctors/register")
                .bodyValue(req)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        // assume doctor-service returns { "id": "<uuid>", ...}
        return resp != null ? String.valueOf(resp.get("id")) : null;
    }

    // register patient -> returns domain id (string)
    public String registerPatient(PatientDomain req) {
        Map resp = patientClient.post().uri("/patients")
                .bodyValue(req).retrieve().bodyToMono(Map.class).block();
        return resp != null ? String.valueOf(resp.get("id")) : null;
    }

    // register patient -> returns domain id (string)
    public String registerAdmin(AdminDomain req) {
        Map resp = adminClient.post().uri("/admins")
                .bodyValue(req).retrieve().bodyToMono(Map.class).block();
        return resp != null ? String.valueOf(resp.get("id")) : null;
    }

    // fetch domain profile by role+domainId
    public Map<String,Object> fetchDoctorProfile(String doctorId){
        return doctorClient.get().uri("/doctors/{id}", doctorId)
                .retrieve().bodyToMono(new ParameterizedTypeReference<Map<String,Object>>(){}).block();
    }

    public Map<String,Object> fetchPatientProfile(String patientId){
        return patientClient.get().uri("/patients/{id}", patientId)
                .retrieve().bodyToMono(new ParameterizedTypeReference<Map<String,Object>>(){}).block();
    }

    public Map<String,Object> fetchAdminProfile(String adminId){
        return adminClient.get().uri("/admins/{id}", adminId)
                .retrieve().bodyToMono(new ParameterizedTypeReference<Map<String,Object>>(){}).block();
    }
}

