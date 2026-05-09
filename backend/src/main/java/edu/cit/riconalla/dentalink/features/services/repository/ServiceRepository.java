package edu.cit.riconalla.dentalink.features.services.repository;
import edu.cit.riconalla.dentalink.features.services.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRepository extends JpaRepository<Service, Long> {
}