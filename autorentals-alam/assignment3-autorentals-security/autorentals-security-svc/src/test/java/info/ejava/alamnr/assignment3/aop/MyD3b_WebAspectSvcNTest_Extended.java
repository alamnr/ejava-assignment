package info.ejava.alamnr.assignment3.aop;



import info.ejava.alamnr.assignment3.security.AutoRentalsSecurityApp;
import info.ejava.assignments.aop.autorenters.util.D3a_AspectSvcNTest;
import info.ejava.assignments.api.autorentals.svc.main.auto.AutoTestConfiguration;
import info.ejava.assignments.api.autorenters.client.autos.AutosAPIClient;
import info.ejava.assignments.api.autorenters.client.renters.RentersAPIClient;
import info.ejava.assignments.api.autorenters.dto.autos.AutoDTOFactory;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTOFactory;
import info.ejava.assignments.security.autorenters.svc.ProvidedAuthorizationTestHelperConfiguration;

import lombok.extern.slf4j.Slf4j;

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes={AutoRentalsSecurityApp.class,
        ProvidedAuthorizationTestHelperConfiguration.class
        //AutoTestConfiguration.class
    },
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
@ActiveProfiles({"test", "authorizations", "authentication", "aop"})//aop profile!
@DisplayName("Part D3b: Web Aspect (Service)")
//@Disabled
public class MyD3b_WebAspectSvcNTest_Extended
{
    @Autowired
    private AutoDTOFactory autoFactory;
    @Autowired
    private RenterDTOFactory renterFactory;
    @Autowired @Qualifier("autosAPIClient")
    private AutosAPIClient autosService;
    @Autowired @Qualifier("rentersAPIClient")
    private RentersAPIClient rentersService;
    @Autowired
    private Environment env;

    @Test
    void context() {
        BDDAssertions.then(rentersService).isNotNull();
    }
  
}
