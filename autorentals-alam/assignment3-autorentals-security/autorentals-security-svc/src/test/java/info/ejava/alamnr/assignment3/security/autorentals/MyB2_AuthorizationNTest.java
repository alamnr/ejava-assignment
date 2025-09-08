package info.ejava.alamnr.assignment3.security.autorentals;

import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import info.ejava.alamnr.assignment3.security.AutoRentalsSecurityApp;
import info.ejava.alamnr.assignment3.security.autorentals.impl.SecurityTestConfiguration;
import info.ejava.assignments.security.autorenters.svc.rentals.B2_AuthorizationNTest;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest(classes= {
        AutoRentalsSecurityApp.class,
        SecurityTestConfiguration.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test","authorities", "authorization"})
@Slf4j
@DisplayName("Part B2: Authorization")
//@Disabled

public class MyB2_AuthorizationNTest extends B2_AuthorizationNTest {
    

}
