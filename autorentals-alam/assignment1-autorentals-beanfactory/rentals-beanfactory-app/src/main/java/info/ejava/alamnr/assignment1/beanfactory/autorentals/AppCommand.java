package info.ejava.alamnr.assignment1.beanfactory.autorentals;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

import info.ejava.alamnr.assignment1.beanfactory.autorentals.RentalDTO;
import info.ejava.alamnr.assignment1.beanfactory.autorentals.RentalsService;

public class AppCommand implements CommandLineRunner  {


    private final RentalsService rentalsService;

    public AppCommand(RentalsService rentalsService){
        this.rentalsService = rentalsService;
    }

    @Override
    public void run(String... args) throws Exception {
        RentalDTO rental = rentalsService.getRandomRental();
        String msg = String.format("Rentals has started, rental: %s",   rental);
        System.out.println(msg);
    }
    
}
