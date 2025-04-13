package info.ejava.alamnr.assignment1.autorentals.logging.svc;

import java.math.BigDecimal;

public interface AutoRentalsService {
    BigDecimal calcDelta(String autoId, String renterId);
}
