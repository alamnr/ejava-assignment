package info.ejava.alamnr.assignment1.beanfactory.autorentals;

import info.ejava.alamnr.assignment1.beanfactory.autorentals.RentalDTO;
import info.ejava.alamnr.assignment1.beanfactory.autorentals.RentalsService;

public class AutoRentalsServiceImpl implements RentalsService {

    @Override
    public RentalDTO getRandomRental() {
        return new RentalDTO("autoRental0");
    }
    
}
