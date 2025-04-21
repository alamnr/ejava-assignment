package info.ejava.assignments.testing.rentals.renters;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

@RequiredArgsConstructor
@Slf4j
public class RenterValidatorImpl implements RenterValidator {

    @Override
    public List<String> validateNewRenter(RenterDTO renter, int minAge) {
        List<String> errorMsgs = new ArrayList<>();
        log.trace("validating {}, minAge={}", renter, minAge);

        if (renter == null) {
            errorMsgs.add("renter: cannot be null");
        } else {
            validate(errorMsgs,
                    () -> renter.getId() == null,
                    () -> "id must be null");
            validate(errorMsgs,
                    () -> !ObjectUtils.isEmpty(renter.getFirstName()),
                    () -> "renter.firstName: cannot be blank");
            validate(errorMsgs,
                    () -> !ObjectUtils.isEmpty(renter.getLastName()),
                    () -> "renter.lastName: cannot be blank");
            Optional.ofNullable(renter.getDob())
                    .ifPresentOrElse((dob) -> {
                        LocalDate minDob = LocalDate.now().minusYears(minAge);
                        validate(errorMsgs,
                                () -> dob.isBefore(minDob),
                                () -> String.format("renter.dob: must be greater than %d years", minAge));
                    }, () -> errorMsgs.add("renter.dob: cannot be null"));
        }
        log.trace("renter {}, valid={}, errors={}", renter, errorMsgs.isEmpty(), errorMsgs);
        return errorMsgs;
    }

    private void validate(List<String> errorMsgs, BooleanSupplier predicate, Supplier<String> errorMsg) {
        if (!predicate.getAsBoolean()) {
            errorMsgs.add(errorMsg.get());
        }
    }
}
