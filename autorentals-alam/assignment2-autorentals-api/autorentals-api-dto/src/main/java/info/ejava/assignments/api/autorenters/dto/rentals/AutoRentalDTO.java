package info.ejava.assignments.api.autorenters.dto.rentals;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import info.ejava.assignments.api.autorenters.dto.StreetAddressDTO;
import info.ejava.assignments.api.autorenters.dto.autos.AutoDTO;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
import info.ejava.examples.common.dto.adapters.JaxbTimeAdapters;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;

@Data
@Builder
@With
@NoArgsConstructor
@AllArgsConstructor

@XmlRootElement(name = "autoRental" , namespace = "urn:ejava.svc-controllers.autoRental") // JAXB
@XmlAccessorType(XmlAccessType.FIELD) // JAXB java.util.Date and java.time adapters

@JacksonXmlRootElement(localName = "autoRental" , namespace = "urn:ejava.svc-controllers.autoRental") // Jackson XML
@Slf4j
public class AutoRentalDTO implements RentalDTO {

    @XmlAttribute // JAXB
    @JacksonXmlProperty (isAttribute = true)  // Jackson
    private String id;
    private String autoId;
    private String renterId;

    @XmlJavaTypeAdapter(JaxbTimeAdapters.LocalDateJaxbAdapter.class) 
    // JAXB local date adapters since JAXB does not have that default
    private LocalDate startDate;
    @XmlJavaTypeAdapter(JaxbTimeAdapters.LocalDateJaxbAdapter.class) 
    // JAXB local date adapters since JAXB does not have that default
    private LocalDate endDate;
    private BigDecimal  amount;

    private String makeModel;
    private String renterName;
    private Integer renterAge;

    private StreetAddressDTO streetAddress;

    @JsonIgnore
    private String userName;

    public AutoRentalDTO(AutoDTO auto, RenterDTO renter, TimePeriod timePeriod){
        this.autoId = auto.getId();
        this.renterId = renter.getId();
        this.startDate = timePeriod.getStartDate();
        this.endDate = timePeriod.getEndDate();
        this.makeModel = auto.getMake() + "-" + auto.getModel();
        this.amount = BigDecimal.valueOf(auto.getDailyRate().doubleValue() * timePeriod.getDays());
        this.renterName = renter.getUsername()!=null ? renter.getUsername():"";
        this.renterAge = TimePeriod.create(renter.getDob(), LocalDate.now()).getPeriod().getYears();
        
        this.streetAddress = deepCopyOfAutoLocation(auto.getLocation());
        
    }

    private StreetAddressDTO deepCopyOfAutoLocation(StreetAddressDTO streetAddress)  {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        StreetAddressDTO deepCopy;
        try {
            deepCopy = mapper.readValue(mapper.writeValueAsString(streetAddress), StreetAddressDTO.class);
            
        } catch (JsonProcessingException e) {
            
            log.error("error creating AutoRental while assigning StreetAddress- {}", e.getMessage());
            deepCopy = null;
        }
        return deepCopy;
    }
}
