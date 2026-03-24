package edu.cit.riconalla.dentalink.repository;
import edu.cit.riconalla.dentalink.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRepository extends JpaRepository<Service, Long> {
}