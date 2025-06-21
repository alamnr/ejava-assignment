package info.ejava.assignments.api.autorenters.dto.rentals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import info.ejava.assignments.api.autorenters.dto.autos.AutoDTO;
import info.ejava.assignments.api.autorenters.dto.autos.AutoDTOFactory;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTOFactory;
import net.datafaker.Faker;

public class AutoRentalDTOFactory {

    private static final AtomicInteger ID   = new AtomicInteger();
    private final Faker faker = new Faker();

    private final AutoDTOFactory autoDTOFactory = new AutoDTOFactory();
    private final RenterDTOFactory renterDTOFactory = new RenterDTOFactory();

    public static String id() {
        return Integer.valueOf(ID.incrementAndGet()).toString();
    }


    public String username() {
        return faker.name().username();
    }

    public final AutoRentalDTO make(Consumer<AutoRentalDTO>... visitors){
        
        final AutoDTO auto = autoDTOFactory.make(AutoDTOFactory.withId);
        final RenterDTO renter = renterDTOFactory.make(RenterDTOFactory.withId);
        final TimePeriod timePeriod = new TimePeriod(LocalDate.now(), 1);
        final AutoRentalDTO result = new AutoRentalDTO(auto, renter, timePeriod);
        result.withAmount(BigDecimal.valueOf(80));
        //result.withUserName(username());
        Stream.of(visitors).forEach(v->v.accept(result));
        return result;
    }

    public static Consumer<AutoRentalDTO> withId = renter -> renter.setId(id());

    public class AutoRentalsDTOFactory {

        @SafeVarargs
        public final List<AutoRentalDTO> renters(int min, int max, Consumer<AutoRentalDTO>... visitors) {
            return IntStream.range(0, faker.number().numberBetween(min, max))
                    .mapToObj(i->AutoRentalDTOFactory.this.make(visitors))
                    .collect(Collectors.toList());
        }
        

        @SafeVarargs
        public final AutoRentalListDTO make(int min, int max, Consumer<AutoRentalDTO>... visitors) {
            return AutoRentalListDTO.builder()
                    .autoRentals(renters(min, max, visitors))
                    .build();
        }
    }

    public AutoRentalsDTOFactory listBuilder() {
        return new AutoRentalsDTOFactory();
    }

    
}
