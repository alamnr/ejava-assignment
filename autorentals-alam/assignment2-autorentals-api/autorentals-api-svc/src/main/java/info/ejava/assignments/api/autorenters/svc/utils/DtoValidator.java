package info.ejava.assignments.api.autorenters.svc.utils;

import java.util.List;

import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;

public interface DtoValidator {
    //List<String> validateNewRenter(RenterDTO renter, int minAge);
    <T> List<String> validateDto(T type, Integer minAge);
    
}
