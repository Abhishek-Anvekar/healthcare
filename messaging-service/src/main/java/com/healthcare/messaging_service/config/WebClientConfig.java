package com.healthcare.messaging_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient twilioClient(@Value("${twilio.account-sid}") String accountSid,
                                  @Value("${twilio.auth-token}") String authToken) {
        return WebClient.builder()
                .defaultHeaders(h -> h.setBasicAuth(accountSid, authToken))
                .baseUrl("https://api.twilio.com/2010-04-01")
                .build();
    }

    @Bean
    public WebClient appointmentClient(@Value("${external.appointment-service-url}") String base) {
        return WebClient.builder().baseUrl(base).build();
    }

    @Bean
    public WebClient doctorClient(@Value("${external.doctor-service-url}") String base) {
        return WebClient.builder().baseUrl(base).build();
    }

    @Bean
    public WebClient patientClient(@Value("${external.patient-service-url}") String base) {
        return WebClient.builder().baseUrl(base).build();
    }
}