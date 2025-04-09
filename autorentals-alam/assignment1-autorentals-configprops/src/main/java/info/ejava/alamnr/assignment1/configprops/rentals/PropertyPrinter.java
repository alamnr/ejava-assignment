package info.ejava.alamnr.assignment1.configprops.rentals;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import info.ejava.alamnr.assignment1.configprops.rentals.properties.BoatRentalProperties;
import info.ejava.alamnr.assignment1.configprops.rentals.properties.RentalProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Component
@AllArgsConstructor
@Getter // for unit testing
public class PropertyPrinter implements CommandLineRunner {

    private final List<RentalProperties> autos;
    private final List<RentalProperties> tools;
    private final BoatRentalProperties boat;

//   @Autowired  @Qualifier("autosRentals") private  List<RentalProperties> autos;
//   @Autowired  @Qualifier("toolsRentals") private  List<RentalProperties> tools;
//   @Autowired  private  BoatRentalProperties boat;


     @Override
    public void run(String... args) throws Exception {
        System.out.println("autos:" + format(autos));
        System.out.println("tools:" + format(tools));
        System.out.println("boat:" + format(null==boat ? null : List.of(boat)));
    }

    private String format(List<?> rentals) {
        return null==rentals ? "(null)" :
            String.format("%s", rentals.stream()
                .map(r->"*" + r.toString())
                .collect(Collectors.joining(System.lineSeparator(), System.lineSeparator(), "")));
    }

    
}
