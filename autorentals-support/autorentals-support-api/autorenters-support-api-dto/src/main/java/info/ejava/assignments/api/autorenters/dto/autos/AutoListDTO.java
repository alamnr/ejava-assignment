package info.ejava.assignments.api.autorenters.dto.autos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AutoListDTO {
    private List<AutoDTO> contents = new ArrayList<>();
}
