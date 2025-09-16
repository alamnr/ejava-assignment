package info.ejava.alamnr.assignment3.aop;

import info.ejava.alamnr.assignment3.security.AutoRentalsSecurityApp;
import info.ejava.assignments.aop.autorenters.util.D3a_AspectSvcNTest;
import info.ejava.assignments.api.autorentals.svc.main.ProvidedApiAutoRenterTestConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes={AutoRentalsSecurityApp.class,
        ProvidedApiAutoRenterTestConfiguration.class})
@Slf4j
@ActiveProfiles({"test", "nosecurity"})//no aop profile!
@DisplayName("Part D3a1: No Aspect (Service)")
public class MyD3a1_NoAspectSvcNTest extends D3a_AspectSvcNTest
{

}

