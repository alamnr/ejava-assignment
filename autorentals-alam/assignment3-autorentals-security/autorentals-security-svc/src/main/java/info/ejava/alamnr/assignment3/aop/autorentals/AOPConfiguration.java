package info.ejava.alamnr.assignment3.aop.autorentals;



import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;

import info.ejava.assignments.aop.autorenters.util.MethodConstraints;
import info.ejava.assignments.aop.autorenters.util.NullPropertyAssertion;

import java.util.ArrayList;
import java.util.List;

@Configuration(proxyBeanMethods = false)
//TODO - Enable ...
//@EnableAspectJAutoProxy
public class AOPConfiguration {
    @Bean
    public NullPropertyAssertion nullPropertyAssertion() {
        return new NullPropertyAssertionImpl();
    }

    /**
     * This will construct a List of MethodConstraints from a property or
     * YAML file provided by the support modules.
     * MethodConstraints contains propert
     * @return
     */
    @Bean
    @ConfigurationProperties("aop.validation")
    public List<MethodConstraints> methodConstraints() {
        return new ArrayList<>();
    }

    @Bean
    @Profile("aop")
    public ValidatorAspect validatorAspect(NullPropertyAssertion nullPropertyAssertion,
                                           List<MethodConstraints> methodConstraints) {
        return new ValidatorAspect(nullPropertyAssertion, methodConstraints);
    }
}
