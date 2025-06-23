package info.ejava.assignments.api.autorentals.svc.main.rental.repo;

import java.util.List;
import java.util.Optional;

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import info.ejava.assignments.api.autorenters.dto.rentals.AutoRentalDTO;
import info.ejava.assignments.api.autorenters.dto.rentals.AutoRentalDTOFactory;
import info.ejava.assignments.api.autorenters.svc.POJORepositoryMapImpl;
import lombok.extern.slf4j.Slf4j;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Slf4j
public class AutoRentalRepositoryMapImplTest {

    private POJORepositoryMapImpl<AutoRentalDTO> repo;
    private AutoRentalDTOFactory autoRentalDTOFactory;

    @BeforeEach
    void init(){
        this.repo = new POJORepositoryMapImpl<>(autoRenter -> autoRenter.getId() ,
                     (autoRenter,id)->autoRenter.setId(id), "autoRenter-");
        this.autoRentalDTOFactory = new AutoRentalDTOFactory();
    }

    @Test
    void  save_and_find_by_id(){
     
        // given / arrange
        AutoRentalDTO autoRentalDTO = autoRentalDTOFactory.make();

        // when / act 
        AutoRentalDTO saved = repo.save(autoRentalDTO);

        Optional<AutoRentalDTO> found = repo.findById(saved.getId());
        // then / evaluate-assert

        BDDAssertions.then(saved.getId()).isNotNull();
        BDDAssertions.then(found.isPresent()).isTrue();
        BDDAssertions.then(found.get().getRenterName()).isEqualTo(saved.getRenterName());

    }

    
    @Test
    void find_all(){
        // given / arrange
        AutoRentalDTO dto_1 = autoRentalDTOFactory.make();
        AutoRentalDTO dto_2  = autoRentalDTOFactory.make();

        // when / act
        repo.save(dto_1);
        repo.save(dto_2);
        List<AutoRentalDTO> autoRentals = repo.findAll();

        // then / evaluate - assert
        BDDAssertions.then(autoRentals.size()).isEqualTo(2);

    }

    @Test
    void delete_by_id(){
        // given / arrange
        AutoRentalDTO dto = autoRentalDTOFactory.make();

        // when / act
        AutoRentalDTO saved = repo.save(dto);
        repo.deleteById(saved.getId());

        // then / evaluate - assert
        BDDAssertions.then(repo.findById(saved.getId()).isPresent()).isFalse();
    }

}

