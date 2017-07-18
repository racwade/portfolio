import main.java.backend.User;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UserTest {
    private double eps = 0.0002;

    @Test public void constructorTest() {
        User user = new User("michael", "AA", 50.00);
        assertEquals("USER CONSTRUCTOR: Error setting username", "michael", user.getName());
        assertEquals("USER CONSTRUCTOR: Error setting user type", "AA", user.getType());
        assertEquals("USER CONSTRUCTOR: Error setting balance", 50.00f, user.getBalance(), eps);
    }

    @Test
    public void setNameTest() {
        User user = new User("michael", "AA", 50.00);
        user.setName("test01");
        // also tests getName
        assertEquals("USER SET NAME: Error setting user name", "test01", user.getName());
    }

    @Test
    public void setTypeTest() {
        User user = new User("michael", "AA", 50.00);
        user.setType("TT");
        // also tests getType
        assertEquals("USER SET TYPE: Error setting user type", "TT", user.getType());
    }

    @Test
    public void setBalanceTest() {
        User user = new User("michael", "AA", 50.00);
        user.setBalance(10.00);
        // also tests getBalance
        assertEquals("USER SET BALANCE: Error setting user balance", 10.00, user.getBalance(), eps);
    }

    @Test
    public void incrementBalanceTest() {
        User user = new User("michael", "AA", 50.00);
        user.incrementBalance(10.00);
        assertEquals("USER INCR BALANCE: Error balance did not increment properly", 60.00, user.getBalance(), eps);
    }

    @Test
    public void decrementBalanceTest() {
        User user = new User("michael", "AA", 50.00);
        user.decrementBalance(10.00);
        assertEquals("USER INCR BALANCE: Error balance did not increment properly", 40.00, user.getBalance(), eps);
    }
}
