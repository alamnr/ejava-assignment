package info.ejava.alamnr.assignment3.security.autorentals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import info.ejava.alamnr.assignment3.security.AutoRentalsSecurityApp;
import info.ejava.alamnr.assignment3.security.autorentals.impl.SecurityTestConfiguration;
import info.ejava.assignments.security.autorenters.svc.rentals.B1_AuthoritiesNTest;

@SpringBootTest(classes= {
        AutoRentalsSecurityApp.class,
        SecurityTestConfiguration.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test", "authorities"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Part B1: Authorities")
//@Disabled
public class MyB1_AuthoritiesNTest extends B1_AuthoritiesNTest {
    
}
