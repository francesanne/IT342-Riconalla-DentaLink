package edu.cit.riconalla.dentalink.service;

import edu.cit.riconalla.dentalink.entity.Dentist;
import edu.cit.riconalla.dentalink.repository.DentistRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DentistService {

    private final DentistRepository dentistRepository;

    public DentistService(DentistRepository dentistRepository) {
        this.dentistRepository = dentistRepository;
    }

    public List<Dentist> getAllDentists() {
        return dentistRepository.findAll();
    }

    public Dentist getDentistById(Long id) {
        return dentistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dentist not found"));
    }

    public Dentist createDentist(String name, String specialization, String status) {
        Dentist d = new Dentist();
        d.setDentistName(name);
        d.setDentistSpecialization(specialization);
        d.setDentistStatus(status != null ? status : "ACTIVE");
        return dentistRepository.save(d);
    }

    public Dentist updateDentist(Long id, String name, String specialization, String status) {
        Dentist d = getDentistById(id);
        if (name != null) d.setDentistName(name);
        if (specialization != null) d.setDentistSpecialization(specialization);
        if (status != null) d.setDentistStatus(status);
        return dentistRepository.save(d);
    }

    public void deleteDentist(Long id) {
        getDentistById(id);
        dentistRepository.deleteById(id);
    }
}