package info.ejava.alamnr.assignment1.autorentals.logging.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;

import info.ejava.alamnr.assignment1.autorentals.logging.svc.AutoRentalsService;
import info.ejava.alamnr.assignment1.autorentals.logging.svc.AutoRentalsServiceImpl;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AppCommand implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger("X.Y");
    private final AutoRentalsService autoRentalsService;
    private final String autoId;
    private final String renterId;

    @Override
    public void run(String... args) throws Exception {
        log.info("Autorentals has started");
        try {
             log.info("Auto Id - {}  , Renter Id - {} ",autoId,renterId);
            log.info("CalcDelta - {}  ", autoRentalsService.calcDelta(autoId, renterId));
            //log.info("CalcDelta - {}  ", autoRentalsService.calcDelta("a1", "r1"));

        } catch(Exception ex){
            log.error("Exception occured - ", ex);
        }
        
        log.info("Autorentals has ended");
    }
    
}
