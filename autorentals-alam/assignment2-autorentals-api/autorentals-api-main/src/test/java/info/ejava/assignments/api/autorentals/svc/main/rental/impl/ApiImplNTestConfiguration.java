package info.ejava.assignments.api.autorentals.svc.main.rental.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.client.RestTemplate;

import info.ejava.assignments.api.autorentals.svc.main.rental.ApiTestHelper;
import info.ejava.examples.common.web.ServerConfig;

//TODO: implement this @TestConfiguration for MyAutoRentalsAPINTest
/**
 * This test configuration will provide a factory bean for
 * the test-helper and any additional injections the test-helper
 * requires.
 */

 @TestConfiguration(proxyBeanMethods = false)

public class ApiImplNTestConfiguration {

    @Bean @Lazy
    public ApiTestHelper testHelper(@Autowired RestTemplate restTemplate, @Autowired ServerConfig serverConfig){
        return new ApiTestHelperImpl(restTemplate, serverConfig);
    }

    
    
}
