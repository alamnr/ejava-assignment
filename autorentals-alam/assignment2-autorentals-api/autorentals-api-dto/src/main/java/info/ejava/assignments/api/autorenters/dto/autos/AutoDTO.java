package info.ejava.assignments.api.autorenters.dto.autos;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import info.ejava.assignments.api.autorenters.dto.StreetAddressDTO;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Data
@Builder
@With
@NoArgsConstructor
@AllArgsConstructor

@XmlRootElement(name = "auto" , namespace = "urn:ejava.svc-controllers.autos")      // JAXB
@XmlAccessorType(XmlAccessType.FIELD)  // JAXB java.util.date and java.time adapters

@JacksonXmlRootElement(localName = "auto", namespace = "urn:ejava.svc-controllers.autos")  // JACKSON
public class AutoDTO {

    @XmlAttribute // JAXB
    @JacksonXmlProperty(isAttribute = true)  // JACKSON
    private String id;
    private Integer passengers;
    private String fuelType;
    private BigDecimal dailyRate;
    private String make;
    private String model;
    private StreetAddressDTO location;
    @JsonIgnore
    private  String username;
}