package edu.cit.riconalla.dentalink.controller;

import edu.cit.riconalla.dentalink.entity.Service;
import edu.cit.riconalla.dentalink.service.ServiceService;
import edu.cit.riconalla.dentalink.service.SupabaseStorageService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/services")
@CrossOrigin(origins = "*")
public class ServiceController {


    private final ServiceService serviceService;
    private final SupabaseStorageService storageService;

    // ✅ FIXED CONSTRUCTOR (THIS IS WHAT YOU ASKED)
    public ServiceController(ServiceService serviceService,
                             SupabaseStorageService storageService) {
        this.serviceService = serviceService;
        this.storageService = storageService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllServices() {
        List<Service> services = serviceService.getAllServices();
        return ResponseEntity.ok(Map.of("success", true, "data", services));
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Map<String, Object>> createService(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam String price,
            @RequestParam(required = false) MultipartFile image
    ) {
        try {
            BigDecimal priceValue = new BigDecimal(price);

            String imagePath = null;

            // ✅ SUPABASE UPLOAD (FIXED)
            if (image != null && !image.isEmpty()) {

                String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();

                imagePath = storageService.uploadFile(
                        image.getBytes(),
                        fileName
                );
            }

            Service s = serviceService.createService(name, description, priceValue, imagePath);

            return ResponseEntity.status(201).body(Map.of("success", true, "data", s));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "error", "UPLOAD FAILED: " + e.getMessage()
            ));
        }
    }

    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, Object>> updateService(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String price,
            @RequestParam(required = false) MultipartFile image
    ) {
        try {
            BigDecimal priceValue = null;

            if (price != null && !price.isEmpty()) {
                priceValue = new BigDecimal(price);
            }

            String imagePath = null;

            // ✅ SUPABASE UPLOAD (FIXED)
            if (image != null && !image.isEmpty()) {

                String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();

                imagePath = storageService.uploadFile(
                        image.getBytes(),
                        fileName
                );
            }

            Service s = serviceService.updateService(id, name, description, priceValue, imagePath);

            return ResponseEntity.ok(Map.of("success", true, "data", s));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "error", "UPLOAD FAILED: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteService(@PathVariable Long id) {
        serviceService.deleteService(id);
        return ResponseEntity.ok(Map.of("success", true, "data", "Service deleted"));
    }
}