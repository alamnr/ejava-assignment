package info.ejava.assignments.testing.rentals.renters;

public class InvalidInputException extends RuntimeException {
    public InvalidInputException(String message, Object...args) {
        super(String.format(message, args));
    }
}
