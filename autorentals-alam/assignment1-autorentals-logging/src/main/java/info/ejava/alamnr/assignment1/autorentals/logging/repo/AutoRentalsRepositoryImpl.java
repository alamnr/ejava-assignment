package info.ejava.alamnr.assignment1.autorentals.logging.repo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import info.ejava.alamnr.assignment1.autorentals.logging.exception.ClientErrorException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AutoRentalsRepositoryImpl  implements AutoRentalsRepository {

    private static final List<AutoRentalDTO> autorentals = Arrays.asList(
        AutoRentalDTO.builder().autoId("a1").renterId("r1").amount(BigDecimal.valueOf(200)).build(),
        AutoRentalDTO.builder().autoId("a2").renterId("r2").amount(BigDecimal.valueOf(300)).build(),
        AutoRentalDTO.builder().autoId("a3").renterId("r3").amount(BigDecimal.valueOf(400)).build());
      

    @Override
    public AutoRentalDTO getLeaderByAutoId(String autoId) {
        log.trace("autoId - {}", autoId);
        AutoRentalDTO expected = null;
        for (AutoRentalDTO autoRentalDTO : autorentals) {
            if(autoRentalDTO.getAutoId() == autoId)
            {
                expected = autoRentalDTO;
                
            }
        }
        log.trace("getByAutoId - {} ", expected);
        if(expected == null)
                throw new ClientErrorException.NotFoundException("leader with autiId[%s] not found",autoId);
        else
            return expected;

        

    }

    @Override
    public AutoRentalDTO getByRenterId(String renterId) {
        log.trace("renterId - {}", renterId);
        AutoRentalDTO expected = null;
        for (AutoRentalDTO autoRentalDTO : autorentals) {
            if(autoRentalDTO.getRenterId() == renterId)
            {
                expected = autoRentalDTO;
                
            }
        }
        log.trace("getByRenterId - {} ", expected);
        if(expected == null)
                throw new ClientErrorException.NotFoundException("target with renterId[%s] not found",renterId);
        else
            return expected;

        
    }
    
}
