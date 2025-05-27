package info.ejava.assignments.api.autorenters.dto.autos;

import java.util.stream.Stream;

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import info.ejava.examples.common.dto.DtoUtil;
import info.ejava.examples.common.dto.JsonUtil;
import lombok.extern.slf4j.Slf4j;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Slf4j
public class AutoDTOTest {

    private DtoUtil dtoUtil = new JsonUtil();
    private AutoDTOFactory autoDTOFactory = new AutoDTOFactory();

    private Stream<Arguments> autos() {
        return Stream.of(
            Arguments.of(autoDTOFactory.make()),
            Arguments.of(autoDTOFactory.make(AutoDTOFactory.withId)),
            Arguments.of(new AutoListDTO(autoDTOFactory.listBuilder().make(3, AutoDTOFactory.withId)))
        );
    }

    @ParameterizedTest
    @MethodSource("autos")
    void can_marshal_demarshal(Object original){
        // when 
        String payload =  dtoUtil.marshal(original);
        log.info("payload- {}", payload);
        Object copy = dtoUtil.unmarshal(payload, original.getClass());
        // then
        BDDAssertions.then(copy).isEqualTo(original);
    }
    
}
