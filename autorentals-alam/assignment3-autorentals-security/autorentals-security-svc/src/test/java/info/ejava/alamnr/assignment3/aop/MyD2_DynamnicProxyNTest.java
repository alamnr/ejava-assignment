package info.ejava.alamnr.assignment3.aop;

import info.ejava.alamnr.assignment3.security.AutoRentalsSecurityApp;
import info.ejava.assignments.aop.autorenters.util.D2_DynamnicProxyNTest;
import info.ejava.assignments.api.autorentals.svc.main.ProvidedApiAutoRenterTestConfiguration;
import info.ejava.assignments.api.autorentals.svc.main.auto.AutoTestConfiguration;
import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes={AutoRentalsSecurityApp.class,  AutoTestConfiguration.class})
@DisplayName("Part D2: Dynamic Proxies")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Slf4j
public class MyD2_DynamnicProxyNTest extends D2_DynamnicProxyNTest
{

}
