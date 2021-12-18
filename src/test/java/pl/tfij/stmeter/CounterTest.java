package pl.tfij.stmeter;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

public class CounterTest {

    @AfterEach
    void cleanUp() {
        Stmeter.reset();
    }

    @Test
    void shouldMeasureCounter() {
        // given initialized Stmeter
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        Stmeter.init(meterRegistry);

        // when record a value
        Stmeter.counter("sample.metric").increment(17);

        // then value is recorded
        Counter counter = meterRegistry.find("sample.metric").counter();
        await().timeout(1, SECONDS).until(() -> counter.count() == 17);
    }

    @Test
    void shouldRegisterMetricFromCounterBuilder() {
        // given initialized Stmeter
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        Stmeter.init(meterRegistry);

        // and gauge builder
        Counter.Builder counterBuilder = Counter.builder("sample.metric")
                .tags("aTag", "aValue")
                .description("my sample metric");

        // when I register the builder
        Counter registeredCounter = Stmeter.register(counterBuilder);

        // and record a value
        registeredCounter.increment(19);

        // then value is recorded
        Counter counter = meterRegistry.find("sample.metric").counter();
        await().timeout(1, SECONDS).until(() -> counter.count() == 19);
    }
}
