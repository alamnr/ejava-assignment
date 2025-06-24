package info.ejava.assignments.api.autorenters.svc.autorentals;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import info.ejava.assignments.api.autorenters.dto.autos.AutoDTO;
import info.ejava.assignments.api.autorenters.dto.rentals.AutoRentalDTO;
import info.ejava.assignments.api.autorenters.dto.rentals.RentalSearchParams;
import info.ejava.assignments.api.autorenters.svc.POJORepositoryMapImpl;

public class AutoRentalDTORepositoryMapImpl  extends POJORepositoryMapImpl<AutoRentalDTO>
    implements AutoRentalDTORepository {

    public AutoRentalDTORepositoryMapImpl() {
        super(autoRenter -> autoRenter.getId(), (autoRenter,id)->autoRenter.setId(id), "autoRental-");
    }

    /*
      * Returns autos that match the provided example prototype
      * @param example containing prototype value to match
      * @param pageable containing offset and limit unless unpaged
      * @return page of matching autos ordered by id
      */

    @Override
    public Page<AutoRentalDTO> findAll(Example<AutoRentalDTO> example, Pageable pageable) {
        
         AutoRentalDTO probe = example.getProbe();
         List<Predicate<AutoRentalDTO>> predicates = new ArrayList<>();
         if(probe.getId()!= null) {
            predicates.add(candidate -> Objects.equals(probe.getId(), candidate.getId()));
         }
         if(probe.getAmount() != null){
            predicates.add(candidate -> Objects.equals(probe.getAmount(), candidate.getAmount()));
         }
         if(probe.getEndDate()!=null){
            predicates.add(candidate -> Objects.equals(probe.getEndDate(), candidate.getEndDate()));
         }
         if(probe.getStartDate()!=null){
            predicates.add(candidate-> Objects.equals(probe.getStartDate(), candidate.getStartDate()));
         }
         if(probe.getRenterAge()!=null){
            predicates.add(candidate -> Objects.equals(probe.getRenterAge(), candidate.getRenterAge()));
         }
         if(probe.getRenterName()!=null){
            predicates.add(candidate -> Objects.equals(probe.getRenterName(), candidate.getRenterName()));
         }

         Predicate<AutoRentalDTO> query = predicates.stream().reduce(Predicate::and).orElse(x->true);
         return super.findAll(query,pageable);

    }

    @Override
    public Page<AutoRentalDTO> findAllBySearchParam(RentalSearchParams searchParams, Pageable pageable) {
      List<Predicate<AutoRentalDTO>> predicates = new ArrayList<>();
      if(searchParams.getAutoId() != null)     {
         predicates.add(candidate -> Objects.equals(searchParams.getAutoId(), candidate.getAutoId()));
      }
      if(searchParams.getRenterId() != null){
         predicates.add(candidate -> Objects.equals(searchParams.getRenterId(), searchParams.getRenterId()));
      }
      if(searchParams.getStartDate() != null){
         predicates.add(candidate -> !candidate.getStartDate().isBefore(searchParams.getStartDate()));
      }

      if(searchParams.getEndDate() != null ){
         predicates.add(candidate -> !candidate.getEndDate().isAfter(searchParams.getEndDate()));
      }

      Predicate<AutoRentalDTO> query = predicates.stream().reduce(Predicate::and).orElse(x->true);

      return super.findAll(query,pageable);
    }

    @Override
    public Page<AutoRentalDTO> findByRenterName(String renterName, Pageable pageable) {
      Predicate<AutoRentalDTO> findByRenterNameQuery = candidate -> Objects.equals(renterName, candidate.getRenterName());
      return super.findAll(findByRenterNameQuery, pageable);
    }
     
}
