package edu.cit.riconalla.dentalink.features.dentists.repository;

import edu.cit.riconalla.dentalink.features.dentists.entity.Dentist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DentistRepository extends JpaRepository<Dentist, Long> {

    List<Dentist> findByDentistStatus(String status);
}

