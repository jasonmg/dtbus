package com.hawker.utils.exception;

import org.slf4j.Logger;

import java.util.function.Function;

public class ExceptionHandlerLogging<T> implements ExceptionHandler<T> {

    // convert map key from type T to string
    private Function<T, String> keyToStrF;
    private Logger logger;

    public ExceptionHandlerLogging(Logger log) {
        this.logger = log;
        this.keyToStrF = T::toString;
    }

    public ExceptionHandlerLogging(Logger log, Function<T, String> keyToStrF) {
        this.logger = log;
        this.keyToStrF = keyToStrF;
    }
    
    @Override
    public void handle(T key, Exception e) {
        logger.error(keyToStrF.apply(key) + " : " + e.getMessage());
    }
}
