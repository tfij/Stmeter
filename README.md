# Stmeter lib

`Stmeter` is an abstraction to call micrometer metrics in spring framework with no need of injecting `meterRegistry` bean.

Basically, the standard metric call like

```
meterRegistry.counter("my.metric").increment();
```

can be replaced with

```
Stmeter.counter("my.metric").increment();
```

## Advantages of metric static access

When you write a large system, it is highly likely that the metrics in your code will be almost everywhere.
In spring framework, you have to inject `meterRegistry` bean everywhere.
This is especially troublesome when we want to have metrics in non-bean classes.

This library allows metric to be used through a static context which avoids the need to inject `metricRegistry` bean.
This approach is similar to logging.
You don't need to inject logger to log something.
You just create a static instance.

## Usage

Add maven dependency

```
TODO
```

Use `Stmeter` in your spring application:

```
import pl.tfij.stmeter.Stmeter;

...

Stmeter.counter("sample.metric").increment();
```

## Spring framework integration

Library provide spring autoconfiguration to setup the `Stmeter` class.
The `Stmeter` will be initialized with instance of `meterRegistry` spring bean.

## Meter async code execution

The Micrometer provide `Timer` class to meter code time execution.
When working with async code, usage of `Timer` require to add some boilerplate.

To measure async code time execution, `Stmeter` library provide `AsyncTimer` class.

```
void codeWithMetrics() {
    ...
    CompletableFuture<String> result = Stmeter.asyncTimer("metricName").record(() -> asyncFunction());
    ...
}

...

CompletableFuture<String> asyncFunction() {
    ...
}
```

## Limitations

### Metrics during spring context initialization

`Stmeter` is initialized while building the context and initializing the spring beans.
For this reason, you may find that `Stmeter` is initialized later than some beans.
This may result in the metrics collected during context initialization being lost.
For example, such metric

```
@Component
class MyComponent {
    MyComponent() {
        Stmeter.counter("sample.metric").increment();
    }
}
```

can be lost. 
In this case you should use injected `meterRegistry` bean.

However, the `Stmeter` support late initialization for `Gauge` metric.
You don't have to use `meterRegistry` bean for `Gauge` even if you use it before `Stmeter` was initialized (provided it is finally initiated).

```
@Component
class MyComponent {
    private List<String> listField = new ArrayList<>();

    MyComponent() {
        Stmeter.gaugeListSize("sample.metric", listField);
    }
}
```

### Many MeterRegistry beans

If for some reason you use two or more `MeterRegistry` beans, then by design, this library can handle only one of such bean.
