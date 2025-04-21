package info.ejava.assignments.testing.rentals.renters;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Validated
//@ConfigurationProperties("rentals.renters")
public class RentersProperties {
    @NotNull
    private Integer minAge;
}
