package recording;

import model.MarketEvent;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class EventRecorder implements AutoCloseable {
    private final BufferedWriter writer;


    public EventRecorder(Path path) throws IOException {
        //open or create file
        //creates new object BufferedWriter using path as filePath
        writer = Files.newBufferedWriter(path);
        writer.write("symbol,price,timestamp");
        writer.newLine();

        }



    public void record(MarketEvent event) throws IOException {
        //write one event as a csv row
        String csvFormatted = "%s,%s,%s".formatted(
                event.symbol(),
                event.price(),
                event.timestamp()
        );
        writer.write(csvFormatted);
        writer.newLine();
    }
    @Override
    public void close() throws IOException {
        //close file safely
        writer.close();
    }
}
