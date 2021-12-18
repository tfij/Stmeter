package pl.tfij.stmeter.integration.fixture;

import org.springframework.stereotype.Component;
import pl.tfij.stmeter.Stmeter;

@Component
public class SampleSpringComponent {

    public static final String COUNTER_METRIC_NAME = "SampleSpringComponent.counter";

    public void increaseCounter() {
        Stmeter.counter(COUNTER_METRIC_NAME).increment();
    }

}
