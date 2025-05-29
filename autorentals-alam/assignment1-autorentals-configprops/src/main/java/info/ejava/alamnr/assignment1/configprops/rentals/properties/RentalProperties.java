package info.ejava.alamnr.assignment1.configprops.rentals.properties;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.Value;

@Value
@Validated
@ToString
@AllArgsConstructor
public class RentalProperties {
    @NotNull
    private int id;
    @NotNull
    private LocalDate rentalDate;

    private BigDecimal rentalAmount;
    private String renterName;
    @NestedConfigurationProperty
    @NotNull
    private AddressProperties location;

    
}
