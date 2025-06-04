package info.ejava.assignments.api.autorenters.dto.renters;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

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

@Data
@Builder
@With
@NoArgsConstructor
@AllArgsConstructor

@XmlRootElement(name = "renter", namespace = "urn:ejava.svc-controllers.renters") // JAXB
@XmlAccessorType(XmlAccessType.FIELD) // JAXB java.util.Date and java.time adapters

@JacksonXmlRootElement(localName = "renter", namespace = "urn:ejava.svc-controllers.renters") // Jackson XML
public class RenterDTO {
    @XmlAttribute // JAXB
    @JacksonXmlProperty (isAttribute = true)  // Jackson
    private String id;
    private String firstName;
    private String lastName;
    @XmlJavaTypeAdapter(info.ejava.examples.common.dto.adapters.JaxbTimeAdapters.LocalDateJaxbAdapter.class) 
    // JAXB local date adapters since JAXB does not have that default
    private LocalDate dob;
    private String email;

    @JsonIgnore
    private String username;
}
