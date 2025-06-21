package info.ejava.assignments.api.autorenters.dto.rentals;

import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import info.ejava.examples.common.dto.adapters.EmptyStringAdapter;
import info.ejava.examples.common.dto.adapters.IntegerAdapter;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
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

@XmlRootElement(name = "autoRentals", namespace = "urn:ejava.svc-controllers.autoRentals")   // JAXB
@XmlType                                            // JAXB
@XmlAccessorType(XmlAccessType.NONE)                // JAXB

@JacksonXmlRootElement(localName = "autoRentals", namespace = "urn:ejava.svc-controllers.autoRentals")  // JACKSON

public class AutoRentalListDTO {
    
    
        @XmlElement // JAXB
        @XmlJavaTypeAdapter(IntegerAdapter.class) // JAXB
        private Integer offset;
    
        @XmlElement // JAXB
        @XmlJavaTypeAdapter(IntegerAdapter.class) // JAXB
        private Integer limit;
    
        @XmlElement // JAXB
        @XmlJavaTypeAdapter(IntegerAdapter.class) // JAXB
        private Integer total;
    
        @XmlElement // JAXB
        @XmlJavaTypeAdapter(EmptyStringAdapter.class) //JAXB
        private String keywords;
    
        @XmlElementWrapper(name = "autoRentals") // JAXB
        @XmlElement(name = "autoRental") // JAXB
    
        @JacksonXmlElementWrapper(localName = "autoRentals") // Jackson
        @JacksonXmlProperty(localName = "autoRental") // Jackson
        private List<AutoRentalDTO> autoRentals;
    
        @XmlElement // JAXB
        public int getCount(){
            return autoRentals == null ? 0 : autoRentals.size(); 
        }
    
        public void setCount(Integer count){
            // ignored - count is determined from autoRentals.size
        }
}

