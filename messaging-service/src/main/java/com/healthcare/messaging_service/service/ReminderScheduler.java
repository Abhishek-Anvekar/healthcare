package com.healthcare.messaging_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.messaging_service.config.ReminderProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.*;
import java.util.List;
import java.util.Map;

/**
 * Periodically poll appointment-service for upcoming appointments and send reminders.
 * Scheduling is configurable via application.yml.
 */
@Component
public class ReminderScheduler {

    private final WebClient appointmentClient;
    private final MessagingService messagingService;
    private final ObjectMapper mapper = new ObjectMapper();
    private final ReminderProperties properties;

//    @Value("${messaging.reminder.reminders-minutes}")
//    private List<Integer> remindersMinutes;

    public ReminderScheduler(WebClient appointmentClient, MessagingService messagingService, ReminderProperties properties){
        this.appointmentClient = appointmentClient;
        this.messagingService = messagingService;
        this.properties = properties;
    }

    // cron configured in application.yml; here using every 5 minutes default
    @Scheduled(cron = "${messaging.reminder.cron:0 */5 * * * *}")
    public void checkAndSendReminders(){
        try {
            Instant now = Instant.now();
            // For each reminder offset, find appointments starting at (now + minutes)
            for (int minutes : properties.getRemindersMinutes()){
                Instant target = now.plus(Duration.ofMinutes(minutes));
                // Convert to ISO string
                String from = target.minusSeconds(30).toString();
                String to = target.plusSeconds(30).toString();
                // Expect appointment-service to have an endpoint to fetch appointments between two instants:
                // GET /appointments/search?from={from}&to={to}
                List<Map<String,Object>> appts = appointmentClient.get()
                        .uri(uriBuilder -> uriBuilder.path("/appointments/search")
                                .queryParam("from", from).queryParam("to", to).build())
                        .retrieve().bodyToMono(new ParameterizedTypeReference<List<Map<String,Object>>>(){})
                        .onErrorResume(e -> Mono.just(List.of()))
                        .block();

                if (appts == null) continue;
                for (Map<String,Object> a : appts){
                    String patientPhone = String.valueOf(a.get("patientPhone"));
                    String doctorName = String.valueOf(a.get("doctorName"));
                    String start = String.valueOf(a.get("startTime"));
                    String appointmentId = String.valueOf(a.get("id"));
                    if (patientPhone != null && !patientPhone.isBlank()) {
                        String msg = "Reminder: Your appointment with Dr. " + doctorName + " is at " + start + ". ID:" + appointmentId;
                        messagingService.sendSms(patientPhone, msg, "REMINDER");
                    }
                }
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
