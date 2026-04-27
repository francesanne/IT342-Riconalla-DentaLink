package edu.cit.riconalla.dentalink.repository;

import edu.cit.riconalla.dentalink.entity.Dentist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DentistRepository extends JpaRepository<Dentist, Long> {

    List<Dentist> findByDentistStatus(String status);
}

