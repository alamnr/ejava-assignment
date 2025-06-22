package info.ejava.assignments.api.autorenters.svc.autorentals;

import org.springframework.web.bind.annotation.RestControllerAdvice;

import info.ejava.examples.common.web.BaseExceptionAdvice;

@RestControllerAdvice(basePackageClasses = AutoRentalController.class)
public class AutoRentalExceptionAdvice extends BaseExceptionAdvice {
    
}
