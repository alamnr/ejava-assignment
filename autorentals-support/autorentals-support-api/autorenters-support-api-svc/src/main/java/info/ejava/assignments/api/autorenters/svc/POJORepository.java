package info.ejava.assignments.api.autorenters.svc;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;

public interface POJORepository<T> extends
        ListCrudRepository<T, String>,
        ListPagingAndSortingRepository<T, String> {
}