import main.java.backend.TransactionProcessor;
import main.java.backend.Transaction;
import main.java.backend.User;
import main.java.backend.Event;
import main.java.backend.Error;

import static org.junit.Assert.assertEquals;
//import org.junit.Assert.assertEquals;

import org.junit.Test;

public class BuySellTest {
    @Test
    public void runBuySellTest() {
        TransactionProcessor tp = new TransactionProcessor("./logs/transactionLog", "./logs/userLog", "./logs/eventLog");
        Transaction t=new Transaction();
        int status=t.setPurchaseTransaction(3,"Metallica at ACC","alex","2","20.00");
        assertEquals("USER CONSTRUCTOR: Error setting transaction id", 3, status);

        //make sure the function runs properly
        User user=new User("alex","AA",999950.00);
        tp.runSell(t,user);
        assertEquals("USER CONSTRUCTOR: Error setting event name", "Metallica at ACC", t.getEventName());
        assertEquals("USER CONSTRUCTOR: Error setting number of tickets", 2, t.getNumberOfTickets());
        assertEquals("USER CONSTRUCTOR: Error setting price of tickets", 20.00f, t.getTicketPrice(), 0.0002);
        assertEquals("USER CONSTRUCTOR: Error setting owner of tickets", "alex", t.getPrimaryUser());

        //make sure transaction id is validated
        status=t.setPurchaseTransaction(4,"Metallica at ACC","alex","2","20.00");
        status=tp.validateSell(t,user);
        assertEquals("USER CONSTRUCTOR: Error validating transaction type", Error.E_INV_TRAN_TYPE, status);

        //make sure seller is not buy standard
        status=t.setPurchaseTransaction(3,"Metallica at ACC","alex","2","20.00");
        user=new User("buyer","BS",20.00);
        status=tp.validateSell(t,user);
        assertEquals("USER CONSTRUCTOR: Error validating user type", Error.E_UNPRIVILEGED, status);

        //make sure duplicate events are rejected
        status=t.setPurchaseTransaction(3,"Show 01","alex","2","20.00");
        user=new User("alex","AA",999990.00);
        status=tp.validateSell(t,user);
        assertEquals("USER CONSTRUCTOR: Error validating event name", Error.E_DUPLICATE_EVENT, status);

        //check to see if transaction id is correct using an incorrect one
        status=tp.validateBuy(t,user);
        assertEquals("USER CONSTRUCTOR: Error validating transaction", Error.E_INV_TRAN_TYPE, status);

        t=new Transaction();
        status=t.setPurchaseTransaction(4,"Metallica at ACC","alex","1","20.00");
        user=new User("buyer","BS",30.00);
        tp.runBuy(t,user);
        assertEquals("USER CONSTRUCTOR: Error getting event name", "Metallica at ACC", t.getEventName());
        assertEquals("USER CONSTRUCTOR: Error modifying number of tickets", 1, t.getNumberOfTickets());
        assertEquals("USER CONSTRUCTOR: Error getting price of tickets", 20.00f, t.getTicketPrice(), 0.0002);
        assertEquals("USER CONSTRUCTOR: Error getting owner of tickets", "alex", t.getPrimaryUser());

        assertEquals("USER CONSTRUCTOR: Error getting new balance", 10.00f, user.getBalance());

        //reject buy is insufficient credit
        user=new User("buyer","BS",10.00);
        status=tp.validateBuy(t,user);
        assertEquals("USER CONSTRUCTOR: Error with credit amount", Error.E_INSUFF_CREDIT, status);

        //trying to buy with SS account
        user=new User("seller","SS",200.00);
        status=tp.validateBuy(t,user);
        assertEquals("USER CONSTRUCTOR: Error with credit amount", Error.E_UNPRIVILEGED, status);

        //trying to buy with no available tickets
        status=t.setPurchaseTransaction(4,"Metallica at ACC","alex","3","20.00");
        user=new User("buyer","BS",200.00);
        status=tp.validateBuy(t,user);
        assertEquals("USER CONSTRUCTOR: Error with credit amount", Error.E_SOLD_OUT, status);

        //trying to buy tickets from an event that does not exist
        status=t.setPurchaseTransaction(4,"Queen at Rogers Centre","alex","3","20.00");
        status=tp.validateBuy(t,user);
        assertEquals("USER CONSTRUCTOR: Error with credit amount", Error.E_NO_SUCH_EVENT, status);

        status=t.setPurchaseTransaction(4,"Metallica at ACC","alex","1","20.00");
        status=tp.validateBuy(t,user);
        assertEquals("USER CONSTRUCTOR: Error with credit amount with seller", Error.E_EXCESS_CREDIT, status);





    }
}
