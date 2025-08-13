package info.ejava.assignments.api.autorenters.svc.renters;

import info.ejava.examples.common.web.BaseExceptionAdvice;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = RentersController.class)
public class RentersExceptionAdvice extends BaseExceptionAdvice {
}
