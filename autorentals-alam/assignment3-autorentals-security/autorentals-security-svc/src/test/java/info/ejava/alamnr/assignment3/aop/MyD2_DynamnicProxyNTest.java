package info.ejava.alamnr.assignment3.aop;

import info.ejava.alamnr.assignment3.security.AutoRentalsSecurityApp;
import info.ejava.assignments.aop.autorenters.util.D2_DynamnicProxyNTest;
import info.ejava.assignments.api.autorentals.svc.main.ProvidedApiAutoRenterTestConfiguration;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes={AutoRentalsSecurityApp.class,
        ProvidedApiAutoRenterTestConfiguration.class})
@DisplayName("Part D2: Dynamic Proxies")
@Disabled
public class MyD2_DynamnicProxyNTest extends D2_DynamnicProxyNTest
{

}
