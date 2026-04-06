package edu.cit.riconalla.dentalink.service;

import edu.cit.riconalla.dentalink.entity.Service;
import edu.cit.riconalla.dentalink.repository.ServiceRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class ServiceService {

    private final ServiceRepository serviceRepository;

    public ServiceService(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    public List<Service> getAllServices() {
        return serviceRepository.findAll();
    }

    public Service createService(String name, String description, BigDecimal price, String imageUrl) {
        Service s = new Service();
        s.setServiceName(name);
        s.setServiceDescription(description);
        s.setServicePrice(price);
        s.setServiceImageUrl(imageUrl);
        return serviceRepository.save(s);
    }

    public Service updateService(Long id, String name, String description, BigDecimal price, String imageUrl) {
        Service s = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        if (name != null) s.setServiceName(name);
        if (description != null) s.setServiceDescription(description);
        if (price != null) s.setServicePrice(price);
        if (imageUrl != null) s.setServiceImageUrl(imageUrl);

        return serviceRepository.save(s);
    }

    public void deleteService(Long id) {
        serviceRepository.deleteById(id);
    }
}