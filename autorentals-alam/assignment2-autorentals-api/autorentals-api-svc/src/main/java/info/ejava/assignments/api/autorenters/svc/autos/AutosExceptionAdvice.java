package info.ejava.assignments.api.autorenters.svc.autos;

import org.springframework.web.bind.annotation.RestControllerAdvice;

import info.ejava.examples.common.web.BaseExceptionAdvice;

@RestControllerAdvice(basePackageClasses = AutosController.class)
public class AutosExceptionAdvice extends BaseExceptionAdvice {
    
}
