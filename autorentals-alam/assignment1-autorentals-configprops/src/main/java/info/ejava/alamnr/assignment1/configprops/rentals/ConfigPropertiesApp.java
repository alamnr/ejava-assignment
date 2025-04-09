package info.ejava.alamnr.assignment1.configprops.rentals;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import info.ejava.alamnr.assignment1.configprops.rentals.properties.BoatRentalProperties;
import info.ejava.alamnr.assignment1.configprops.rentals.properties.RentalProperties;

@SpringBootApplication
public class ConfigPropertiesApp {
    public static void main(String[] args) {
        SpringApplication.run(ConfigPropertiesApp.class, args);
    }

    
    @Bean
    @ConfigurationProperties("rentals.autos")
    public List<RentalProperties> autos() {
        return new ArrayList<>();
    }

    
    // @Bean
    // @ConfigurationProperties("rentals.autos")
    // public List<RentalProperties> autosRentals() {
    //     return new ArrayList<>();
    // }
     
    @Bean
    @ConfigurationProperties("rentals.tools")
    public List<RentalProperties> tools() {
        return new ArrayList<>();
    }

    // @Bean
    // @ConfigurationProperties("rentals.tools")
    // public List<RentalProperties> toolsRentals() {
    //     return new ArrayList<>();
    // }

    @Bean
    @ConfigurationProperties("boatrental")
    public BoatRentalProperties boat(){
        return new BoatRentalProperties();
    }
}

