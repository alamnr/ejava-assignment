package info.ejava.alamnr.assignment3.security.autorentals;

import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import info.ejava.alamnr.assignment3.security.AutoRentalsSecurityApp;
import info.ejava.alamnr.assignment3.security.autorentals.impl.SecurityTestConfiguration;
import info.ejava.assignments.security.autorenters.svc.rentals.A2b_NoSecurityNTest;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest(classes = {
    AutoRentalsSecurityApp.class,
    SecurityTestConfiguration.class
}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test","nosecurity"})
@Slf4j
@DisplayName("Part A2b: No Security")
// @Disabled
public class MyA2b_NoSecurityNTest extends A2b_NoSecurityNTest {
    
}
