package info.ejava.assignments.api.autorenters.dto.renters;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import net.datafaker.Faker;

public class RenterDTOFactory {

    private static final AtomicInteger ID   = new AtomicInteger();
    private final Faker faker = new Faker();

    public static String id() {
        return Integer.valueOf(ID.incrementAndGet()).toString();
    }

    public String firstName() { 
        return faker.name().firstName();
    }

    public String lastName(){
        return faker.name().lastName();
    }

    public LocalDate dob() {
        return LocalDate.ofInstant(faker.date().birthday(21, 99).toInstant(), ZoneOffset.UTC);
    }

    public String email() {
        return faker.internet().emailAddress();
    }

    public String username() {
        return faker.name().username();
    }

    public final RenterDTO make(Consumer<RenterDTO>... visitors){
        final String firstName = firstName();
        final String lastName = lastName();
        final String account = (firstName+"."+lastName).toLowerCase().replaceAll("'", "");
        final String email = email().replaceAll("(.*)@", account+"@");
        final RenterDTO result = RenterDTO.builder().id(null).firstName(firstName).lastName(lastName).dob(dob())
                                            .email(email).build();
        Stream.of(visitors).forEach(v->v.accept(result));
        return result;
    }

    public static Consumer<RenterDTO> withId = renter -> renter.setId(id());

    public class RentersDTOFactory {
        public final List<RenterDTO> make(int count, Consumer<RenterDTO>... visitors){
            return IntStream.range(0, count)
                .mapToObj(i -> RenterDTOFactory.this.make(visitors))
                .collect(Collectors.toList());
        }
    }

    public RentersDTOFactory listBuilder() {
        return new RentersDTOFactory();
    }

    
}
