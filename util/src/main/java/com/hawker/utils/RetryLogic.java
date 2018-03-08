package com.hawker.utils;

import com.hawker.utils.exception.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

public final class RetryLogic {
    private static final Logger log = LoggerFactory.getLogger(RetryLogic.class);

    public static <In, Out> Out retry(In args, Function<In, Out> func, ExceptionHandler<In> handler, int maxTried)
            throws InterruptedException {
        return retry1(args, func, handler, 1, maxTried, 1000);
    }

    private static <In, Out> Out retry1(In args,
                                        Function<In, Out> func,
                                        ExceptionHandler<In> handler,
                                        int tried,
                                        int maxTried,
                                        long interval) throws InterruptedException {
        try {
            return func.apply(args);
        } catch (Exception e) {
            if (tried >= maxTried) {
                log.error("Exception occurred over maxRetry[{}] times, stop retry !!!", maxTried);
                handler.handle(args, e);
                return null;
            } else {
                tried += 1;
                long internalNext = interval + 1000 * tried;
                log.debug("Exception occurred: {}\n" +
                        "Try the: {} time, interval sleep(retry after): {}s", e.getMessage(), tried, internalNext / 1000);
                Thread.sleep(internalNext);
                return retry1(args, func, handler, tried, maxTried, internalNext);
            }
        }
    }

}
