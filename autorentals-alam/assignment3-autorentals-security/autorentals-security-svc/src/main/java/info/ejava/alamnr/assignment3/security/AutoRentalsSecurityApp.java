package info.ejava.alamnr.assignment3.security;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import info.ejava.alamnr.assignment3.aop.autorentals.AOPConfiguration;

@SpringBootApplication(scanBasePackageClasses = {
    AutoRentalsSecurityApp.class, //scan here
    AOPConfiguration.class     //scan AOP

})
public class AutoRentalsSecurityApp {
    
    public static void main(String[] args){
        SpringApplication.run(AutoRentalsSecurityApp.class, args);
    }
}
