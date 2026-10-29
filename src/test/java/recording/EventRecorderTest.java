package recording;

import model.MarketEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EventRecorderTest {
    //private final String filePath = "marketCSVFile.text";
    private final long testTimestamp = 100L;
    MarketEvent event = new MarketEvent(
            "test",
            1.00,
            testTimestamp
    );
    MarketEvent event2 = new MarketEvent(
            "test2",
            2.00,
            testTimestamp
    );


    @Test
    public void eventRecorderShouldWriteToFile(@TempDir Path tempDir) throws IOException {
        Path filePath = tempDir.resolve("market.csv");
        try (EventRecorder recorder = new EventRecorder(filePath)) {
            recorder.record(event);
            recorder.record(event2);

        }
        List<String> lines = Files.readAllLines(filePath);
        assertEquals(3, lines.size());

        assertEquals("symbol,price,timestamp", lines.get(0));
        assertEquals("test,1.0,100", lines.get(1));
        assertEquals("test2,2.0,100", lines.get(2));
        assertEquals(
                List.of(
                        "symbol,price,timestamp",
                        "test,1.0,100",
                        "test2,2.0,100"
                ),
                lines
        );

    }
    @Test
    public void shouldRejectInvalidFilePath(@TempDir Path tempDir) {
        assertThrows(IOException.class, () -> {
            Path invalidPath = tempDir.resolve("missing/file.csv");
            EventRecorder recorder = new EventRecorder(invalidPath);
            //recorder.record(event);
        });
    }

    @Test
    public void shouldRejectWritesAfterClose(@TempDir Path tempDir)
            throws IOException {

        Path fileName = tempDir.resolve("market.csv");

        EventRecorder recorder = new EventRecorder(fileName);

        try (recorder) {
            recorder.record(event);
        }

        assertThrows(IOException.class, () -> recorder.record(event2));
    }
}
