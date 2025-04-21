package info.ejava.alamnr.assignment1.autorentals.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import info.ejava.alamnr.assignment1.autorentals.logging.repo.AutoRentalsRepository;
import info.ejava.alamnr.assignment1.autorentals.logging.repo.AutoRentalsRepositoryImpl;

//@ExtendWith(MockitoExtension.class)
public class ExceptionTest {

    private static AutoRentalsRepository repository;
    @BeforeAll
    static void init(){
        repository = new AutoRentalsRepositoryImpl();
    }   

    @Test
    void assert_exception_inside_method(){
        String renterId = "r15";
        Throwable ex = assertThrows(RuntimeException.class , ()-> repository.getByRenterId("r15"));
        assertEquals(ex.getMessage(), String.format("target with renterId[%s] not found", renterId));

        Throwable ex1 = Assertions.catchThrowable(()-> repository.getByRenterId(renterId));
        Assertions.assertThat(ex1).hasMessage(String.format("target with renterId[%s] not found",renterId));

        RuntimeException rException = Assertions.catchThrowableOfType(()->repository.getByRenterId(renterId), RuntimeException.class);
        Assertions.assertThat(rException).hasMessage(String.format("target with renterId[%s] not found", renterId));

        Assertions.assertThatThrownBy(()-> repository.getByRenterId(renterId))
                    .hasMessage(String.format("target with renterId[%s] not found", renterId));
        Assertions.assertThatExceptionOfType(RuntimeException.class).isThrownBy(()-> repository.getByRenterId(renterId))
                    .withMessage(String.format("target with renterId[%s] not found", renterId));

    }




}
