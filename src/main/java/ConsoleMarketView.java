

import java.util.Comparator;
import java.util.List;

public class ConsoleMarketView {
    //table width
    private static final int TABLE_WIDTH = 97;

    //makes the table for the terminal view
    public void render(List<MarketViewRow> rows) {

       System.out.println("-".repeat(TABLE_WIDTH));

        //lays out table nicely in columns
        System.out.printf(
                "%-8s %10s %10s %10s %10s %10s %12s %10s %8s%n",
                "SYMBOL",
                "PRICE",
                "TICK %",
                "OPEN",
                "HIGH",
                "LOW",
                "SESSION %",
                "STATUS",
                "TREND"
        );

        System.out.println("-".repeat(TABLE_WIDTH));

        //orders the symbols in a predictable output (ASC)

        rows.stream()
                .sorted(Comparator.comparing(row -> row.state().symbol()))
                .forEach(row -> {
                    MarketState state = row.state();

                    System.out.printf(
                            "%-8s %10.2f %+9.2f%% %10.2f %10.2f %10.2f %+11.2f%% %10s %8s%n",
                            state.symbol(),
                            state.price(),
                            state.changePercent(),
                            state.openPrice(),
                            state.highPrice(),
                            state.lowPrice(),
                            state.sessionChangePercent(),
                            state.status(),
                            row.trend()
                    );
                });
    }
}
