package info.ejava.alamnr.assignment1.autorentals.logging.exception;


import java.time.Instant;
import java.util.Date;

import lombok.Getter;


public abstract class ServerErrorException extends RuntimeException {
    
    protected Instant date = Instant.now();
    protected String error;
    
    public ServerErrorException withDate(Instant date) {
        this.date = date;
        return this;
    }

    public ServerErrorException withError(String error) {
        this.error = error;
        return this;
    }

    public String getError(){
        return this.error;
    }

    public Instant getDate(){
        return this.date;
    }

    protected ServerErrorException(Throwable cause){
        super(cause);
    }

    protected ServerErrorException(String message, Object... args){
        super(String.format(message, args));
    }

    protected ServerErrorException(Throwable cause, String message, Object... args){
        super(String.format(message, args), cause);
    }

    public static class InternalErrorException extends ServerErrorException {
        public InternalErrorException(String message, Object... args) {
            super(message, args);
        }
        public InternalErrorException(Throwable cause, String message, Object... args){
            super(cause,message,args);
        }
    }
}
