package info.ejava.assignments.testing.rentals.renters;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RequiredArgsConstructor
public class RentersServiceImpl implements RentersService {
    private final RenterValidator validator;
    private final int minAge;
    private static final AtomicInteger IDS = new AtomicInteger();

    public RentersServiceImpl(RentersProperties props, RenterValidator validator) {
        this.validator = validator;
        this.minAge = props.getMinAge();
        log.debug("initialized with minAge={}", minAge);
    }

    @Override
    public RenterDTO createRenter(RenterDTO renter) throws InvalidInputException {
        validRegistration(renter);
        //...
        RenterDTO addedRenter = renter.withId(Integer.valueOf(IDS.incrementAndGet()).toString());
        //...
        log.info("renter added: {}", renter);
        return addedRenter;
    }

    void validRegistration(RenterDTO renter) throws InvalidInputException {
        List<String> errorMsgs = validator.validateNewRenter(renter, minAge);
        if (!errorMsgs.isEmpty()) {
            log.info("invalid renter: {}, {}", renter, errorMsgs);
            throw new InvalidInputException("invalid renter: %s", errorMsgs);
        }
    }
}
