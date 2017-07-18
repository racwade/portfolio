import main.java.backend.Event;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class EventTest {
    @Test
    public void createEventTest() {
        Event event = new Event("Metallica at the ACC", 3, 20.00, "Alex");
        assertEquals("USER CONSTRUCTOR: Error setting event name", "Metallica at the ACC", event.getName());
        assertEquals("USER CONSTRUCTOR: Error setting number of tickets", 3, event.getTickets());
        assertEquals("USER CONSTRUCTOR: Error setting price of tickets", 20.00f, event.getTicketPrice(), 0.0002);
        assertEquals("USER CONSTRUCTOR: Error setting owner of tickets", "Alex", event.getOwner());

    }
}
