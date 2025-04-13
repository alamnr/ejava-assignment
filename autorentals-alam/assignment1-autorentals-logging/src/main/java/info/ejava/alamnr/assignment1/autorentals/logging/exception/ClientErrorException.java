package info.ejava.alamnr.assignment1.autorentals.logging.exception;

import java.time.Instant;

public abstract class ClientErrorException extends RuntimeException {

    protected Instant date = Instant.now();
    protected String error;

    public ClientErrorException withDate(Instant date){
        this.date = date;
        return this;
    }

    public ClientErrorException withError(String error) {
        this.error = error;
        return this;
    }   

    protected ClientErrorException(Throwable cause) {
        super(cause);
    }

    protected ClientErrorException(String message, Object... args) {
        super(String.format(message, args));
    }

    protected ClientErrorException(Throwable cause, String message, Object... args){
        super(String.format(message, args), cause);
    }

    public static class NotFoundException extends ClientErrorException {
        public NotFoundException(String message, Object... args){
            super(message, args);
        }
        public NotFoundException(Throwable cause, String message, Object... args){
            super(cause, message, args);
        }
    }

    public static class InvalidInputException extends ClientErrorException {
        public InvalidInputException(String message, Object...args) {  
            super(message, args); 
        }
        public InvalidInputException(Throwable cause, String message, Object...args) { 
            super(cause, message, args); 
        }
    }

    
}
