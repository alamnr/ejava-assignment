package info.ejava.alamnr.assignment3.aop.autorentals;

import info.ejava.assignments.aop.autorenters.util.NullPropertyAssertion;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ClassUtils;

import java.lang.reflect.Method;
import java.util.List;

@RequiredArgsConstructor
public class NullValidatorHandler /*implements ...*/ {
    private final NullPropertyAssertion nullPropertyAssertion;
    private final Object target;
    private final String methodName;
    private final List<String> isNullProperties;
    private final List<String> nonNullProperties;

    /**
     * Implement the handler method invoked by the dynamic proxy interpose.
     * @param proxy
     * @param method that was invoked by caller
     * @param args to the method invoked, supplied by caller
     * @return value returned from target.method() call if args valid
     * @throws info.ejava.examples.common.exceptions.ClientErrorException.InvalidInputException
     * if args not valid
     */
    //@Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return null; //TODO
    }

    /**
     * Creates a new dynamic interface proxy for target that will perform
     * nullProperty assertion logic against provided objects for named
     * methods.
     * @param nullPropertyAssertion validator for handler to validate with
     * @param target the object we are proxying
     * @param methodName method for the handler to process
     * @param nullProperties properties validated against isNull
     * @param nonNullProperties properties validated against notNull
     * @param <T> target object type
     * @return dynamic proxy implementing same interfaces as target
     */
    public static <T> T newInstance(
            NullPropertyAssertion nullPropertyAssertion,
            T target,
            String methodName,
            List<String> nullProperties,
            List<String> nonNullProperties) { //TODO
        ClassUtils.getAllInterfaces(target.getClass());
        new NullValidatorHandler(nullPropertyAssertion, target, methodName, nullProperties, nonNullProperties);
        return (T) null;
    }
}
