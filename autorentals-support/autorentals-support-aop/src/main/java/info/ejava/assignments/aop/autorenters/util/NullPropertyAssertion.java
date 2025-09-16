package info.ejava.assignments.aop.autorenters.util;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import info.ejava.examples.common.exceptions.ClientErrorException;

//This class is complete. Student implements subclass implementing abstract methods.
public abstract class NullPropertyAssertion {
    
    public void assertNull(Object object, String property) {
        makeAssertion(object, property, true);
    }
    public void assertNotNull(Object object, String property) {
        makeAssertion(object, property, false);
    }

    public void assertConditions(Object object, List<String> properties, boolean isNull) {
        if (null!=object && null!=properties) {
            for (String property : properties) {
                makeAssertion(object, property, isNull);
            }
        }
    }

    protected void makeAssertion(Object obj, String property, boolean isNull){
        Method getterMethod = null;

        if (null==obj || null==property ||
                (getterMethod=getGetterMethod(obj, getterName(property)).orElse(null))==null) {
            return;
        }

        Object value = getValue(obj, getterMethod);
        if (isNull && null!=value) {
            throw new ClientErrorException.InvalidInputException(
                    "%s: must be null, value=%s", property, value);
        } else if (!isNull && null==value) {
            throw new ClientErrorException.InvalidInputException(
                    "%s: must not be null", property);
        }

    }

    protected String getterName(String property) {
        return "get" + StringUtils.capitalize(property);
    }

    /**
     * This method obtains a Method to the property getter using the class
     * of the provided object and getter method name.
     * @param object
     * @param getterName
     * @return method for getterName in class or null if does not exist
     */
    protected abstract Optional<Method> getGetterMethod(Object object, String getterName);

    /**
     * This method will return the value from the method.
     * @param object
     * @param getterMethod
     * @return result of calling method against object
     */
    protected abstract Object getValue(Object object, Method getterMethod);

}
