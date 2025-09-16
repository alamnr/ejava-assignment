package info.ejava.alamnr.assignment3.aop.autorentals;



import info.ejava.assignments.aop.autorenters.util.NullPropertyAssertion;

import java.lang.reflect.Method;
import java.util.Optional;

public class NullPropertyAssertionImpl extends NullPropertyAssertion {
    /**
     * Return the named Method for the object or empty if
     * method does not exist.
     */
    @Override
    protected Optional<Method> getGetterMethod(Object object, String getterName) {
        return null; //TODO
    }

    /**
     * Return the value returned from the getter method and report
     * any errors that with a server-type error.
     */
    @Override
    protected Object getValue(Object object, Method getterMethod) {
        return null; //TODO
    }
}
