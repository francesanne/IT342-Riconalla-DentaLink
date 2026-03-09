package com.dentalink.riconalla.repository;

import com.dentalink.riconalla.entity.Dentist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DentistRepository extends JpaRepository<Dentist, Long> {
}