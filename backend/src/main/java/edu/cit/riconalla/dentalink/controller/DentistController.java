package edu.cit.riconalla.dentalink.controller;

import edu.cit.riconalla.dentalink.entity.Dentist;
import edu.cit.riconalla.dentalink.service.DentistService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dentists")
public class DentistController {

    private final DentistService dentistService;

    public DentistController(DentistService dentistService) {
        this.dentistService = dentistService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllDentists() {
        List<Dentist> dentists = dentistService.getAllDentists();
        return ResponseEntity.ok(Map.of("success", true, "data", dentists));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getDentist(@PathVariable Long id) {
        Dentist d = dentistService.getDentistById(id);
        return ResponseEntity.ok(Map.of("success", true, "data", d));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createDentist(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        String spec = (String) body.get("specialization");
        String status = (String) body.get("status");
        Dentist d = dentistService.createDentist(name, spec, status);
        return ResponseEntity.status(201).body(Map.of("success", true, "data", d));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateDentist(@PathVariable Long id,
                                                             @RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        String spec = (String) body.get("specialization");
        String status = (String) body.get("status");
        Dentist d = dentistService.updateDentist(id, name, spec, status);
        return ResponseEntity.ok(Map.of("success", true, "data", d));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteDentist(@PathVariable Long id) {
        dentistService.deleteDentist(id);
        return ResponseEntity.ok(Map.of("success", true, "data", "Dentist deleted"));
    }
}