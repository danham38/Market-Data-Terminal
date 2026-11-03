package ui;

import model.MarketState;
import model.TrendDirection;

public record MarketViewRow(
        MarketState state,
        TrendDirection trend
) {
}
