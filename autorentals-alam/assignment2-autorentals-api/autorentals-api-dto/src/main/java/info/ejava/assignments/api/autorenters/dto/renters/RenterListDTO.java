package info.ejava.assignments.api.autorenters.dto.renters;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RenterListDTO {
    private List<RenterDTO> contents = new ArrayList<>();
}
