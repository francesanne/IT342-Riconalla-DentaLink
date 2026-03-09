package com.dentalink.riconalla.repository;

import com.dentalink.riconalla.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRepository extends JpaRepository<Service, Long> {
}