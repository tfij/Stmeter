package pl.tfij.stmeter;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

public class GaugeLateInitTest {

    private final List<Integer> sampleList = new ArrayList<>();

    @AfterEach
    void cleanUp() {
        Stmeter.reset();
    }

    @Test
    void shouldMeterGaugeConfiguredBeforeInitialization() {
        // when I configure gauge before initialize Stmeter
        Stmeter.gaugeCollectionSize("gauge.collection.metric", sampleList, Tag.of("aTag", "tagValue"));

        // and then I initialize Stmeter
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        Stmeter.init(meterRegistry);

        // and then update gauged collection
        sampleList.addAll(List.of(2, 3, 5, 7));

        // then gauge metric is updated
        Gauge gauge = meterRegistry.find("gauge.collection.metric").gauge();
        await().timeout(1, SECONDS).until(() -> gauge != null && gauge.value() == sampleList.size());
    }

    @Test
    void shouldMeterGaugeRegisteredFromBuilderBeforeInitialization() {
        // when registered gauge builder
        Gauge.Builder<List<Integer>> gaugeBuilder = Gauge.builder("gauge.collection.metric", sampleList, List::size)
                .tags("aTag", "aValue")
                .description("my sample metric");
        Stmeter.register(gaugeBuilder);

        // and then I initialize Stmeter
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        Stmeter.init(meterRegistry);

        // and then update gauged collection
        sampleList.addAll(List.of(2, 3, 5, 7));

        // then gauge metric is updated
        Gauge gauge = meterRegistry.find("gauge.collection.metric").gauge();
        await().timeout(1, SECONDS).until(() -> gauge != null && gauge.value() == sampleList.size());
    }

}
