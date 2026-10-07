
# Java Market Data Terminal

A Java/JavaFX market-data terminal simulator built to explore event-driven architecture, concurrency, state management, and fault handling.

The project simulates multiple market-data feeds, preserves per-symbol event ordering through a striped executor model, derives live market state, maintains bounded rolling price history, and visualises the result in a JavaFX desktop UI.


## Features

- Simulated live feeds for multiple symbols
- Event-driven processing using immutable `MarketEvent` records
- Striped single-thread executors for bounded concurrency and per-symbol ordering
- Derived `MarketState` including current/previous price, tick movement, session open/high/low, session movement, timestamp, and live/stale status
- Bounded rolling price history and trend calculation
- JavaFX table and selected-symbol price chart
- Controlled feed-failure injection and recovery
- Read-time stale-data detection
- Unit tests for processor behaviour
- Explicit executor shutdown and application lifecycle management

## Architecture

```text
FakeMarketDataProvider
        |
        | MarketEvent
        v
StripedEventDispatcher
        |
        | Runnable / process(event)
        v
MarketProcessor
        |
        | read-only snapshot
        v
MarketTerminalApp (JavaFX)
```

Each symbol is hashed to a fixed single-threaded executor stripe. This preserves event ordering for the same symbol while allowing bounded concurrency across independent symbols.

The `MarketProcessor` separates raw events from derived state, maintains rolling history, calculates trends, and determines whether data is stale based on the age of the latest event.

The JavaFX layer reads snapshots from the processor and updates the UI on the JavaFX Application Thread.

### Architecture diagram

<img width="932" height="717" alt="architecture drawio" src="https://github.com/user-attachments/assets/063f7db3-0d1e-4257-b765-4b59fefd8d4e" />


## Fault Handling

The UI includes controlled fault injection for the selected symbol.

When a feed is paused:

1. The provider stops producing new events for that symbol.
2. Other feeds continue processing normally.
3. The last valid state and historical chart remain available.
4. Once the latest event exceeds the stale threshold, the processor reports the symbol as `STALE`.
5. Restoring the feed causes the next valid event to update the timestamp and return the symbol to `LIVE`.

The UI does not manually set a symbol to stale; stale state is derived from event timestamps.

## Testing

The processor is tested independently from the UI, scheduler, and dispatcher.

Current tests cover:

- initial market-state creation
- derived metrics after subsequent events
- stale-state detection
- rolling price history and trend calculation

Run the test suite with:

```bash
mvn test
```

## Running the Application

### Requirements

- Java 24
- Maven

### Start the application

```bash
mvn javafx:run
```

## Technologies

- Java 24
- JavaFX
- Maven
- JUnit 5
- Java concurrency utilities including `ScheduledExecutorService`, `ExecutorService`, and `ConcurrentHashMap`

