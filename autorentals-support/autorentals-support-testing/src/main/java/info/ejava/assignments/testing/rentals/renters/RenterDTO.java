package info.ejava.assignments.testing.rentals.renters;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.time.LocalDate;
@Value
@With
@Builder
@AllArgsConstructor
public class RenterDTO {
    private final String id;
    private final String firstName;
    private final String lastName;
    private final LocalDate dob;
}
