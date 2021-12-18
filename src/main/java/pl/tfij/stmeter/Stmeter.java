package pl.tfij.stmeter;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.ToDoubleFunction;

public class Stmeter {
    private static final Logger logger = LoggerFactory.getLogger(Stmeter.class);
    private static final MeterRegistry DEFAULT_METRIC_REGISTRY = new SimpleMeterRegistry();

    private static volatile boolean isInitialized = false;
    private static volatile MeterRegistry meterRegistry = DEFAULT_METRIC_REGISTRY;
    private static final Set<LateInitGauge> gaugeLateInit = ConcurrentHashMap.newKeySet();

    public static void init(MeterRegistry meterRegistry) {
        Stmeter.meterRegistry = meterRegistry;
        isInitialized = true;
        gaugeLateInit.forEach(initGauge -> {
            initGauge.registerGauge.accept(meterRegistry);
            logger.debug("Late init of `{}` metric.", initGauge.metricName);
        });
        gaugeLateInit.clear();
        logger.info("Stmeter is initialized.");
    }

    public static Counter counter(String metricName, Tag... tags) {
        return meterRegistry.counter(metricName, Arrays.asList(tags));
    }

    public static Timer timer(String metricName, Tag... tags) {
        return meterRegistry.timer(metricName, Arrays.asList(tags));
    }

    public static AsyncTimer asyncTimer(String metricName, Tag... tags) {
        return new AsyncTimer(timer(metricName, tags));
    }

    public static DistributionSummary distributionSummary(String metricName, Tag... tags) {
        return DistributionSummary.builder(metricName).tags(Arrays.asList(tags)).register(meterRegistry);
    }

    public static <T extends Collection<?>> T gaugeCollectionSize(String metricName, T collection, Tag... tags) {
        return gauge(metricName, collection, Collection::size, tags);
    }

    public static <T extends Map<?, ?>> T gaugeMapSize(String metricName, T map, Tag... tags) {
        return gauge(metricName, map, Map::size, tags);
    }

    public static <T> T gauge(String metricName, T stateObject, ToDoubleFunction<T> valueFunction, Tag... tags) {
        Gauge.Builder<T> gaugeBuilder = Gauge.builder(metricName, stateObject, valueFunction).tags(Arrays.asList(tags));
        register(gaugeBuilder, metricName);
        return stateObject;
    }

    public static Counter register(Counter.Builder builder) {
        return builder.register(meterRegistry);
    }

    public static Timer register(Timer.Builder builder) {
        return builder.register(meterRegistry);
    }

    public static <T> void register(Gauge.Builder<T> builder) {
        register(builder, "");
    }

    private static <T> void register(Gauge.Builder<T> builder, String metricName) {
        Consumer<MeterRegistry> gaugeInitFunction = mr -> builder.register(mr);
        if (!isInitialized) {
            logger.debug("Call registry a gauge `{}` on not initialized state.", metricName);
            LateInitGauge initGauge = new LateInitGauge("", gaugeInitFunction);
            gaugeLateInit.add(initGauge);
            if (isInitialized) { // race condition fallback
                gaugeLateInit.remove(initGauge);
                gaugeInitFunction.accept(meterRegistry);
            } else {
                gaugeInitFunction.accept(meterRegistry);
            }
        } else {
            gaugeInitFunction.accept(meterRegistry);
        }
    }

    public static DistributionSummary register(DistributionSummary.Builder builder) {
        return builder.register(meterRegistry);
    }

    public static void reset() {
        isInitialized = false;
        meterRegistry = DEFAULT_METRIC_REGISTRY;
        gaugeLateInit.clear();
    }

    private static class LateInitGauge {
        private final String metricName;
        private final Consumer<MeterRegistry> registerGauge;

        public LateInitGauge(String metricName, Consumer<MeterRegistry> registerGauge) {
            this.metricName = metricName;
            this.registerGauge = registerGauge;
        }
    }

}
