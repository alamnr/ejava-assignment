package info.ejava.alamnr.assignment3.security.autorentals;

import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import info.ejava.alamnr.assignment3.security.AutoRentalsSecurityApp;
import info.ejava.alamnr.assignment3.security.autorentals.impl.SecurityTestConfiguration;
import info.ejava.assignments.security.autorenters.svc.rentals.A3_UserDetailsNTest;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest(classes= {
        AutoRentalsSecurityApp.class,
        SecurityTestConfiguration.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)

// [ERROR]   A3_UserDetailsNTest$with_identities.valid_credentials_can_authenticate:122 
// expected: "ted" but was: "(null)"
//@ActiveProfiles({"test","userdetails","nosecurity"}) 

// [ERROR]   A3_UserDetailsNTest$create_rental.cleanUp:202 authenticated user failed to delete all in profile that does not have roles; if this worked eariler, make sure 
// you have applied authorization checks in a way that will not be active during profile(s): [test, userdetails]
//@ActiveProfiles({"test","userdetails"}) 

@ActiveProfiles({"test","userdetails","authorities"})  //  all test pass with this profile setup

@Slf4j
@DisplayName("Part A3: User Details")
//@Disabled

public class MyA3_UserDetailsNTest extends A3_UserDetailsNTest {
    
}
