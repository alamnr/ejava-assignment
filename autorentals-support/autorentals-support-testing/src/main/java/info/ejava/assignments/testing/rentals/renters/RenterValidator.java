package info.ejava.assignments.testing.rentals.renters;

import java.util.List;

public interface RenterValidator {
    List<String> validateNewRenter(RenterDTO renter, int minAge);
}
