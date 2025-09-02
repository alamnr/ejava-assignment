package info.ejava.assignments.security.autorenters.svc.autorentals;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.util.Assert;

import info.ejava.assignments.api.autorenters.svc.autorentals.AutoRentalService;
import info.ejava.assignments.api.autorenters.svc.autos.AutosService;
import info.ejava.assignments.api.autorenters.svc.renters.RentersService;
import info.ejava.assignments.security.autorenters.svc.AuthorizationHelper;
import lombok.extern.slf4j.Slf4j;

@Configuration(proxyBeanMethods = false)
@Slf4j
public class AutoRentalSecurityConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public AuthorizationHelper authzHelper(){
        return new AuthorizationHelper();
    }

    @Primary
    @Bean
    @Profile("nosecurity")
    public AutoRentalService nosecureAutoRentalsService(List<AutoRentalService> serviceImpls) {
        Assert.notEmpty(serviceImpls,"no RenterService impls found to secure");
        log.info("nosecurity profile active, allowing all calls to renter service");
        
        return new NoSecurityAutoRentalServiceWrapper(serviceImpls.get(0)); 
    }

    @Primary
    @Bean
    @Profile("!nosecurity")
    public AutoRentalService secureAutoRentalsService(List<AutoRentalService> serviceImpls,
                                                   AutosService autosService,
                                                   RentersService rentersService,
                                                   AuthorizationHelper authzHelper) {
        Assert.notEmpty(serviceImpls,"no RenterService impls found to secure");
        
        return new SecureAutoRentalServiceWrapper(serviceImpls.get(0),autosService,rentersService,authzHelper); 
    }
}
