

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        //all symbols to subscribe to
        List<String> symbols = List.of(
                "AAPL",
                "MSFT",
                "NVDA"
        );

        ScheduledExecutorService displayScheduler = Executors.newSingleThreadScheduledExecutor();

        FakeMarketDataProvider provider = new FakeMarketDataProvider();
        StripedEventDispatcher dispatcher = new StripedEventDispatcher(4);
        MarketProcessor processor = new MarketProcessor();
        ConsoleMarketView view = new ConsoleMarketView();

        for (String symbol : symbols) {
            provider.subscribe(
                    symbol,
                    event -> dispatcher.dispatch(
                            event.symbol(),
                            () -> processor.process(event)
                    )
            );
        }


        displayScheduler.scheduleAtFixedRate(
                () -> {
                    Map<String, MarketState> snapshot = processor.getSnapshot();
                    List<MarketViewRow> rows = new ArrayList<>();

                    for (MarketState state : snapshot.values()) {
                        TrendDirection trend = processor.getTrend(state.symbol());
                        MarketViewRow row = new MarketViewRow(state, trend);
                        rows.add(row);
                    }
                    view.render(rows);
                },
                1,
                2,
                TimeUnit.SECONDS
        );

    }
}