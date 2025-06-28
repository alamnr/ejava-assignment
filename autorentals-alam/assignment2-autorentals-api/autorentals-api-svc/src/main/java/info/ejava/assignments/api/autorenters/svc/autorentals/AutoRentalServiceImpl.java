package info.ejava.assignments.api.autorenters.svc.autorentals;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import info.ejava.assignments.api.autorenters.dto.autos.AutoDTO;
import info.ejava.assignments.api.autorenters.dto.rentals.AutoRentalDTO;
import info.ejava.assignments.api.autorenters.dto.rentals.RentalSearchParams;
import info.ejava.assignments.api.autorenters.dto.rentals.TimePeriod;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
import info.ejava.assignments.api.autorenters.svc.autos.AutosDTORepository;
import info.ejava.assignments.api.autorenters.svc.renters.RenterDTORepository;
import info.ejava.assignments.api.autorenters.svc.utils.DtoValidator;
import info.ejava.examples.common.exceptions.ClientErrorException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class AutoRentalServiceImpl  implements AutoRentalService {

    private final AutoRentalDTORepository autoRentalRepository;
    private final AutosDTORepository autoRepository;
    private final RenterDTORepository renterRepository;
    private final DtoValidator dtoValidator;

    @Override
    public AutoRentalDTO createAutoRental(AutoRentalDTO autoRental) {
        //log.info("##################################### received in service autoRental save - {} ", autoRental);
        final Optional<AutoDTO> auto = autoRepository.findById(autoRental.getAutoId());
        if(!auto.isPresent()){
            throw new ClientErrorException.InvalidInputException("autoRental is not valid - autoId[%s] does not exist", 
                                                                            autoRental.getAutoId());
        }
        final Optional<RenterDTO> renter = renterRepository.findById(autoRental.getRenterId());
        if(!renter.isPresent()){
            throw new ClientErrorException.InvalidInputException("autoRental is not valid - renterId[%s] does not exist", 
                                                                            autoRental.getRenterId());
        }
        final TimePeriod timePeriod = new TimePeriod(autoRental.getStartDate(), 
                                autoRental.getEndDate() != null ? autoRental.getEndDate() : autoRental.getStartDate());
        
        autoRental = new AutoRentalDTO(auto.get(), renter.get(), timePeriod);

        validateAutoRental(autoRental);
        isOverlapTimePeriod(autoRental);
        AutoRentalDTO savedAutoRental = autoRentalRepository.save(autoRental);
        log.debug("added autoRental - {}", savedAutoRental);
        return savedAutoRental;
    }

    @Override
    public AutoRentalDTO updateAutoRental(String id, AutoRentalDTO autoRental) {
        
        //log.info("##################################### received in service autoRental update - {} ", autoRental);
        final Optional<AutoDTO> auto = autoRepository.findById(autoRental.getAutoId());
        if(!auto.isPresent()){
            throw new ClientErrorException.InvalidInputException("autoRental is not valid - autoId[%s] does not exist", 
                                                                            autoRental.getAutoId());
        }
        final Optional<RenterDTO> renter = renterRepository.findById(autoRental.getRenterId());
        if(!renter.isPresent()){
            throw new ClientErrorException.InvalidInputException("autoRental is not valid - renterId[%s] does not exist", 
                                                                            autoRental.getRenterId());
        }
        final TimePeriod timePeriod = new TimePeriod(autoRental.getStartDate(), 
                                autoRental.getEndDate() != null ? autoRental.getEndDate() : autoRental.getStartDate());
        
        autoRental = new AutoRentalDTO(auto.get(), renter.get(), timePeriod);


        validateAutoRental(autoRental);
        isOverlapTimePeriod(autoRental);
        if(null == autoRental){
            throw new ClientErrorException.InvalidInputException("autoRental is required");
        }
        if(null !=id && autoRentalRepository.existsById(id)){
            autoRental.setId(id);
        } else {
            throw new ClientErrorException.InvalidInputException("autoRental-[%s] not found", id);
        }
        log.debug("updating auto id - {} , {}",id, autoRental);
        return autoRentalRepository.save(autoRental);
        
    }

    @Override
    public AutoRentalDTO getAutoRental(String id) {
        return autoRentalRepository.findById(id)
                .orElseThrow(()->{
                    log.debug("getAutoRental({}) not found ", id);
                    throw new ClientErrorException.NotFoundException("autoRental[%s] not found", id);
                });
    }

    @Override
    public boolean hasAutoRental(String id) {
        return autoRentalRepository.existsById(id);
    }

    

    @Override
    public void removeAutoRental(String id) {
        autoRentalRepository.deleteById(id);
    }

    @Override
    public void removeAllAutoRental() {
        autoRentalRepository.deleteAll();
    }

    @Override
    public Page<AutoRentalDTO> queryAutoRental(AutoRentalDTO probe, Pageable pageable) {
         Page<AutoRentalDTO> autoRentals  = autoRentalRepository.findAll(Example.of(probe), pageable);
        if(pageable.isPaged()){
            log.debug("pageNumber-{} / pageSize-{}, returns -{}", pageable.getPageNumber(), pageable.getPageSize(), autoRentals.getNumberOfElements());
        } else {
            log.debug("non page returns - {}", autoRentals.getNumberOfElements());
        }

        return autoRentals;
    }

    @Override
    public Page<AutoRentalDTO> searchAutoRental(RentalSearchParams searchParams, Pageable pageable) {
        Page<AutoRentalDTO> autoRentals = autoRentalRepository.findAllBySearchParam(searchParams, pageable);
        
        if(pageable.isPaged()){
            log.debug("pageNumber -{} / page size - {} / returns -{}", pageable.getPageNumber(), pageable.getPageSize(), autoRentals.getNumberOfElements());
        } else {
            log.debug("non page returns - {}", autoRentals.getNumberOfElements());
        }
        return autoRentals;
    }

    @Override
    public Page<AutoRentalDTO> getAutoRentals(Pageable pageable) {
        Page<AutoRentalDTO> autoRentals = autoRentalRepository.findAll(pageable);
        if(pageable.isPaged()){
            log.debug("pageSize - {}/ pageNumber-{}, returns per page - {}", pageable.getPageSize(),
                            pageable.getPageNumber(), autoRentals.getNumberOfElements());            
        } else {
            log.debug("nonPaged returns total - {}", autoRentals.getNumberOfElements());
        }
        return autoRentals;
    }

    @Override
    public Page<AutoRentalDTO> findByRenterName(String renterName, Pageable pageable) {
       
       return autoRentalRepository.findByRenterName(renterName, pageable);


    }

    private void validateAutoRental(AutoRentalDTO autoRental) {
       List<String> errMsg = dtoValidator.validateDto(autoRental, null);
       if(!errMsg.isEmpty()){
        throw new ClientErrorException.InvalidInputException("auto rental is not valid - %s", errMsg);
       }
    }

    private void isOverlapTimePeriod(AutoRentalDTO autoRentalDTO){
        TimePeriod timePeriod = TimePeriod.builder().startDate(autoRentalDTO.getStartDate()).endDate(autoRentalDTO.getEndDate()).build();
        RentalSearchParams searchParams = RentalSearchParams.builder().autoId(autoRentalDTO.getAutoId())
                                                                        .timePeriod(timePeriod).build();
        Page<AutoRentalDTO> rentalPage =  autoRentalRepository.findAllBySearchParam(searchParams, Pageable.unpaged());
        if(rentalPage.hasContent()){
            throw new ClientErrorException.InvalidInputException("autoRental time period overlap, use another date. Given overlap time- %s",
                                                     timePeriod.getStartDate().toString());
        }
    }
    
}
