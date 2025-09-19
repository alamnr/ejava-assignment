package info.ejava.alamnr.assignment3.aop.autorentals;

import info.ejava.assignments.aop.autorenters.util.NullPropertyAssertion;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ClassUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

@RequiredArgsConstructor
public class NullValidatorHandler implements InvocationHandler {
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

     /**
     * Intercepts proxy calls and validates before invoking target method.
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // Only intercept the configured method
        if (method.getName().equals(methodName)) {
            if (args != null && args.length > 0) {
                Object arg = args[0]; // assume first argument is DTO to validate

                // Validate required null properties
                for (String prop : isNullProperties) {
                    nullPropertyAssertion.assertNull(arg, prop);
                }

                // Validate required non-null properties
                for (String prop : nonNullProperties) {
                    nullPropertyAssertion.assertNotNull(arg, prop);
                }
            }
        }

        // Delegate to actual target method
        return method.invoke(target, args);
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
    // public static <T> T newInstance(
    //         NullPropertyAssertion nullPropertyAssertion,
    //         T target,
    //         String methodName,
    //         List<String> nullProperties,
    //         List<String> nonNullProperties) { //TODO
    //     ClassUtils.getAllInterfaces(target.getClass());
    //     new NullValidatorHandler(nullPropertyAssertion, target, methodName, nullProperties, nonNullProperties);
    //     return (T) null;
    // }
    /**
     * Factory method to create a new dynamic proxy instance.
     */
    @SuppressWarnings("unchecked")
    public static <T> T newInstance(
            NullPropertyAssertion nullPropertyAssertion,
            T target,
            String methodName,
            List<String> nullProperties,
            List<String> nonNullProperties) {

        return (T) Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                ClassUtils.getAllInterfaces(target.getClass()).toArray(new Class[0]),
                new NullValidatorHandler(nullPropertyAssertion, target, methodName, nullProperties, nonNullProperties)
        );
    }
}
