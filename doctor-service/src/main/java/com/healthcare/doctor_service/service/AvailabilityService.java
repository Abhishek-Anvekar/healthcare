package com.healthcare.doctor_service.service;

import com.healthcare.doctor_service.dto.AvailabilityDtos.CreateSlotsRequest;
import com.healthcare.doctor_service.entity.AvailabilitySlot;
import com.healthcare.doctor_service.entity.Doctor;
import com.healthcare.doctor_service.exception.BadRequestException;
import com.healthcare.doctor_service.exception.NotFoundException;
import com.healthcare.doctor_service.repository.AvailabilitySlotRepository;
import com.healthcare.doctor_service.repository.DoctorRepository;
import com.healthcare.doctor_service.service.messaging.DoctorEventsProducer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AvailabilityService {
    private final AvailabilitySlotRepository repo;
    private final DoctorRepository doctorRepo;
    private final DoctorEventsProducer producer;

    public AvailabilityService(AvailabilitySlotRepository repo, DoctorRepository doctorRepo, DoctorEventsProducer producer) {
        this.repo = repo; this.doctorRepo = doctorRepo; this.producer = producer;
    }

    @Transactional
    public List<AvailabilitySlot> createSlots(String doctorId, CreateSlotsRequest req) {
        Doctor doctor = doctorRepo.findById(doctorId).orElseThrow(() -> new NotFoundException("Doctor not found"));
        if (req.endTime().isBefore(req.startTime())) throw new BadRequestException("End time must be after start time");
        List<AvailabilitySlot> created = new ArrayList<>();
        LocalTime cursor = req.startTime();
        while (!cursor.plusMinutes(req.slotMinutes()).isAfter(req.endTime())) {
            AvailabilitySlot s = new AvailabilitySlot();
            s.setDoctor(doctor);
            s.setDate(req.date());
            s.setStartTime(cursor);
            s.setEndTime(cursor.plusMinutes(req.slotMinutes()));
            s.setMode(req.mode());
            repo.save(s);
            created.add(s);
            cursor = cursor.plusMinutes(req.slotMinutes());
        }
        producer.sendAvailabilityUpdated(doctorId);
        return created;
    }

    public List<AvailabilitySlot> listByDoctor(String doctorId){
        return repo.findByDoctorIdOrderByDateAscStartTimeAsc(doctorId);
    }

    @Transactional
    public void blockSlots(String doctorId, List<String> slotIds, boolean blocked){
        for (String id : slotIds) {
            AvailabilitySlot s = repo.findById(id).orElseThrow(() -> new NotFoundException("Slot not found: "+id));
            if (!s.getDoctor().getId().equals(doctorId)) throw new BadRequestException("Slot does not belong to doctor");
            s.setBlocked(blocked);
            repo.save(s);
        }
        producer.sendAvailabilityUpdated(doctorId);
    }
}
