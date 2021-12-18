package pl.tfij.stmeter;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnBean(MeterRegistry.class)
public class StmeterConfig {

    public StmeterConfig(MeterRegistry meterRegistry) {
        Stmeter.init(meterRegistry);
    }

}
