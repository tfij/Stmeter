package pl.tfij.stmeter;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

public class GaugeTest {

    private final Map<Integer, String> sampleMap = Map.of(1, "a", 2, "b");

    @AfterEach
    void cleanUp() {
        Stmeter.reset();
    }

    @Test
    void shouldMeasureGauge() {
        // given initialized Stmeter
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        Stmeter.init(meterRegistry);

        // when record distributed summary value
        Stmeter.gaugeMapSize("sample.metric", sampleMap);

        // then value is recorded
        Gauge gauge = meterRegistry.find("sample.metric").gauge();
        await().timeout(1, SECONDS).until(() -> gauge.value() == 2);
    }

    @Test
    void shouldRegisterMetricFromGaugeBuilder() {
        // given initialized Stmeter
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        Stmeter.init(meterRegistry);

        // and gauge builder
        Gauge.Builder<Map<Integer, String>> gaugeBuilder = Gauge.builder("sample.metric", sampleMap, Map::size)
                .tags("aTag", "aValue")
                .description("my sample metric");

        // when I register the builder
        Stmeter.register(gaugeBuilder);

        // then value is recorded
        Gauge gauge = meterRegistry.find("sample.metric").gauge();
        await().timeout(1, SECONDS).until(() -> gauge.value() == 2);
    }

}
