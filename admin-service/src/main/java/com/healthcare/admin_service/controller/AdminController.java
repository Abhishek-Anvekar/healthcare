package com.healthcare.admin_service.controller;

import com.healthcare.admin_service.entity.Admin;
import com.healthcare.admin_service.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admins")
@Tag(name = "Admin APIs", description = "CRUD operations for Admin management")
public class AdminController {

    private final AdminService service;

    public AdminController(AdminService service) {
        this.service = service;
    }

    @Operation(
            summary = "Create a new Admin",
            description = "Registers a new admin in the system",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Admin created successfully",
                            content = @Content(schema = @Schema(implementation = Admin.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content)
            }
    )
    @PostMapping
    public ResponseEntity<Admin> createAdmin(@RequestBody Admin admin) {
        return ResponseEntity.ok(service.createAdmin(admin));
    }

    @Operation(
            summary = "Get all Admins",
            description = "Retrieves a list of all admins",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of admins retrieved successfully",
                            content = @Content(schema = @Schema(implementation = Admin.class)))
            }
    )
    @GetMapping
    public ResponseEntity<List<Admin>> getAllAdmins() {
        return ResponseEntity.ok(service.getAllAdmins());
    }

    @Operation(
            summary = "Get Admin by ID",
            description = "Retrieves a single admin by their ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Admin found",
                            content = @Content(schema = @Schema(implementation = Admin.class))),
                    @ApiResponse(responseCode = "404", description = "Admin not found", content = @Content)
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<Admin> getAdminById(
            @Parameter(description = "ID of the admin to be retrieved") @PathVariable String id) {
        return ResponseEntity.ok(service.getAdminById(id));
    }

    @Operation(
            summary = "Update an Admin",
            description = "Updates details of an existing admin",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Admin updated successfully",
                            content = @Content(schema = @Schema(implementation = Admin.class))),
                    @ApiResponse(responseCode = "404", description = "Admin not found", content = @Content)
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<Admin> updateAdmin(
            @Parameter(description = "ID of the admin to be updated") @PathVariable String id,
            @RequestBody Admin admin) {
        return ResponseEntity.ok(service.updateAdmin(id, admin));
    }

    @Operation(
            summary = "Delete an Admin",
            description = "Deletes an admin by their ID",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Admin deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Admin not found", content = @Content)
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdmin(
            @Parameter(description = "ID of the admin to be deleted") @PathVariable String id) {
        service.deleteAdmin(id);
        return ResponseEntity.noContent().build();
    }
}
