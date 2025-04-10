package info.ejava.alamnr.assignment1.autoconfig.rentals;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import info.ejava.alamnr.assignment1.beanfactory.autorentals.RentalDTO;
import info.ejava.alamnr.assignment1.beanfactory.autorentals.RentalsService;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class AppCommand implements CommandLineRunner {
    
    @Getter
    private RentalsService rentalsService;
    private String rentalsActive;
    private String rentalsPreference;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("rentals.active=" + rentalsActive);
        System.out.println("rentals.preference=" + rentalsPreference);

        RentalDTO rental = null== rentalsService ? null : rentalsService.getRandomRental();
        String msg = null==rental ?
                "Rentals is not active" :
                String.format("Rentals has started, rental:%s", rental);
        System.out.println(msg);
    }

}
