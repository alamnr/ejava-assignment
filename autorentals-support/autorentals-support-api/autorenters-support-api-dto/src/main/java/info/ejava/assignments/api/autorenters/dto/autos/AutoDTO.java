package info.ejava.assignments.api.autorenters.dto.autos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import info.ejava.assignments.api.autorenters.dto.StreetAddressDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import java.math.BigDecimal;

@Data
@Builder(toBuilder = true)
@With
@NoArgsConstructor
@AllArgsConstructor
public class AutoDTO {
    private String id;
    private Integer passengers;
    private String fuelType;
    private BigDecimal dailyRate;
    private String make;
    private String model;
    private StreetAddressDTO location;
    @JsonIgnore
    private String username;
}
