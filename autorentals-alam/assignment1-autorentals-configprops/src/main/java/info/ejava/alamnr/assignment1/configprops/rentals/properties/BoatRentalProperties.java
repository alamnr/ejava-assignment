package info.ejava.alamnr.assignment1.configprops.rentals.properties;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;
import lombok.Value;


//@Value
@Validated
@ToString
@Data
public class BoatRentalProperties {
    
    private int id;
    private LocalDate rentalDate;
    private BigDecimal rentalAmount;
    @NotEmpty
    private String renterName;
    @NestedConfigurationProperty
    @NotNull
    private AddressProperties location;

    
    
}
