package com.healthcare.messaging_service.service;

import com.healthcare.messaging_service.entity.MessageLog;
import com.healthcare.messaging_service.repository.MessageLogRepository;
import com.healthcare.messaging_service.util.TemplateUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.OffsetDateTime;
import java.util.Map;

@Service
public class MessagingService {

    private final WebClient twilioClient;
    private final MessageLogRepository logRepo;
    private final String fromNumber;
    private final String accountSid;

    public MessagingService(WebClient twilioClient,
                            MessageLogRepository logRepo,
                            @Value("${twilio.from-number}") String fromNumber,
                            @Value("${twilio.account-sid}") String accountSid) {
        this.twilioClient = twilioClient;
        this.logRepo = logRepo;
        this.fromNumber = fromNumber;
        this.accountSid = accountSid;
    }

    /**
     * Send an SMS via Twilio REST API.
     * Returns provider message SID on success.
     */
    public String sendSms(String to, String body, String eventType){
        MessageLog log = MessageLog.builder().toPhone(to).body(body).eventType(eventType).createdAt(OffsetDateTime.now()).build();
        try {
            // Twilio requires POST to /Accounts/{AccountSid}/Messages.json with form data
            var resp = twilioClient.post()
                    .uri(uriBuilder -> uriBuilder.path("/Accounts/{sid}/Messages.json").build(accountSid))
                    .body(BodyInserters.fromFormData("To", to)
                            .with("From", fromNumber)
                            .with("Body", body))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            String sid = resp != null && resp.get("sid") != null ? String.valueOf(resp.get("sid")) : null;
            String status = resp != null && resp.get("status") != null ? String.valueOf(resp.get("status")) : "SENT";
            log.setProviderMessageId(sid); // sid nothing but unique messageSid for each message
            log.setStatus(status);
            logRepo.save(log);
            return sid;
        } catch (Exception ex){
            log.setStatus("FAILED: " + ex.getMessage());
            logRepo.save(log);
            throw new RuntimeException("Failed to send sms: " + ex.getMessage());
        }
    }

    /**
     * Templated send
     */
    public String sendTemplated(String to, String template, Map<String,String> vars, String eventType){
        String body = TemplateUtil.apply(template, vars);
        return sendSms(to, body, eventType);
    }
}
