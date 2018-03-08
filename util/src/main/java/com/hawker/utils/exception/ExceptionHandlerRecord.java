package com.hawker.utils.exception;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class ExceptionHandlerRecord<T> implements ExceptionHandler<T> {

    // convert map key from type T to string
    private Function<T, String> keyToStrF;
    private Map<String, String> errorMap = new ConcurrentHashMap<>();


    public ExceptionHandlerRecord() {
        this.keyToStrF = T::toString;
    }

    public ExceptionHandlerRecord(Function<T, String> keyToStrF) {
        this.keyToStrF = keyToStrF;
    }

    @Override
    public void handle(T key, Exception e) {
        errorMap.putIfAbsent(keyToStrF.apply(key), e.getMessage());
    }

    public Map<String, String> getErrorMap() {
        return errorMap;
    }

    public boolean hasError() {
        return !errorMap.isEmpty();
    }
}
