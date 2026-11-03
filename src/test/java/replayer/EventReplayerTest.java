package replayer;

import model.MarketEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EventReplayerTest {
//    Files.writeString(filePath,
//            "AAPL,182.50,1729000000\n" +
//            "MSFT,190.75,1729000001\n" +
//            "GOOG,165.20,1729000002\n");
    //AAPL,182.50,1729000000
    MarketEvent event1 = new MarketEvent(
            "AAPL",
            182.50,
            1729000000
    );
    //MSFT,invalid,1729000001
    MarketEvent event2 = new MarketEvent(
            "MSFT",
            190.75,
            1729000000
    );
    //GOOG,165.20,1729000002
    MarketEvent event3 = new MarketEvent(
            "GOOG",
            165.20,
            1729000000
    );

    @Test
    public void acceptsValidCSVAndReturnsMarketEvents(@TempDir Path tempDir) throws IOException {
        Path filePath = tempDir.resolve("marketEventsTest.csv");
        Files.writeString(filePath,
                "AAPL,182.50,1729000000\n" +
                        "MSFT,190.75,1729000000\n" +
                        "GOOG,165.20,1729000000\n");
        //arrange,act,assert
        EventReplayer replayer = new EventReplayer(filePath);
        assertEquals(3, replayer.readEvents().size());
        assertEquals(event1, replayer.readEvents().get(0));
        assertEquals(event2, replayer.readEvents().get(1));
        assertEquals(event3, replayer.readEvents().get(2));

    }

    @Test
    public void shouldRejectInvalidRow(@TempDir Path tempDir) throws IOException {
        Path filePath = tempDir.resolve("marketEventsTest.csv");

        Files.writeString(filePath,
                "AAPL,182.50,1729000000\n" +
                        "MSFT,NaN,1729000000\n" +
                        "GOOG,165.20,1729000000\n");
        EventReplayer replayer = new EventReplayer(filePath);
        List<MarketEvent> events = replayer.readEvents();

        assertEquals(2, events.size());
        assertEquals("AAPL", events.get(0).symbol());
        assertEquals("GOOG", events.get(1).symbol());
    }
    @Test
    public void shouldRejectMissingFile(@TempDir Path tempDir) throws IOException {
        Path filePath = tempDir.resolve("missing.csv");
//        Files.writeString(filePath,
//                "AAPL,182.50,1729000000\n" +
//                        "MSFT,190.75,1729000000\n" +
//                        "GOOG,165.20,1729000000\n");
        assertThrows(IOException.class, () -> {
            EventReplayer replayer = new EventReplayer(filePath);
            replayer.readEvents();
        });
    }


}
