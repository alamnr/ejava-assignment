package info.ejava.assignments.api.autorenters.dto.autos;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import info.ejava.assignments.api.autorenters.dto.StreetAddressDTO;
import info.ejava.assignments.api.autorenters.dto.StreetAddressDTOFactory;
import net.datafaker.Faker;
import net.datafaker.providers.base.Vehicle;

public class AutoDTOFactory {
    private static final AtomicInteger ID = new AtomicInteger();
    
    private final Faker faker = new Faker();

    public static String id(){
        return Integer.valueOf(ID.incrementAndGet()).toString();
    }

    public int passengers() {
        return faker.number().numberBetween(1, 5);
    }

    public int passengers(Vehicle vehicle) {
        int doors = Integer.parseInt(vehicle.doors());
        return doors > 1 && doors < 4 ? 5 : doors; 
    }

    public String fuelType() {
        return FuelType.values()[faker.number().numberBetween(0, FuelType.values().length-1)].getText();
    }

    public BigDecimal dailyRate(){
        return BigDecimal.valueOf(faker.number().numberBetween(20, 100)).setScale(2, RoundingMode.UNNECESSARY);
    }

    public Vehicle vehicle() { 
        return faker.vehicle();
    }

    private final StreetAddressDTOFactory locationFactory = new StreetAddressDTOFactory();

    public StreetAddressDTO location() {
        return locationFactory.make();
    }

    public String username(){
        return faker.name().username().replaceAll("\\.", "");
    }

    public final AutoDTO make(Consumer<AutoDTO>... visitors){
        Vehicle vehicle = vehicle();
        AutoDTO result = AutoDTO.builder().id(null).dailyRate(dailyRate()).fuelType(fuelType()).make(vehicle.make())
                                .model(vehicle.model()).passengers(passengers(vehicle)).location(location()).build();
        Stream.of(visitors).forEach(v->v.accept(result));
        return result;
    }

    public static Consumer<AutoDTO> withId = auto -> auto.setId(id());

    public class AutosDTOFactory {
        public final List<AutoDTO> make(int count, Consumer<AutoDTO>... visitors){
            return IntStream.range(0, count)
                        .mapToObj(i -> AutoDTOFactory.this.make(visitors))
                        .collect(Collectors.toList());
        }
    }

    public AutosDTOFactory listBuilder() {
        return new AutosDTOFactory();
    }
}
