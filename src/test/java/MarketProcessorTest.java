import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class MarketProcessorTest {
    //for tests use fixed timestamp -> deterministic
    private long testTimestamp = 1_000L;
    private double tolerance = 0.0001;

    @Test
    void firstEventCreatesInitMarketState() {
        //ARRANGE
        MarketProcessor processor = new MarketProcessor();
        MarketEvent event = new MarketEvent(
                "TEST_SYMBOL",
                200.00,
                testTimestamp
        );
        //ACT
        processor.process(event);
        MarketState state = processor.getState("TEST_SYMBOL");
        //ASSERT
        //check existence
        assertNotNull(state);
        //check state price vs created event
        assertEquals(200.00, state.price());
        assertEquals(200.00, state.previousPrice());
        assertEquals(0.00, state.change());
        assertEquals(testTimestamp, state.timestamp());

    }

    @Test
    void secondEventUpdatedDerivedState() {
        //ARRANGE
        MarketProcessor processor = new MarketProcessor();
        MarketEvent event_1 = new MarketEvent(
                "TEST_SYMBOL_1",
                200.00,
                testTimestamp
        );
        MarketEvent event_2 = new MarketEvent(
                "TEST_SYMBOL_1",
                210.00,
                testTimestamp+1
        );
        //ACT
        processor.process(event_1);
        processor.process(event_2);
        MarketState state = processor.getState("TEST_SYMBOL_1");

        //ASSERT
        assertNotNull(state);
        //0.0001 the tolerance that we allow _. as using doubles need smaller tolerance than ints
        assertEquals(210.00, state.price(), tolerance);
        assertEquals(200.00, state.previousPrice(), tolerance);
        assertEquals(10.00, state.change(), tolerance);
        //0.01 tolerance for percentages
        assertEquals(5.0, state.changePercent(), 0.01);
        assertEquals(210.00, state.highPrice(), tolerance);
        assertEquals(200.00, state.lowPrice(), tolerance);
        assertEquals(10.00, state.sessionChange(), tolerance);
        //0.01 tolerance for percentages
        assertEquals(5.0, state.sessionChangePercent(), 0.01);
    }


    @Test
    void staleDetection() {
        //deliberately old timestamp
        long oldTimestamp = System.currentTimeMillis() - 10_000;
        //ARRANGE
        MarketProcessor processor = new MarketProcessor();
        MarketEvent event = new MarketEvent(
                "TEST_SYMBOL",
                200.00,
                oldTimestamp
        );

        //ACT
        processor.process(event);
        //get snapshot
        Map<String, MarketState> snapshot = processor.getSnapshot();
        MarketState state = snapshot.get("TEST_SYMBOL");

        //ASSERT
        assertNotNull(state);
        assertEquals(MarketStatus.STALE, state.status());
    }


    @Test
    void rollingHistoryAndTrend() {
        //ARRANGE
        MarketProcessor processor = new MarketProcessor();
        MarketEvent event_1 = new MarketEvent(
                "TEST_SYMBOL",
                100.00,
                testTimestamp
        );
        MarketEvent event_2 = new MarketEvent(
                "TEST_SYMBOL",
                101.00,
                testTimestamp+1
        );
        MarketEvent event_3 = new MarketEvent(
                "TEST_SYMBOL",
                102.00,
                testTimestamp+2
        );

        //ACT
        processor.process(event_1);
        processor.process(event_2);
        processor.process(event_3);
        MarketState state = processor.getState("TEST_SYMBOL");
        List<Double> history = processor.getPriceHistory("TEST_SYMBOL");

        //ASSERT
        assertNotNull(state);
        assertEquals(3, history.size());
        assertEquals(TrendDirection.UP, processor.getTrend("TEST_SYMBOL"));
        //verify ordering
        assertEquals(100.00, history.get(0), tolerance);
        assertEquals(101.00, history.get(1), tolerance);
        assertEquals(102.00, history.get(2), tolerance);
    }
}
