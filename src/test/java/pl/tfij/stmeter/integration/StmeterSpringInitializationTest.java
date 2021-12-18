package pl.tfij.stmeter.integration;

import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pl.tfij.stmeter.Stmeter;
import pl.tfij.stmeter.integration.fixture.SampleSpringComponent;
import pl.tfij.stmeter.integration.fixture.TestApp;

@SpringBootTest(classes = TestApp.class)
public class StmeterSpringInitializationTest {

    @Autowired
    private SampleSpringComponent sampleSpringComponent;

    @Autowired
    private MeterRegistry meterRegistry;

    @Test
    void shouldAutoConfigureStmeterAndDelegateStmeterToMeterRegistryBean() {
        // when increase a Stmeter counter
        Stmeter.counter("sample.metric").increment(5);

        // then meterRegistry bean counter is increased
        Assertions.assertEquals(5, meterRegistry.counter("sample.metric").count());
    }

    @Test
    void shouldAutoConfigureStmeterAndDelegateStmeterToMeterRegistryBeanInComponent() {
        // when call component method which collect a metric
        sampleSpringComponent.increaseCounter();

        // then meterRegistry bean counter is increased
        Assertions.assertEquals(1, meterRegistry.counter(SampleSpringComponent.COUNTER_METRIC_NAME).count());
    }

}
