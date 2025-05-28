package info.ejava.assignments.api.autorenters.svc.utils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import org.apache.commons.lang3.ObjectUtils;

import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RenterValidatorImpl implements RenterValidator {
    
    @Override
    public List<String> validateNewRenter(RenterDTO renter, int minAge) {
        
        List<String> errMsgs = new ArrayList<>();
        log.trace(" validating - {} and minAge - {}", renter, minAge);
        if(renter == null){
            errMsgs.add("renter: can not be null");
        } else {
            validate(errMsgs, ()->renter.getId()==null,()->"id must be null");
            validate(errMsgs, ()-> !ObjectUtils.isEmpty(renter.getFirstName()), ()->"renter.FirtstName - can not be blnk");
            validate(errMsgs, ()-> !ObjectUtils.isEmpty(renter.getLastName()), ()->"renter.LastName - can not be blank");
            Optional.ofNullable(renter.getDob()).ifPresentOrElse((dob)->{
                LocalDate minDob = LocalDate.now().minusYears(minAge);
                validate(errMsgs, ()->dob.isBefore(minDob), ()->String.format("renter.DOB must be greater than %d years", minAge));
            }, ()-> errMsgs.add("renter.DOB can not be null"));
        }

        log.trace("renter - {} , valid - {}, errors-{}", renter, errMsgs.isEmpty(), errMsgs);
        return errMsgs;
    }

    private void validate(List<String> errMsgs, BooleanSupplier booleanSupplier, Supplier<String> errorMsg) {
        if(!booleanSupplier.getAsBoolean()){
            errMsgs.add(errorMsg.get());
        }
    }

}
