package pl.tfij.stmeter;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

public class DistributedSummaryTest {

    @AfterEach
    void cleanUp() {
        Stmeter.reset();
    }

    @Test
    void shouldMeasureDistributedSummary() {
        // given initialized Stmeter
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        Stmeter.init(meterRegistry);

        // when record distributed summary value
        Stmeter.distributionSummary("sample.metric").record(17);

        // then value is recorded
        DistributionSummary distributionSummary = meterRegistry.find("sample.metric").summary();
        await().timeout(1, SECONDS).until(() -> distributionSummary.count() == 1);
        Assertions.assertEquals(17, distributionSummary.mean());
    }

    @Test
    void shouldRegisterMetricFromSummaryBuilder() {
        // given initialized Stmeter
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        Stmeter.init(meterRegistry);

        // and gauge builder
        DistributionSummary.Builder builder = DistributionSummary.builder("sample.metric")
                .tags("aTag", "aValue")
                .description("my sample metric");

        // when I register the builder
        DistributionSummary registeredSummary = Stmeter.register(builder);

        // and record a value
        registeredSummary.record(19);

        // then value is recorded
        DistributionSummary summary = meterRegistry.find("sample.metric").summary();
        await().timeout(1, SECONDS).until(() -> summary.mean() == 19);
    }

}
