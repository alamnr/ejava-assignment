package info.ejava.assignments.api.autorentals.svc.main.auto.repo;

import java.util.Optional;

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import info.ejava.assignments.api.autorenters.dto.autos.AutoDTO;
import info.ejava.assignments.api.autorenters.dto.autos.AutoDTOFactory;
import info.ejava.assignments.api.autorenters.svc.autos.AutosDTORepository;
import info.ejava.assignments.api.autorenters.svc.autos.AutosDTORepositoryMapImpl;
import lombok.extern.slf4j.Slf4j;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Slf4j
public class AutoRepositoryMapImplTest {

    private AutosDTORepository autosDTORepository;
    private AutoDTOFactory factory;

    @BeforeEach
    void init(){
        autosDTORepository = new AutosDTORepositoryMapImpl();
        factory = new AutoDTOFactory();
    }
    
    @Test
    void save_and_find_by_id() {
        // given / arrange
        AutoDTO renter = factory.make();

        // when / act
        AutoDTO saved = autosDTORepository.save(renter);

        Optional<AutoDTO> found = autosDTORepository.findById(saved.getId());
        
        // then / evaluate - assert
        BDDAssertions.then(saved.getId()).isNotNull();
        BDDAssertions.then(found.isPresent()).isTrue();
        BDDAssertions.then(found.get().getUsername()).isEqualTo(saved.getUsername());

    }

    @Test
    void find_all(){
        // given / arrange
        AutoDTO dto_1 = factory.make();
        AutoDTO dto_2  = factory.make();

        // when / act
        autosDTORepository.save(dto_1);
        autosDTORepository.save(dto_2);
        Page<AutoDTO> autos = autosDTORepository.findAll(Pageable.unpaged());

        // then / evaluate - assert
        BDDAssertions.then(autos.getContent().size()).isEqualTo(2);

    }

    @Test
    void delete_by_id(){
        // given / arrange
        AutoDTO dto = factory.make();

        // when / act
        AutoDTO saved = autosDTORepository.save(dto);
        autosDTORepository.deleteById(saved.getId());

        // then / evaluate - assert
        BDDAssertions.then(autosDTORepository.findById(saved.getId()).isPresent()).isFalse();
    }

    
}
