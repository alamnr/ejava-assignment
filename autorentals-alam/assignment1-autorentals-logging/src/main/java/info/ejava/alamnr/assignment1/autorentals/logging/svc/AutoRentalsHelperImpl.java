package info.ejava.alamnr.assignment1.autorentals.logging.svc;

import java.math.BigDecimal;

import info.ejava.alamnr.assignment1.autorentals.logging.repo.AutoRentalDTO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AutoRentalsHelperImpl implements AutoRentalsHelper {

    @Override
    public BigDecimal calcDelta(AutoRentalDTO leader, AutoRentalDTO targetResult) {
        log.trace("calling heper with leader - {}  and  target - {} ", leader, targetResult);
        BigDecimal amount =  leader.getAmount().add(targetResult.getAmount());
        log.debug("returned amount - {}", amount);
        return amount;
    }
    
}
