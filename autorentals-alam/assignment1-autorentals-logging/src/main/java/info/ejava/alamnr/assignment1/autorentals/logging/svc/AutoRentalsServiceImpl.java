package info.ejava.alamnr.assignment1.autorentals.logging.svc;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import info.ejava.alamnr.assignment1.autorentals.logging.exception.ClientErrorException;
import info.ejava.alamnr.assignment1.autorentals.logging.repo.AutoRentalDTO;
import info.ejava.alamnr.assignment1.autorentals.logging.repo.AutoRentalsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class AutoRentalsServiceImpl implements AutoRentalsService {

    @Autowired
    private final AutoRentalsHelper autoRentalsHelper;
    private final AutoRentalsRepository repository;
    @Override
    public BigDecimal calcDelta(String autoId, String renterId) {
        log.trace("autoId in service - {}", autoId);
        validate(autoId);
        AutoRentalDTO leader = repository.getLeaderByAutoId(autoId);
        
        log.trace("autoId in service - {}", autoId);
        validate(renterId);
        AutoRentalDTO target = repository.getByRenterId(renterId);
        log.info("leader and target dto are - {}  and {}",leader,target);
        BigDecimal amount =  autoRentalsHelper.calcDelta(leader, target);
        log.info("amount in service - {}", amount);
        return amount;
    }

    protected void validate(String id){
       if(StringUtils.isEmpty(id))
            throw new ClientErrorException.InvalidInputException("Id can not be empty or null", id);
       
    }

}    

