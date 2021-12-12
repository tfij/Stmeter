package pl.tfij.stmeter;

import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

public class AsyncTimerTest {

    private ExecutorService threadPool = Executors.newFixedThreadPool(1);

    @AfterEach
    void cleanUp() {
        Stmeter.reset();
    }

    @Test
    void shouldMeasureTimeOfAsyncExecution() {
        // given initialized Stmeter
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        Stmeter.init(meterRegistry);

        // when execute async function
        int executionTimeMillis = 1000;
        Stmeter.asyncTimer("sample.metric").record(() -> async(executionTimeMillis));

        // then timer is recorded
        Timer timer = meterRegistry.find("sample.metric").timer();
        await().timeout(2, SECONDS).until(() -> timer.count() == 1);

        // and timer record valid async all time (plus/minus margins)
        assertGraterThenOfEquals(executionTimeMillis, timer.mean(TimeUnit.MILLISECONDS));
        assertSmollerThenOfEquals(executionTimeMillis + 100, timer.mean(TimeUnit.MILLISECONDS));
    }

    private CompletableFuture<String> async(int executionTimeMillis) {
        CompletableFuture<String> future = new CompletableFuture<>();
        long startTime = System.currentTimeMillis();
        threadPool.submit(() -> {
            while (true) {
                long actualTime = System.currentTimeMillis();
                if (actualTime - startTime >= executionTimeMillis) {
                    future.complete("DONE");
                    return;
                }
            }
        });
        return future;
    }

    private static void assertGraterThenOfEquals(double expected, double actual) {
        if (actual < expected) {
            throw new AssertionFailedError("", expected, actual);
        }
    }

    private static void assertSmollerThenOfEquals(double expected, double actual) {
        if (actual > expected) {
            throw new AssertionFailedError("", expected, actual);
        }
    }

}
