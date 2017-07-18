import static org.junit.Assert.assertEquals;
import main.java.backend.Transaction;

import org.junit.Test;

public class TransactionTest {
	@Test
	public void LogoutTransactionTests() {
		Transaction trans = new Transaction();
		trans.setUserTransaction(Transaction.T_LOGOUT,"user","AA","1.0");
		assertEquals("LOGOUT Transaction ID must be 0",0,trans.getId());
		assertEquals("LOGOUT Transaction username must be 'user'","user",trans.getPrimaryUser());
		assertEquals("LOGOUT Transaction usertype must be 'AA'","AA",trans.getUserType());
		assertEquals("LOGOUT Transaction credit must be 1.0",1.0f, trans.getCredit(), 0.0002);
	}

    @Test
	public void CreateTransactionTests() {
		Transaction trans = new Transaction();
		trans.setUserTransaction(Transaction.T_CREATE,"user","AA","1.0");
		assertEquals("CREATE Transaction ID must be 1",1,trans.getId());
		assertEquals("CREATE Transaction username must be 'user'","user",trans.getPrimaryUser());
		assertEquals("CREATE Transaction usertype must be 'AA'","AA",trans.getUserType());
		assertEquals("CREATE Transaction credit must be 1.0",1.0f, trans.getCredit(), 0.0002);
	}

    @Test
	public void DeleteTransactionTests() {
		Transaction trans = new Transaction();
		trans.setUserTransaction(Transaction.T_DELETE,"user","AA","1.0");
		assertEquals("DELETE Transaction ID must be 2",2,trans.getId());
		assertEquals("DELETE Transaction username must be 'user'","user",trans.getPrimaryUser());
		assertEquals("DELETE Transaction usertype must be 'AA'","AA",trans.getUserType());
		assertEquals("DELETE Transaction credit must be 1.0",1.0f, trans.getCredit(), 0.0002);
	}

    @Test
	public void SellTransactionTests() {
		Transaction trans = new Transaction();
		trans.setPurchaseTransaction(Transaction.T_SELL,"event","user","1","1.0");
		assertEquals("SELL Transaction ID must be 3",3,trans.getId());
		assertEquals("SELL Transaction event name must be 'event'","event",trans.getEventName());
		assertEquals("SELL Transaction username must be 'user'","user",trans.getPrimaryUser());
		assertEquals("SELL Transaction number of tickets must be 1",1,trans.getNumberOfTickets());
		assertEquals("SELL Transaction ticket price must be 1.0",1.0f,trans.getTicketPrice(), 0.0002);
	}

    @Test
	public void BuyTransactionTests() {
		Transaction trans = new Transaction();
		trans.setPurchaseTransaction(Transaction.T_BUY,"event","user","1","1.0");
		assertEquals("BUY Transaction ID must be 3",4,trans.getId());
		assertEquals("BUY Transaction event name must be 'event'","event",trans.getEventName());
		assertEquals("BUY Transaction username must be 'user'","user",trans.getPrimaryUser());
		assertEquals("BUY Transaction number of tickets must be 1",1,trans.getNumberOfTickets());
		assertEquals("BUY Transaction ticket price must be 1.0",1.0f,trans.getTicketPrice(), 0.0002);
	}

    @Test
	public void RefundTransactionTests() {
		Transaction trans = new Transaction();
		trans.setRefundTransaction("user1","user2","1.0");
		assertEquals("REFUND Transaction ID must be 5",5,trans.getId());
		assertEquals("REFUND Transaction primary username must be 'user1'","user1",trans.getPrimaryUser());
		assertEquals("REFUND Transaction secondary username must be 'user2'","user2",trans.getSecondaryUser());
		assertEquals("REFUND Transaction credit must be 1.0",1.0f, trans.getCredit(), 0.0002);
	}

    @Test
	public void AddcreditTransactionTests() {
		Transaction trans = new Transaction();
		trans.setUserTransaction(Transaction.T_CREDIT,"user","AA","1.0");
		assertEquals("ADDCREDIT Transaction ID must be 6",6,trans.getId());
		assertEquals("ADDCREDIT Transaction username must be 'user'","user",trans.getPrimaryUser());
		assertEquals("ADDCREDIT Transaction usertype must be 'AA'","AA",trans.getUserType());
		assertEquals("ADDCREDIT Transaction credit must be 1.0",1.0f, trans.getCredit(), 0.0002);
	}
}
