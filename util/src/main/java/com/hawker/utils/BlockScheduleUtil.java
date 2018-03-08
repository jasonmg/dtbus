package com.hawker.utils;

import fj.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class BlockScheduleUtil {

    private static final Logger logger = LoggerFactory.getLogger(BlockScheduleUtil.class);


    public static <T, R> BlockingQueue<R> callBySchedule(ScheduledExecutorService executor,
                                                         long delay,
                                                         TimeUnit timeUnit,
                                                         T input,
                                                         Function<T, R> f) {

        return callBySchedule(executor, delay, timeUnit, Arrays.asList(input), f);
    }

    /**
     * @param executor schedule executor
     * @param delay    fixed delay Minute
     * @param inputs   list of  parameter
     * @param f        function apply inputs
     * @param <T>      parameter type
     * @param <R>      return element type
     * @return
     */
    public static <T, R> BlockingQueue<R> callBySchedule(ScheduledExecutorService executor,
                                                         long delay,
                                                         TimeUnit timeUnit,
                                                         List<T> inputs,
                                                         Function<T, R> f) {

        BlockingQueue<R> completeQueue = new LinkedBlockingQueue<>();
        BlockingQueue<T> unCompleteQueue = new LinkedBlockingQueue<>();

        inputs.stream().forEach(unCompleteQueue::offer);
        schedule(executor, delay, timeUnit, completeQueue, unCompleteQueue, f);

        return completeQueue;
    }


    private static <T, R> void schedule(ScheduledExecutorService executor,
                                        long delay,
                                        TimeUnit timeUnit,
                                        BlockingQueue<R> completeQueue,
                                        BlockingQueue<T> unCompleteQueue,
                                        Function<T, R> f) {

        executor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                unCompleteQueue.stream().forEach(input -> {

                    Try.f(() -> f.apply(input)).f().validation(
                            exception -> {
                                logger.error("CallBySchedule encountered exception, reschedule after: {} {}.\n" +
                                        "Input: {}, Exception: {}", delay, timeUnit, input, exception.getMessage());
                                return null;
                            },
                            res -> {
                                if (completeQueue.offer(res)) {
                                    unCompleteQueue.remove(input);
                                    logger.info("CallBySchedule apply function successful, Input: {}", input);
                                }
                                return null;
                            }
                    );
                });
            }
        }, 0, delay, timeUnit);
    }
}
