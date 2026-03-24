package edu.cit.riconalla.dentalink.repository;

import edu.cit.riconalla.dentalink.entity.Dentist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DentistRepository extends JpaRepository<Dentist, Long> {
}