package info.ejava.assignments.api.autorenters.svc.autorentals;

import java.util.Optional;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import info.ejava.assignments.api.autorenters.dto.rentals.AutoRentalDTO;
import info.ejava.assignments.api.autorenters.dto.rentals.RentalSearchParams;
import info.ejava.assignments.api.autorenters.svc.POJORepository;

public interface AutoRentalDTORepository extends POJORepository<AutoRentalDTO> {

    Page<AutoRentalDTO> findAll(Example<AutoRentalDTO> example, Pageable pageable);

    Page<AutoRentalDTO> findAllBySearchParam(RentalSearchParams searchParams, Pageable pageable);

    Page<AutoRentalDTO> findByRenterName(String renterName, Pageable pageable);


    
}
