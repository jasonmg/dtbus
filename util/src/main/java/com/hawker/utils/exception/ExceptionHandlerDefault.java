package com.hawker.utils.exception;

public class ExceptionHandlerDefault<T> implements ExceptionHandler<T> {
    public void handle(T key, Exception e) {
        // doing nothing by default
    }
}
