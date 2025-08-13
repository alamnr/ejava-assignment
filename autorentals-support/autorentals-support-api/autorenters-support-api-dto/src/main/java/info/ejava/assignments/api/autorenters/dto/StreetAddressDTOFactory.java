package info.ejava.assignments.api.autorenters.dto;

import net.datafaker.Faker;
import net.datafaker.providers.base.Address;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class StreetAddressDTOFactory {
    private final Faker faker = new Faker();
    public Address address() { return faker.address(); }
    public String street() { return faker.address().streetAddress(); }
    public String city() { return faker.address().city(); }
    public String state() { return faker.address().stateAbbr(); }
    public String zip() { return faker.address().zipCode(); }

    @SafeVarargs
    public final StreetAddressDTO make(Consumer<StreetAddressDTO>... visitors) {
        Address address = address();
        final StreetAddressDTO result = StreetAddressDTO.builder()
                .street(address.streetAddress())
                .city(address.city())
                .state(address.stateAbbr())
                .zip(address.zipCode())
                .build();
        Stream.of(visitors).forEach(v->v.accept(result));
        return result;
    }

    public StreetAddressesDTOFactory listBuilder() { return new StreetAddressesDTOFactory(); }

    public class StreetAddressesDTOFactory {
        @SafeVarargs
        public final List<StreetAddressDTO> make(int count, Consumer<StreetAddressDTO>... visitors) {
            return IntStream.range(0, count)
                    .mapToObj(i-> StreetAddressDTOFactory.this.make(visitors))
                    .collect(Collectors.toList());
        }
    }
}
