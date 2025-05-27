package info.ejava.assignments.api.autorenters.dto.autos;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AutoListDTO {
    private List<AutoDTO> contents = new ArrayList<>();
    
}
