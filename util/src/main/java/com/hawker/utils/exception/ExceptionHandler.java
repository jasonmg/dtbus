package com.hawker.utils.exception;

public interface ExceptionHandler<T> {
    void handle(T key, Exception e);
}

