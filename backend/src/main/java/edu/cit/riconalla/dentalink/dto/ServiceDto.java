package edu.cit.riconalla.dentalink.dto;

import edu.cit.riconalla.dentalink.entity.Service;

import java.math.BigDecimal;

public class ServiceDto {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;

    public ServiceDto(Long id, String name, String description,
                      BigDecimal price, String imageUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
    }

    /** Factory method — maps from Service entity to ServiceDto. */
    public static ServiceDto from(Service service) {
        return new ServiceDto(
                service.getServiceId(),
                service.getServiceName(),
                service.getServiceDescription(),
                service.getServicePrice(),
                service.getServiceImageUrl()
        );
    }

    public Long getId()          { return id; }
    public String getName()      { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public String getImageUrl()  { return imageUrl; }
}