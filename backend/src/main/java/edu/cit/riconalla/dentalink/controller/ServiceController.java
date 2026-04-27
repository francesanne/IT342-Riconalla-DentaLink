package edu.cit.riconalla.dentalink.controller;

import edu.cit.riconalla.dentalink.dto.ApiResponse;
import edu.cit.riconalla.dentalink.dto.ServiceDto;
import edu.cit.riconalla.dentalink.dto.ServiceRequest;
import edu.cit.riconalla.dentalink.service.ServiceService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/services")
public class ServiceController {

    private final ServiceService serviceService;

    public ServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    /** GET /api/v1/services — public, no auth required — SDD §5.3 */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ServiceDto>>> getAllServices() {
        List<ServiceDto> services = serviceService.getAllServices();
        return ResponseEntity.ok(ApiResponse.success(services));
    }

    /** GET /api/v1/services/{id} — public, no auth required — SDD §5.3 */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceDto>> getServiceById(@PathVariable Long id) {
        ServiceDto service = serviceService.getServiceById(id);
        return ResponseEntity.ok(ApiResponse.success(service));
    }

    /** POST /api/v1/services — ADMIN only — SDD §5.3 */
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<ServiceDto>> createService(
            @RequestBody ServiceRequest request
    ) {
        ServiceDto created = serviceService.createService(
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                null
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    /** PUT /api/v1/services/{id} — ADMIN only — SDD §5.3 */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<ServiceDto>> updateService(
            @PathVariable Long id,
            @RequestBody ServiceRequest request
    ) {
        ServiceDto updated = serviceService.updateService(
                id,
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                null
        );
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    /** DELETE /api/v1/services/{id} — ADMIN only — SDD §5.3 */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteService(@PathVariable Long id) {
        serviceService.deleteService(id);
        return ResponseEntity.ok(ApiResponse.success("Service deleted"));
    }

    /**
     * POST /api/v1/services/{id}/upload-image — ADMIN only — SDD §5.3
     * Content-Type: multipart/form-data
     * Form field: file (JPEG or PNG, max 5 MB)
     */
    @PostMapping(value = "/{id}/upload-image", consumes = "multipart/form-data")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            String imageUrl = serviceService.uploadServiceImage(id, file);
            return ResponseEntity.ok(ApiResponse.success(Map.of("imageUrl", imageUrl)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("INVALID_FILE", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("UPLOAD_FAILED", e.getMessage()));
        }
    }
}