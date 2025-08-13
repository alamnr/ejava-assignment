package info.ejava.assignments.security.autorenters.svc.renters;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.util.Assert;

import info.ejava.assignments.api.autorenters.svc.renters.RentersService;
import info.ejava.assignments.security.autorenters.svc.AuthorizationHelper;
import lombok.extern.slf4j.Slf4j;

@Configuration(proxyBeanMethods = false)
@Slf4j
public class RentersSecurityConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public AuthorizationHelper authzHelper(){
        return new AuthorizationHelper();
    }

    @Primary
    @Bean
    @Profile("nosecurity")
    public RentersService nosecureRentersServiceNoSecurity(List<RentersService> impls) {
        Assert.notEmpty(impls,"no RenterService impls found to secure");
        log.info("nosecurity profile active, allowing all calls to renter service");
        return new NoSecurityRentersServiceWrapper(impls.get(0));
    }

    @Primary
    @Bean
    @Profile("!nosecurity")
    public RentersService secureRentersService(List<RentersService> impls, AuthorizationHelper authzHelper) {
        Assert.notEmpty(impls,"no RenterService impls found to secure");
        return new SecureRentersServiceWrapper(impls.get(0), authzHelper);
    }

}
