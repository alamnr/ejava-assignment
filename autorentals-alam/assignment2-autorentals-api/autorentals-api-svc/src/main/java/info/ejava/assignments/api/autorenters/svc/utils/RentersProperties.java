package info.ejava.assignments.api.autorenters.svc.utils;

import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Validated
public class RentersProperties {
    @NotNull
    private int minAge;
}
