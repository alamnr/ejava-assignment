package info.ejava.assignments.security.autorenters.svc.autos;

import java.util.List;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.util.Assert;

import info.ejava.assignments.api.autorenters.svc.autos.AutosAPIConfiguration;
import info.ejava.assignments.api.autorenters.svc.autos.AutosService;
import info.ejava.assignments.security.autorenters.svc.Accounts;
import info.ejava.assignments.security.autorenters.svc.AuthorizationHelper;
import lombok.extern.slf4j.Slf4j;


@Configuration(proxyBeanMethods = false )
@AutoConfigureBefore(AutosAPIConfiguration.class)
@Slf4j
public class AutosSecurityConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AuthorizationHelper authzHelper(){
        return new AuthorizationHelper();
    }

    @Bean
    @ConfigurationProperties("autorenters")
    @ConditionalOnMissingBean
    public Accounts rentalAccounts(){
        return new Accounts();
    }

    @Primary
    @Bean
    @Profile("nosecurity")
    public AutosService nosecureAutosService(List<AutosService> impls){
        Assert.notEmpty(impls,"no AutosService impls found to secure");
        log.info("nosecurity profile active, allowing all calls to autos service");
        return new NoSecurityAutosServiceWrapper((impls.get(0))); //use highest priority

    }

    @Primary
    @Bean
    @Profile("!nosecurity")
    public AutosService secureAutosService(List<AutosService> impls, AuthorizationHelper authzHelper) {
        Assert.notEmpty(impls,"no AutosService impls found to secure");
        AutosService wrappedService = impls.get(0); //use highest priority
        return new SecureAutosServiceWrapper(wrappedService, authzHelper);
    }

}