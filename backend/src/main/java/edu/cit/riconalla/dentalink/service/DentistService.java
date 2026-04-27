package edu.cit.riconalla.dentalink.service;

import edu.cit.riconalla.dentalink.dto.DentistDto;
import edu.cit.riconalla.dentalink.dto.DentistRequest;
import edu.cit.riconalla.dentalink.entity.Dentist;
import edu.cit.riconalla.dentalink.exception.ResourceNotFoundException;
import edu.cit.riconalla.dentalink.repository.DentistRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DentistService {

    private static final List<String> VALID_STATUSES = Arrays.asList("ACTIVE", "INACTIVE");

    private final DentistRepository dentistRepository;

    public DentistService(DentistRepository dentistRepository) {
        this.dentistRepository = dentistRepository;
    }

    /**
     * GET /dentists — role-aware (U-1):
     * ADMIN → all dentists
     * PATIENT → only ACTIVE dentists
     */
    public List<DentistDto> getAllDentists(String role) {
        List<Dentist> results = "ADMIN".equals(role)
                ? dentistRepository.findAll()
                : dentistRepository.findByDentistStatus("ACTIVE");

        return results.stream()
                .map(DentistDto::from)
                .collect(Collectors.toList());
    }

    /**
     * GET /dentists/{id} — any authenticated role
     */
    public DentistDto getDentistById(Long id) {
        Dentist d = dentistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dentist not found"));
        return DentistDto.from(d);
    }

    /**
     * POST /dentists — ADMIN only
     */
    public DentistDto createDentist(DentistRequest request) {
        validateStatus(request.getStatus());

        Dentist d = new Dentist();
        d.setDentistName(request.getName());
        d.setDentistSpecialization(request.getSpecialization());
        d.setDentistStatus(request.getStatus());

        return DentistDto.from(dentistRepository.save(d));
    }

    /**
     * PUT /dentists/{id} — ADMIN only
     */
    public DentistDto updateDentist(Long id, DentistRequest request) {
        Dentist d = dentistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dentist not found"));

        if (request.getName() != null)           d.setDentistName(request.getName());
        if (request.getSpecialization() != null) d.setDentistSpecialization(request.getSpecialization());
        if (request.getStatus() != null) {
            validateStatus(request.getStatus());
            d.setDentistStatus(request.getStatus());
        }

        return DentistDto.from(dentistRepository.save(d));
    }

    /**
     * DELETE /dentists/{id} — ADMIN only
     */
    public void deleteDentist(Long id) {
        if (!dentistRepository.existsById(id)) {
            throw new ResourceNotFoundException("Dentist not found");
        }
        dentistRepository.deleteById(id);
    }

    // --- Private helpers ---

    private void validateStatus(String status) {
        if (status == null || !VALID_STATUSES.contains(status)) {
            throw new IllegalArgumentException(
                    "Invalid status value. Allowed values: ACTIVE, INACTIVE");
        }
    }
}