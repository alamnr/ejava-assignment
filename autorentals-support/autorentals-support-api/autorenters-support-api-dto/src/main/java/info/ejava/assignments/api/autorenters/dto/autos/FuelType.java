package info.ejava.assignments.api.autorenters.dto.autos;

import lombok.Getter;

public enum FuelType {
    GAS("Gasoline"),
    ELECTRIC("Electric"),
    HYRBID("Hybrid");

    @Getter
    private final String text;
    private FuelType(String text) {
        this.text=text;
    }
}
