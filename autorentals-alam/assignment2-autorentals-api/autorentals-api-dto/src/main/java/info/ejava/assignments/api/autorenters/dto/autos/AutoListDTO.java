package info.ejava.assignments.api.autorenters.dto.autos;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import info.ejava.examples.common.dto.adapters.EmptyStringAdapter;
import info.ejava.examples.common.dto.adapters.IntegerAdapter;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

@XmlRootElement(name = "autos" , namespace = "urn:ejava.svc-controllers.autos")  // JAXB
@XmlType                                        // JAXB
@XmlAccessorType(XmlAccessType.NONE)            // JAXB

@JacksonXmlRootElement( localName = "autos", namespace = "urn:ejava.svc-controllers.autos")  // JACKSON XML
public class AutoListDTO {
    @XmlElement   // JAXB
    @XmlJavaTypeAdapter(IntegerAdapter.class) // JAXB
    private Integer offset;

    @XmlElement             // JAXB
    @XmlJavaTypeAdapter(IntegerAdapter.class)   // JAXB
    private Integer limit;


    @XmlElement                                              // JAXB   
    @XmlJavaTypeAdapter(IntegerAdapter.class)           // JAXB
    private Integer total;


    @XmlElement                                     // JAXB
    @XmlJavaTypeAdapter(EmptyStringAdapter.class)       // JAXB
    private String keywords;

    @XmlElement(name = "auto")              // JAXB
    @XmlElementWrapper(name = "autos")      // JAXB

    @JacksonXmlProperty(localName = "auto")    // JACKSON
    @JacksonXmlElementWrapper(localName = "autos")      // JACKSON

    private List<AutoDTO> autos;


    @XmlElement //JAXB
    public int getCount(){
        return autos == null ? 0 : autos.size();
    }

    public void setCount(Integer count){
        // ignored - count is determined from autos.size
    }
    
    
}
