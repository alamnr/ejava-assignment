package info.ejava.alamnr.assignment3.aop.autorentals;

import info.ejava.assignments.aop.autorenters.util.MethodConstraints;
import info.ejava.assignments.aop.autorenters.util.NullPropertyAssertion;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.aspectj.lang.annotation.Aspect;

@RequiredArgsConstructor
//@Aspect
public class ValidatorAspect {
    private final NullPropertyAssertion nullPropertyAssertion;
    private final List<MethodConstraints> methodConstraints;

    //pointcut

    //advice
}
