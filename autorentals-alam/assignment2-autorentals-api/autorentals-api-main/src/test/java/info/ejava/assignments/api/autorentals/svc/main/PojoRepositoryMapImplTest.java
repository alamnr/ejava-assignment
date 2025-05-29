package info.ejava.assignments.api.autorentals.svc.main;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import info.ejava.assignments.api.autorenters.svc.POJORepository;
import info.ejava.assignments.api.autorenters.svc.POJORepositoryMapImpl;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PojoRepositoryMapImplTest {
    

    @Data
    @AllArgsConstructor
    @Builder
    static class Pojo {
        String id;
        String name;
        LocalDate date;
    }

    interface PojoRepository extends POJORepository<Pojo> {
        Page<Pojo> findBetweenDate (LocalDate lowInclusive,LocalDate highInclusive,Pageable pageable);
    }

    static class PojoRepoImpl extends POJORepositoryMapImpl<Pojo> implements PojoRepository {

        public PojoRepoImpl() {
            super(pojo->pojo.getId(),(pojo,id)->pojo.setId(id),"pojo-");
        }

        @Override
        public Page<Pojo> findBetweenDate(LocalDate lowInclusive,LocalDate highInclusive, Pageable pageable){
            List<Predicate<Pojo>> predicates = new ArrayList<>();
            predicates.add(p->!p.getDate().isBefore(lowInclusive));
            predicates.add(p->p.getDate().isBefore(highInclusive));
            Predicate<Pojo> criteria = predicates.stream().reduce(Predicate::and).orElse(p->true);
            return super.findAll(criteria, pageable);
        }
    }

    PojoRepository repo = new PojoRepoImpl();
    Pojo one = Pojo.builder().name("one").date(LocalDate.now()).build();

    List<Pojo> pojos(int count) {
        return IntStream.range(0, count).mapToObj(i->Pojo.builder()
                .name(i+"x").date(LocalDate.now().plusDays(i)).build()).toList();   
    }

    @Test
    void can_save(){
        // when / act
        repo.save(one);
        // then
        BDDAssertions.then(one.getId()).isNotEmpty();
        BDDAssertions.then(repo.count()).isEqualTo(1);
    }

    @Test
    void can_save_all(){
        // given / arrange
        List<Pojo> pojos = pojos(5);
        // when / act 
        repo.saveAll(pojos);

        // then / evaluate / assert
        pojos.forEach(p-> BDDAssertions.then(p.getId()).isNotEmpty());
        BDDAssertions.then(repo.count()).isEqualTo(pojos.size());
    }

    @Test
    void can_find_by_id(){
        // given / arrange
        List<Pojo> pojos = pojos(5);
        repo.saveAll(pojos);

        // when / act
        Optional<Pojo> found  = repo.findById(pojos.get(3).getId());

        // then / evaluate-assert
        BDDAssertions.then(found).isPresent();
        BDDAssertions.then(found.get().getName()).isEqualTo(pojos.get(3).getName());

    }

    @Test
    void can_test_exist_by_id() {
        // given / arrange
        List<Pojo> pojos = pojos(5);
        repo.saveAll(pojos);

        // when / act
        boolean found = repo.existsById(pojos.get(3).getId());

        // then / evaluate-assert
        BDDAssertions.then(found).isTrue();
        BDDAssertions.then(repo.existsById("unknownId")).isFalse();
    }

    @Test 
    void can_find_all(){
        // given / arrange
        List<Pojo> pojos = pojos(5);
        repo.saveAll(pojos);

        // when / act
        List<Pojo> allPojos = repo.findAll();
        // then / assert / evaluate the result
        BDDAssertions.then(allPojos).containsAll(pojos);

    }

    @Test
    void can_find_all_paged(){
        // given / arrange
        List<Pojo> pojos = pojos(5);
        repo.saveAll(pojos);

        // when / act
        Page<Pojo> pojoPage = repo.findAll(Pageable.unpaged());
        // then 
        BDDAssertions.then(pojoPage).containsAll(pojos);
        BDDAssertions.then(pojoPage.getTotalElements()).isEqualTo(pojos.size());
        BDDAssertions.then(pojoPage.getNumberOfElements()).isEqualTo(pojos.size());
        BDDAssertions.then(pojoPage.getTotalPages()).isEqualTo(1);

        // when
        pojoPage = repo.findAll(PageRequest.of(0, 3));
        // then
        BDDAssertions.then(pojoPage.getTotalElements()).isEqualTo(pojos.size());
        BDDAssertions.then(pojoPage.getNumberOfElements()).isEqualTo(3);

        // when
        pojoPage = repo.findAll(PageRequest.of(1, 3));
        // then
        BDDAssertions.then(pojoPage.getTotalElements()).isEqualTo(pojos.size());
        BDDAssertions.then(pojoPage.getNumberOfElements()).isEqualTo(2);

        // when
        pojoPage = repo.findAll(PageRequest.of(2, 3));
        // then
        BDDAssertions.then(pojoPage.getTotalElements()).isEqualTo(pojos.size());
        BDDAssertions.then(pojoPage.getNumberOfElements()).isEqualTo(0);
    }

    @Test
    void  can_count(){
        // given
        int size = repo.saveAll(pojos(3)).size();
        // when 
        long count = repo.count();
        // then
        BDDAssertions.then(count).isEqualTo(size);

    }

    @Test
    void can_delete_by_id() {
        // given
        List<Pojo> pojos = pojos(5);
        repo.saveAll(pojos);
        String id = pojos.get(3).getId();

        // when
        repo.deleteById(id);

        // then
        BDDAssertions.then(repo.count()).isEqualTo(pojos.size()-1);
        BDDAssertions.then(repo.findById(id)).isNotPresent();

    }

    @Test
    void can_delete_by_all(){
        // given
        List<Pojo> pojos = pojos(5);
        repo.saveAll(pojos);

        // when 
        repo.deleteAll();
        // then
        BDDAssertions.then(repo.count()).isEqualTo(0);
        BDDAssertions.then(repo.findById(pojos.get(0).getId())).isNotPresent();
    }
    
    @Test
    void can_delete_by_object() {
        // given
        List<Pojo> pojos = pojos(5);
        repo.saveAll(pojos);
        Pojo pojoToDelete = pojos.get(3);
        // when
        repo.delete(pojoToDelete);
        //then 
        BDDAssertions.then(repo.count()).isEqualTo(pojos.size()-1);
        BDDAssertions.then(repo.findById(pojoToDelete.getId())).isNotPresent();
    }

    @Test
    void can_delete_all_by_id(){
        // given
        List<Pojo> pojos = pojos(5);
        repo.saveAll(pojos);
        List<String> ids = IntStream.range(1, 3).mapToObj(i->pojos.get(i).getId()).toList();
        // when
        repo.deleteAllById(ids);

        // then 
        BDDAssertions.then(repo.count()).isEqualTo(pojos.size()-ids.size());
        ids.forEach(id-> BDDAssertions.then(repo.findById(id)).isNotPresent());

    }

    @Test
    void can_delete_all_by_object() {
        // given
        List<Pojo> pojos = pojos(5);
        repo.saveAll(pojos);
        List<Pojo> toDelete = IntStream.range(1, 5).mapToObj(i-> pojos.get(i)).toList();
        // when
        repo.deleteAll(toDelete);

        // then
        BDDAssertions.then(repo.count()).isEqualTo(pojos.size() - toDelete.size());
        for (Pojo pojo : toDelete) {
            BDDAssertions.then(repo.findById(pojo.getId())).isNotPresent();
        }

    }

    @Test
    void can_find_by_date(){
        // given
        List<Pojo> pojos = pojos(5);
        repo.saveAll(pojos);
        Pojo tofind = pojos.get(3);
        // when
        Page<Pojo> found = repo.findBetweenDate(tofind.getDate(), tofind.getDate().plusDays(1), Pageable.unpaged());

        // then
        BDDAssertions.then(found).hasSize(1);
        BDDAssertions.then(found.getContent().get(0)).isEqualTo(tofind);
    }

    @Test
    void can_find_by_date_paged() {
        // given
        List<Pojo> pojos = pojos(5);
        repo.saveAll(pojos);
        List<Pojo> ordered = pojos.stream().sorted((l,r)->Comparator.comparing(Pojo::getDate).compare(l, r)).toList();
        List<Pojo> ordered_1 = pojos.stream().sorted((l,r)->l.getDate().compareTo(r.getDate())).toList();

        log.info("order - {}, order-1 - {}", ordered, ordered_1);

        LocalDate low = ordered_1.get(0).getDate();
        LocalDate high = ordered_1.get(3).getDate();

        // when
        Page<Pojo> page0 = repo.findBetweenDate(low, high, PageRequest.of(0,2));

        // then 
        BDDAssertions.then(page0).hasSize(2);
        BDDAssertions.then(page0.getNumberOfElements()).isEqualTo(2);
        BDDAssertions.then(page0.getTotalElements()).isEqualTo(3);

        // when 
        Page<Pojo> page1 = repo.findBetweenDate(low, high, PageRequest.of(1, 2));

        // then
        BDDAssertions.then(page1).hasSize(1);
        BDDAssertions.then(page1.getNumberOfElements()).isEqualTo(1);
        BDDAssertions.then(page1.getTotalElements()).isEqualTo(3);

        // when
        Page<Pojo> page2 = repo.findBetweenDate(low, high, PageRequest.of(2,2));
        // then
        BDDAssertions.then(page2).hasSize(0);
        BDDAssertions.then(page2.getNumberOfElements()).isEqualTo(0);
        BDDAssertions.then(page2.getTotalElements()).isEqualTo(3);

    }

}
