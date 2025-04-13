package info.ejava.alamnr.assignment1.autorentals.logging.svc;

import java.math.BigDecimal;

import info.ejava.alamnr.assignment1.autorentals.logging.repo.AutoRentalDTO;

public interface AutoRentalsHelper {
    BigDecimal calcDelta(AutoRentalDTO leader, AutoRentalDTO targetResult);
}
