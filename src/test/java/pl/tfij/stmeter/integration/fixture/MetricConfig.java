package pl.tfij.stmeter.integration.fixture;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricConfig {
    @Bean
    MeterRegistry meterRegistry() {
        return new SimpleMeterRegistry();
    }
}
