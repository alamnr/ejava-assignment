package info.ejava.assignments.api.autorentals.svc.main.renter.repo;

import java.util.List;
import java.util.Optional;

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTOFactory;
import info.ejava.assignments.api.autorenters.svc.POJORepositoryMapImpl;
import lombok.extern.slf4j.Slf4j;

//@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Slf4j
public class RenterRepositoryMapImplTest {

    private POJORepositoryMapImpl<RenterDTO> repo;

    private RenterDTOFactory factory;

    @BeforeEach
    void init() {
        repo = new POJORepositoryMapImpl<>(renter -> renter.getId(), (renter, id)-> renter.setId(id), "renter-");
        factory  = new RenterDTOFactory();
    }

    @Test
    void save_and_find_by_id() {
        // given / arrange
        RenterDTO renter = factory.make();

        // when / act
        RenterDTO saved = repo.save(renter);

        Optional<RenterDTO> found = repo.findById(saved.getId());

        // then / evaluate - assert
        BDDAssertions.then(saved.getId()).isNotNull();
        BDDAssertions.then(found.isPresent()).isTrue();
        BDDAssertions.then(found.get().getUsername()).isEqualTo(saved.getUsername());

    }

    @Test
    void find_all(){
        // given / arrange
        RenterDTO dto_1 = factory.make();
        RenterDTO dto_2  = factory.make();

        // when / act
        repo.save(dto_1);
        repo.save(dto_2);
        List<RenterDTO> renters = repo.findAll();

        // then / evaluate - assert
        BDDAssertions.then(renters.size()).isEqualTo(2);

    }

    @Test
    void delete_by_id(){
        // given / arrange
        RenterDTO dto = factory.make();

        // when / act
        RenterDTO saved = repo.save(dto);
        repo.deleteById(saved.getId());

        // then / evaluate - assert
        BDDAssertions.then(repo.findById(saved.getId()).isPresent()).isFalse();
    }

}
