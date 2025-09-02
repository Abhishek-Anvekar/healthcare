package com.healthcare.admin_service.service;

import com.healthcare.admin_service.entity.Admin;
import com.healthcare.admin_service.exception.AdminAlreadyExistsException;
import com.healthcare.admin_service.exception.AdminNotFoundException;
import com.healthcare.admin_service.repository.AdminRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {

    private final AdminRepository repository;

    public AdminService(AdminRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Admin createAdmin(Admin admin) {
        if (repository.existsByEmail(admin.getEmail())) {
            throw new AdminAlreadyExistsException("Admin already exists with email: " + admin.getEmail());
        }
        return repository.save(admin);
    }

    public List<Admin> getAllAdmins() {
        return repository.findAll();
    }

    public Admin getAdminById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new AdminNotFoundException("Admin not found with id: " + id));
    }

    public Admin updateAdmin(String id, Admin updated) {
        Admin existing = getAdminById(id);
        existing.setName(updated.getName());
        existing.setEmail(updated.getEmail());
        existing.setPhone(updated.getPhone());
        existing.setAge(updated.getAge());
        existing.setGender(updated.getGender());
        return repository.save(existing);
    }

    public void deleteAdmin(String id) {
        if (!repository.existsById(id)) {
            throw new AdminNotFoundException("Admin not found with id: " + id);
        }
        repository.deleteById(id);
    }
}

