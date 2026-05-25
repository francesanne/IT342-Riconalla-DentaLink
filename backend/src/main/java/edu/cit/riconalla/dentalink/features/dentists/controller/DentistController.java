package edu.cit.riconalla.dentalink.features.dentists.controller;

import edu.cit.riconalla.dentalink.shared.dto.ApiResponse;
import edu.cit.riconalla.dentalink.features.dentists.dto.DentistDto;
import edu.cit.riconalla.dentalink.features.dentists.dto.DentistRequest;
import edu.cit.riconalla.dentalink.features.dentists.service.DentistService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dentists")
public class DentistController {

    private final DentistService dentistService;

    public DentistController(DentistService dentistService) {
        this.dentistService = dentistService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DentistDto>>> getAllDentists(Authentication auth) {
        String role = auth.getAuthorities().iterator().next().getAuthority();
        List<DentistDto> dentists = dentistService.getAllDentists(role);
        return ResponseEntity.ok(ApiResponse.success(dentists));
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DentistDto>> getDentistById(@PathVariable Long id) {
        DentistDto dentist = dentistService.getDentistById(id);
        return ResponseEntity.ok(ApiResponse.success(dentist));
    }


    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<DentistDto>> createDentist(
            @Valid @RequestBody DentistRequest request
    ) {
        DentistDto created = dentistService.createDentist(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<DentistDto>> updateDentist(
            @PathVariable Long id,
            @Valid @RequestBody DentistRequest request
    ) {
        DentistDto updated = dentistService.updateDentist(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteDentist(@PathVariable Long id) {
        dentistService.deleteDentist(id);
        return ResponseEntity.ok(ApiResponse.success("Dentist deleted"));
    }
}