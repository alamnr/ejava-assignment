package info.ejava.alamnr.assignment1.propertysource.rentals;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import info.ejava.assignments.propertysource.autorentals.PropertyCheck;

//@SpringBootApplication(scanBasePackages = {"info.ejava.assignments.propertysource.autorentals"})
@SpringBootApplication(scanBasePackageClasses = PropertyCheck.class)
public class PropertySourceApp {
    
    private static ApplicationContext applicationContext;
    public static void main(String[] args) {
        applicationContext =  SpringApplication.run(PropertySourceApp.class, args);
        //displayAllBeans();
    }
    private static void displayAllBeans() {
        String[] allBeanNames = applicationContext.getBeanDefinitionNames();
        System.out.println("======================================");
        for(String beanName : allBeanNames) {
            System.out.println(beanName);   
        }        
    }
}
