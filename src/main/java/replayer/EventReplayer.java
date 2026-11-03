package replayer;

import model.MarketEvent;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class EventReplayer{
    //read existing csv
    //convert each valid record back into MarketEvent
    //return the reconstructed Events

    //init scanner here as to not close until file finished
    private final Path path;

    public EventReplayer(Path path) {
        this.path = path;

    }

    public List<MarketEvent> readEvents() throws IOException {
        int lineNumber =0;
        List<MarketEvent> list = new ArrayList<>();
        try (Scanner reader = new Scanner(path)) {
            while (reader.hasNextLine()) {
                //read oneline
                String csvEvent = reader.nextLine();
                lineNumber+=1;
                //pass that line into converter method
                try {
                    list.add(convertToMarketEvent(csvEvent));
                } catch (IllegalArgumentException e) {
                    System.err.println("Invalid row. Row Number:" + lineNumber + ". Error:" + e);
                }

            }
        }
        return list;

    }

    public MarketEvent convertToMarketEvent(String csvEvent) {
        //List<MarketEvent> list = new ArrayList<>();
        String[] part = csvEvent.split(",",-1);
        if (part.length != 3) {
            throw new IllegalArgumentException("Expected 3 fields");
        }
        //define values only after data structure proved to be in valid format

        if(part[0].isBlank() || part[1].isBlank() || part[2].isBlank()) {
            throw new IllegalArgumentException("Expected non blank fields");
        }

        Double price = Double.valueOf(part[1]);
        Long timestamp = Long.valueOf(part[2]);
        //checks NaN and +- infinite values
        if (!Double.isFinite(price)) {
            throw new NumberFormatException("Invalid Price");
        }
        MarketEvent event = new MarketEvent(
                part[0],
                price,
                timestamp
        );
        //list.add(event);
        return event;
    }


}
