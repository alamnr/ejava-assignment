package info.ejava.assignments.api.autorenters.svc.autos;

import info.ejava.examples.common.web.BaseExceptionAdvice;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = AutosController.class)
public class AutosExceptionAdvice extends BaseExceptionAdvice {
}
