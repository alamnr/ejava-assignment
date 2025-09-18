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
        if (object == null || getterName == null || getterName.isBlank()) {
            return Optional.empty();
        }
        try {
            Method method = object.getClass().getMethod(getterName);
            return Optional.of(method);
        } catch (NoSuchMethodException | SecurityException e) {
            return Optional.empty();
        }
    }

    /**
     * Return the value returned from the getter method and report
     * any errors that with a server-type error.
     */
    @Override
    protected Object getValue(Object object, Method getterMethod) {
        
        if (object == null || getterMethod == null) {
            return null;
        }
        try {
            return getterMethod.invoke(object);
        } catch (Exception e) {
            throw new RuntimeException(
                "Unable to invoke method " + getterMethod.getName() +
                " on class " + object.getClass().getName(), e);
        }
    }
}
