package info.ejava.assignments.aop.autorenters.util;

import lombok.Data;

import java.util.List;

@Data
public class MethodConstraints {
    private String methodName;
    private List<String> isNull;
    private List<String> notNull;
}
