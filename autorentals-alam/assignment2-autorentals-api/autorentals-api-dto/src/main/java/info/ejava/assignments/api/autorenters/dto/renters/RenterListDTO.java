package info.ejava.assignments.api.autorenters.dto.renters;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Data
@NoArgsConstructor
@AllArgsConstructor
@With
@Builder

@XmlRootElement(name = "renters", namespace = "urn:ejava.svc-controllers.renters")   // JAXB
@XmlType                                            // JAXB
@XmlAccessorType(XmlAccessType.NONE)                // JAXB

@JacksonXmlRootElement(localName = "renters", namespace = "urn:ejava.svc-controllers.renters")  // JACKSON
public class RenterListDTO {

    @XmlAttribute(required = false) // JAXB
    private Integer offset;

    @XmlAttribute(required = false) // JAXB
    private Integer limit;

    @XmlAttribute(required = false) // JAXB
    private Integer total;

    @XmlAttribute(required = false) // JAXB
    private String keywords;

    @XmlElementWrapper(name = "renters") // JAXB
    @XmlElement(name = "quote") // JAXB

    @JacksonXmlElementWrapper(localName = "renters") // Jackson
    @JacksonXmlProperty(localName = "quote") // Jackson
    private List<RenterDTO> renters;

    @XmlAttribute(required = false) // JAXB
    public int getCount(){
        return renters == null ? 0 : renters.size(); 
    }

    public void setCount(Integer count){
        // ignored - count is determined from renters.size
    }
}
