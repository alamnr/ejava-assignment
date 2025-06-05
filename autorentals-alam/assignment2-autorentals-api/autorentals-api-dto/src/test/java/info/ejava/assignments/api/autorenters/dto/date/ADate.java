package info.ejava.assignments.api.autorenters.dto.date;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import info.ejava.examples.common.dto.adapters.JaxbTimeAdapters;
import info.ejava.examples.common.dto.adapters.JsonbTimeDeserializers;
import info.ejava.examples.common.dto.adapters.JsonbTimeSerializers;
import jakarta.json.bind.annotation.JsonbTypeDeserializer;
import jakarta.json.bind.annotation.JsonbTypeSerializer;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;


@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@With

@XmlRootElement(name = "dates", namespace = "urn:ejava.svc-controllers.renters")
@XmlAccessorType(XmlAccessType.FIELD)
@JacksonXmlRootElement(localName = "dates", namespace = "urn:ejava.svc-controllers.renters")
public class ADate {
    @JsonbTypeDeserializer(JsonbTimeDeserializers.ZonedDateTimeJsonbDeserializer.class) // Jasonb deserializer for java date and time
    @XmlJavaTypeAdapter(JaxbTimeAdapters.ZonedDateTimeJaxbAdapter.class)
    private ZonedDateTime zdt;

    @JsonbTypeDeserializer(JsonbTimeDeserializers.OffsetDateTimeJsonbDeserializer.class)  // Jsonb deserializer for java date and time
    @XmlJavaTypeAdapter(JaxbTimeAdapters.OffsetDateTimeJaxbAdapter.class)
    private OffsetDateTime odt;

    @JsonbTypeDeserializer(JsonbTimeDeserializers.LocalDateTimeJsonbDeserializer.class) // Jasonb deserializer for java date and time
    @XmlJavaTypeAdapter(JaxbTimeAdapters.LocalDateTimeJaxbAdapter.class)
    private LocalDateTime ldt;

    @JsonbTypeDeserializer(JsonbTimeDeserializers.InstantJsonbDeserializer.class)  // Jasonb deserializer for java date and time
    @XmlJavaTypeAdapter(JaxbTimeAdapters.InstantJaxbAdapter.class)
    private Instant instant;

    @JsonbTypeDeserializer(JsonbTimeDeserializers.DateJsonbDeserializer.class) 
    @XmlJavaTypeAdapter(JaxbTimeAdapters.DateJaxbAdapter.class)
    @JsonbTypeSerializer(JsonbTimeSerializers.DateJsonbSerializer.class)
    private Date date;

    public static ADate of(ZonedDateTime zdt) {
        return new ADate(zdt, zdt.toOffsetDateTime(), zdt.toLocalDateTime(), zdt.toInstant(), Date.from(zdt.toInstant()));
    }

    public ADate truncateDateToMillis() {
        Date milliDate = Date.from(date.toInstant().truncatedTo(ChronoUnit.MILLIS));
        return of(zdt).withDate(milliDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ADate)) return false;
        ADate aDate = (ADate) o;

        if (zdt != null ? !zdt.isEqual(aDate.zdt) : aDate.zdt != null) return false;
        if (odt != null ? !odt.isEqual(aDate.odt) : aDate.odt != null) return false;
        //LDT will be in a specific timezone, without a reference to that TZ -- so will be false if not same TZ
        //if (ldt != null ? !ldt.isEqual(aDate.ldt) : aDate.ldt != null) return false;
        if (instant != null ? !instant.equals(aDate.instant) : aDate.instant != null) return false;
        return date != null ? date.equals(aDate.date) : aDate.date == null;
    }

    @Override
    public int hashCode() {
        int result = zdt != null ? zdt.hashCode() : 0;
        result = 31 * result + (odt != null ? odt.hashCode() : 0);
        //result = 31 * result + (ldt != null ? ldt.hashCode() : 0);
        result = 31 * result + (instant != null ? instant.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        return result;
    }
}
