package com.healthcare.messaging_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Consumes domain events from other services and sends messages accordingly.
 */
@Component
public class EventConsumer {

    private final ObjectMapper mapper = new ObjectMapper();
    private final MessagingService messagingService;

    @Value("${kafka.topics.appointmentBooked}")
    private String topicAppointmentBooked;

    @Value("${kafka.topics.appointmentConfirmed}")
    private String topicAppointmentConfirmed;

    @Value("${kafka.topics.appointmentCancelled}")
    private String topicAppointmentCancelled;

    @Value("${kafka.topics.slotUpdated}")
    private String topicSlotUpdated;

    public EventConsumer(MessagingService messagingService) {
        this.messagingService = messagingService;
    }

    @KafkaListener(topics = "${kafka.topics.appointmentBooked}", groupId = "messaging-service")
    public void onAppointmentBooked(String message){
        try {
            Map<String,Object> ev = mapper.readValue(message, Map.class);
            String appointmentId = String.valueOf(ev.get("bookingId") != null ? ev.get("bookingId") : ev.get("appointmentId"));
            String patientPhone = String.valueOf(ev.get("patientPhone") != null ? ev.get("patientPhone") : ev.get("patientMobile"));
            String doctorPhone = String.valueOf(ev.get("doctorPhone") != null ? ev.get("doctorPhone") : ev.get("doctorMobile"));
            String startTime = String.valueOf(ev.get("startTime"));
            // Basic template
            if (patientPhone != null && !patientPhone.isBlank()) {
                String body = "Your appointment is requested for " + startTime + ". Appointment ID: " + appointmentId;
                messagingService.sendSms(patientPhone, body, "APPOINTMENT_REQUESTED");
            }
            if (doctorPhone != null && !doctorPhone.isBlank()) {
                String body = "New appointment request at " + startTime + ". Appointment ID: " + appointmentId;
                messagingService.sendSms(doctorPhone, body, "APPOINTMENT_REQUESTED_DOCTOR");
            }
        } catch (Exception e){ e.printStackTrace(); }
    }

    @KafkaListener(topics = "${kafka.topics.appointmentConfirmed}", groupId = "messaging-service")
    public void onAppointmentConfirmed(String message){
//        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"+message);
        try {
            Map<String,Object> ev = mapper.readValue(message, Map.class);
            String appointmentId = String.valueOf(ev.get("appointmentId"));
            String patientPhone = String.valueOf(ev.get("patientPhone"));
            String doctorPhone = String.valueOf(ev.get("doctorPhone"));
            String startTime = String.valueOf(ev.get("startTime"));
            String doctorFullName = String.valueOf(ev.get("fullName"));
//            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"+ev);
            if (patientPhone != null && !patientPhone.isBlank())
                messagingService.sendSms(patientPhone, "Your appointment is confirmed with " + doctorFullName + " at " + startTime + ". APPOINTMENT ID: "+appointmentId, "APPOINTMENT_CONFIRMED");
            //NOTE - when you want to send sms to doctor then uncomment below code. currently we dont receiving doctors phone number in kafka message.
//            if (doctorPhone != null && !doctorPhone.isBlank())
//                messagingService.sendSms(doctorPhone, "Appointment confirmed at " + startTime + ". ID: "+appointmentId, "APPOINTMENT_CONFIRMED_DOC");
        } catch (Exception e){ e.printStackTrace(); }
    }

    @KafkaListener(topics = "${kafka.topics.appointmentCancelled}", groupId = "messaging-service")
    public void onAppointmentCancelled(String message){
        try {
            Map<String,Object> ev = mapper.readValue(message, Map.class);
            String appointmentId = String.valueOf(ev.get("appointmentId"));
            String patientPhone = String.valueOf(ev.get("patientPhone"));
            String doctorPhone = String.valueOf(ev.get("doctorPhone"));
            String startTime = String.valueOf(ev.get("startTime"));
            if (patientPhone != null && !patientPhone.isBlank())
                messagingService.sendSms(patientPhone, "Your appointment has been cancelled. ID: "+appointmentId, "APPOINTMENT_CANCELLED");
            if (doctorPhone != null && !doctorPhone.isBlank())
                messagingService.sendSms(doctorPhone, "Appointment cancelled. ID: "+appointmentId, "APPOINTMENT_CANCELLED_DOC");
        } catch (Exception e){ e.printStackTrace(); }
    }

    @KafkaListener(topics = "${kafka.topics.slotUpdated}", groupId = "messaging-service")
    public void onSlotUpdated(String message){
        try {
            Map<String,Object> ev = mapper.readValue(message, Map.class);
            // e.g. event contains doctorId, slotId, date, startTime, blocked
            String doctorId = String.valueOf(ev.get("doctorId"));
            boolean blocked = Boolean.parseBoolean(String.valueOf(ev.getOrDefault("blocked","false")));
            // For demo, we can log or implement targeted notifications (e.g., to waitlist)
            // Keep simple here
            System.out.println("Slot update for doctor " + doctorId + " blocked=" + blocked);
        } catch (Exception e){ e.printStackTrace(); }
    }
}
