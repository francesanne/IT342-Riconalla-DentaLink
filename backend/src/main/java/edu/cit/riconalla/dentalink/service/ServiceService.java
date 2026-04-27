package edu.cit.riconalla.dentalink.service;

import edu.cit.riconalla.dentalink.dto.ServiceDto;
import edu.cit.riconalla.dentalink.entity.Service;
import edu.cit.riconalla.dentalink.exception.ResourceNotFoundException;
import edu.cit.riconalla.dentalink.repository.ServiceRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ServiceService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB — C-11
    private static final List<String> ALLOWED_TYPES =
            Arrays.asList("image/jpeg", "image/png");       // SDD §2.4, §3.2

    private final ServiceRepository serviceRepository;
    private final SupabaseStorageService storageService;

    public ServiceService(ServiceRepository serviceRepository,
                          SupabaseStorageService storageService) {
        this.serviceRepository = serviceRepository;
        this.storageService = storageService;
    }

    public List<ServiceDto> getAllServices() {
        return serviceRepository.findAll()
                .stream()
                .map(ServiceDto::from)
                .collect(Collectors.toList());
    }

    public ServiceDto getServiceById(Long id) {
        Service s = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));
        return ServiceDto.from(s);
    }

    public ServiceDto createService(String name, String description,
                                    BigDecimal price, String imageUrl) {
        Service s = new Service();
        s.setServiceName(name);
        s.setServiceDescription(description);
        s.setServicePrice(price);
        s.setServiceImageUrl(imageUrl);
        return ServiceDto.from(serviceRepository.save(s));
    }

    public ServiceDto updateService(Long id, String name, String description,
                                    BigDecimal price, String imageUrl) {
        Service s = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));

        if (name != null)        s.setServiceName(name);
        if (description != null) s.setServiceDescription(description);
        if (price != null)       s.setServicePrice(price);
        if (imageUrl != null)    s.setServiceImageUrl(imageUrl);

        return ServiceDto.from(serviceRepository.save(s));
    }

    public void deleteService(Long id) {
        if (!serviceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Service not found");
        }
        serviceRepository.deleteById(id);
    }

    /**
     * POST /services/{id}/upload-image — SDD §5.3
     * Validates file type (JPEG/PNG) and size (≤5 MB), uploads to storage,
     * persists URL, returns { imageUrl }.
     */
    public String uploadServiceImage(Long id, MultipartFile file) throws IOException {

        // Validate file type — SDD §2.4, §3.2, C-11
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Invalid file type. Only JPEG and PNG are allowed.");
        }

        // Validate file size — C-11 (5 MB max)
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds the 5 MB limit.");
        }

        // Confirm service exists — SDD §5.3 (404 if not found)
        Service s = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));

        // Upload and get public URL
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        String imageUrl = storageService.uploadFile(file.getBytes(), fileName);

        // Persist URL
        s.setServiceImageUrl(imageUrl);
        serviceRepository.save(s);

        return imageUrl;
    }
}