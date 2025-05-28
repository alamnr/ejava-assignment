package info.ejava.assignments.api.autorenters.svc.renters;

import org.springframework.web.bind.annotation.RestControllerAdvice;

import info.ejava.examples.common.web.BaseExceptionAdvice;

@RestControllerAdvice(basePackageClasses = RentersController.class)
public class RentersExceptionAdvice extends BaseExceptionAdvice {
    
}
