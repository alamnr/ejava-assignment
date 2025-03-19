package info.ejava.alamnr.assignment0.app.autorentals;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AutoRentalCommand implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        System.out.println("AutoRentals has started");
    }

}
