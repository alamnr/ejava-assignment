package info.ejava.assignments.api.autorenters.svc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static org.assertj.core.api.BDDAssertions.then;

class POJORepositoryMapImplTest {
    @Data
    @AllArgsConstructor
    @Builder
    static class Pojo {
        String id;
        String name;
        LocalDate date;
    }
    interface PojoRepository extends POJORepository<Pojo> {
        Page<Pojo> findBetweenDates(LocalDate lowInclusive, LocalDate highExclusive, Pageable pageable);
    }
    static class PojoRepoImpl extends POJORepositoryMapImpl<Pojo> implements PojoRepository {
        public PojoRepoImpl() {
            super(Pojo::getId, Pojo::setId, "pojo-");
        }

        @Override
        public Page<Pojo> findBetweenDates(LocalDate lowInclusive, LocalDate highExclusive, Pageable pageable) {
            List<Predicate<Pojo>> predicates = new ArrayList<>();
            predicates.add(p->!p.getDate().isBefore(lowInclusive));
            predicates.add(p->p.getDate().isBefore(highExclusive));
            Predicate<Pojo> criteria = predicates.stream().reduce(Predicate::and).orElse(p->true);
            return super.findAll(criteria, pageable);
        }
    }

    PojoRepository repo = new PojoRepoImpl();
    Pojo one = Pojo.builder().name("one").date(LocalDate.now()).build();

    List<Pojo> pojos(int cnt) {
        return IntStream.range(0,cnt).mapToObj(i-> Pojo.builder()
                .name(i+"x")
                .date(LocalDate.now().plusDays(i))
                .build()).toList();
    }

    @Test
    void can_save() {
        //when
        repo.save(one);
        //then
        then(one.getId()).isNotEmpty();
        then(repo.count()).isEqualTo(1);
    }

    @Test
    void can_save_all() {
        //given
        List<Pojo> pojos = pojos(5);
        //when
        repo.saveAll(pojos);
        //then
        pojos.forEach(p-> then(p.getId()).isNotEmpty() );
        then(repo.count()).isEqualTo(pojos.size());
    }

    @Test
    void can_find_by_id() {
        //given
        List<Pojo> pojos = pojos(5);
        repo.saveAll(pojos);
        //when
        Optional<Pojo> found = repo.findById(pojos.get(3).getId());
        //then
        then(found).isPresent();
        then(found.get().getName()).isEqualTo(pojos.get(3).getName());
    }

    @Test
    void can_test_exist_by_id() {
        //given
        List<Pojo> pojos = pojos(5);
        repo.saveAll(pojos);
        //when
        boolean found = repo.existsById(pojos.get(3).getId());
        //then
        then(found).isTrue();
        then(repo.existsById("unknownId")).isFalse();
    }

    @Test
    void can_find_all() {
        //given
        List<Pojo> pojos = pojos(5);
        repo.saveAll(pojos);
        //when
        List<Pojo> allPojos = repo.findAll();
        //then
        then(allPojos).containsAll(pojos);
    }

    @Test
    void can_find_all_paged() {
        //given
        List<Pojo> pojos = pojos(5);
        repo.saveAll(pojos);

        //when
        Page<Pojo> pojoPage = repo.findAll(Pageable.unpaged());
        //then
        then(pojoPage).containsAll(pojos);
        then(pojoPage.getTotalElements()).isEqualTo(pojos.size());
        then(pojoPage.getNumberOfElements()).isEqualTo(pojos.size());

        //when
        pojoPage = repo.findAll(PageRequest.of(0, 3));
        //then
        then(pojoPage.getTotalElements()).isEqualTo(pojos.size());
        then(pojoPage.getNumberOfElements()).isEqualTo(3);

        //when
        pojoPage = repo.findAll(PageRequest.of(1, 3));
        //then
        then(pojoPage.getTotalElements()).isEqualTo(pojos.size());
        then(pojoPage.getNumberOfElements()).isEqualTo(2);

        //when
        pojoPage = repo.findAll(PageRequest.of(2, 3));
        //then
        then(pojoPage.getTotalElements()).isEqualTo(pojos.size());
        then(pojoPage.getNumberOfElements()).isEqualTo(0);
    }

    @Test
    void can_count() {
        //given
        int size = repo.saveAll(pojos(3)).size();
        //when
        long cnt = repo.count();
        //then
        then(cnt).isEqualTo(size);
    }

    @Test
    void can_delete_by_id() {
        //given
        List<Pojo> pojos = pojos(5);
        repo.saveAll(pojos);
        String pojoToDeleteId = pojos.get(3).getId();
        //when
        repo.deleteById(pojoToDeleteId);
        //then
        then(repo.count()).isEqualTo(pojos.size()-1);
        then(repo.findById(pojoToDeleteId)).isNotPresent();
    }

    @Test
    void can_delete_by_all() {
        //given
        List<Pojo> pojos = pojos(5);
        repo.saveAll(pojos);
        //when
        repo.deleteAll();
        //then
        then(repo.count()).isEqualTo(0);
        then(repo.findById(pojos.get(0).getId())).isNotPresent();
    }

    @Test
    void can_delete_by_object() {
        //given
        List<Pojo> pojos = pojos(5);
        repo.saveAll(pojos);
        Pojo pojoToDelete = pojos.get(3);
        //when
        repo.delete(pojoToDelete);
        //then
        then(repo.count()).isEqualTo(pojos.size()-1);
        then(repo.findById(pojoToDelete.getId())).isNotPresent();
    }

    @Test
    void can_delete_all_by_id() {
        //given
        List<Pojo> pojos = pojos(5);
        repo.saveAll(pojos);
        List<String> ids = IntStream.range(1, 3).mapToObj(i -> pojos.get(i).getId()).toList();
        //when
        repo.deleteAllById(ids);
        //then
        then(repo.count()).isEqualTo(pojos.size()-ids.size());
        for (String id: ids) {
            then(repo.findById(id)).isNotPresent();
        }
    }

    @Test
    void can_delete_all_by_object() {
        //given
        List<Pojo> pojos = pojos(5);
        repo.saveAll(pojos);
        List<Pojo> toDelete = IntStream.range(1, 3).mapToObj(i -> pojos.get(i)).toList();
        //when
        repo.deleteAll(toDelete);
        //then
        then(repo.count()).isEqualTo(pojos.size()-toDelete.size());
        for (Pojo deleted: toDelete) {
            then(repo.findById(deleted.getId())).isNotPresent();
        }
    }

    @Test
    void can_find_by_date() {
        //given
        List<Pojo> pojos = pojos(5);
        repo.saveAll(pojos);
        Pojo toFind = pojos.get(3);
        //when
        Page<Pojo> found = repo.findBetweenDates(toFind.getDate(), toFind.getDate().plusDays(1), Pageable.unpaged());
        //then
        then(found).hasSize(1);
        then(found.getContent().get(0)).isEqualTo(toFind);
    }

    @Test
    void can_find_by_date_paged() {
        //given
        List<Pojo> pojos = pojos(5);
        repo.saveAll(pojos);
        List<Pojo> ordered = pojos.stream().sorted((left, right)-> Comparator.comparing(Pojo::getDate).compare(left, right)).toList();
        LocalDate low = ordered.get(0).getDate();
        LocalDate high = ordered.get(3).getDate();

        //when
        Page<Pojo> page0 = repo.findBetweenDates(low, high, PageRequest.of(0,2));
        //then
        then(page0).hasSize(2);
        then(page0.getNumberOfElements()).isEqualTo(2);
        then(page0.getTotalElements()).isEqualTo(3);

        //when
        Page<Pojo> page1 = repo.findBetweenDates(low, high, PageRequest.of(1,2));
        //then
        then(page1).hasSize(1);
        then(page1.getNumberOfElements()).isEqualTo(1);
        then(page1.getTotalElements()).isEqualTo(3);

        //when
        Page<Pojo> page2 = repo.findBetweenDates(low, high, PageRequest.of(2,2));
        //then
        then(page2).hasSize(0);
        then(page2.getNumberOfElements()).isEqualTo(0);
        then(page2.getTotalElements()).isEqualTo(3);
    }
}
