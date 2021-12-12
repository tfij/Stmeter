package pl.tfij.stmeter;

import io.micrometer.core.instrument.Timer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class AsyncTimer {
    private final Timer timer;

    public AsyncTimer(Timer timer) {
        this.timer = timer;
    }

    public <T> CompletableFuture<T> record(Supplier<CompletableFuture<T>> supplier) {
        long startTime = System.currentTimeMillis();
        CompletableFuture<T> result = supplier.get();
        result.whenComplete((r, t) -> {
            long endTime = System.currentTimeMillis();
            timer.record(endTime - startTime, TimeUnit.MILLISECONDS);
        });
        return result;
    }
}
