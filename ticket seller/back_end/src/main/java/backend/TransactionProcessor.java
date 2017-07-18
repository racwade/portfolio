package main.java.backend;

import java.util.ArrayList;
import java.io.*;

public class TransactionProcessor {
    /*** Variables ***/
    // data structures in memory
    private ArrayList<Transaction> transactions;
    private ArrayList<User> users;
    private ArrayList<Event> events;

    // files
    private BufferedReader transactionFile;
    private BufferedReader userFile;
    private BufferedReader eventFile;

    private String eventLog, userLog;

    /***  Methods ***/
    public TransactionProcessor(String transactionLog, String userLog, String eventLog) {
        try {
            this.transactionFile = new BufferedReader(new InputStreamReader(new FileInputStream(transactionLog)));
            this.userFile = new BufferedReader(new InputStreamReader(new FileInputStream(userLog)));
            this.eventFile = new BufferedReader(new InputStreamReader(new FileInputStream(eventLog)));

            this.transactions = new ArrayList<Transaction>();
            this.users = new ArrayList<User>();
            this.events = new ArrayList<Event>();

            this.eventLog=eventLog;
            this.userLog=userLog;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

	/**
	 * @return the transactionFile
	 */
	public BufferedReader getTransactionFile() {
		return transactionFile;
	}

	/**
	 * @return the userFile
	 */
	public BufferedReader getUserFile() {
		return userFile;
	}

	/**
	 * @return the eventFile
	 */
	public BufferedReader getEventFile() {
		return eventFile;
	}

    public int run() {
        // parse the relevant files
        int puf = parseUserFile();
        if (puf != Error.E_SUCCESS) {
            Error.generateError(puf, "failed while parsing user file");
            return puf;
        }
        int pef = parseEventFile();
        if (pef != Error.E_SUCCESS) {
            Error.generateError(pef, "failed while parsing event file");
            return pef;
        }
        int ptf = parseTransactionFile();
        if (ptf != Error.E_SUCCESS) {
            Error.generateError(ptf, "failed while parsing transaction file");
            return ptf;
        }
        // run through transactions and apply all applicable ones
        // get first user
        int currentPosition = 0;
        User currentUser = getNextLogout(currentPosition);
        if (currentUser == null) {
            // first time it's null? that's a problem
            return Error.E_NO_SUCH_USER;
        }

        // iterate through transactions
        for (Transaction transaction : transactions) {
            if (transaction.getId() == Transaction.T_LOGOUT) {
                currentUser = getNextLogout(currentPosition);
            } else if (transaction.getId() == Transaction.T_CREATE) {
                runCreate(transaction, currentUser);

            } else if (transaction.getId() == Transaction.T_DELETE) {
                runDelete(transaction, currentUser);

            } else if (transaction.getId() == Transaction.T_SELL) {
                runSell(transaction, currentUser);

            } else if (transaction.getId() == Transaction.T_BUY) {
                runBuy(transaction, currentUser);

            } else if (transaction.getId() == Transaction.T_REFUND) {
                runRefund(transaction, currentUser);

            } else if (transaction.getId() == Transaction.T_CREDIT) {
                runAddCredit(transaction, currentUser);

            }
            currentPosition++;
        }
        return 0;
    }

    private int parseUserFile() {
        String line = null;
        try {
            while ((line = userFile.readLine()) != null) {
                if (line.length() != 28) {
                    return Error.E_MALFORMED;
                }
                String username = line.substring(0, 15).trim();
                // skip a space
                String userType = line.substring(16, 18).trim();
                double balance   = new Double(line.substring(19).trim()).doubleValue();
                if (balance > User.UPPER_CREDIT) {
                    return Error.E_EXCESS_CREDIT;
                }
                if (balance < User.LOWER_CREDIT) {
                    return Error.E_INSUFF_CREDIT;
                }

                User u = new User(username, userType, balance);
                users.add(u);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Error.E_SUCCESS;
    }

    private int parseEventFile() {
        String line = null;
        try {
            while ((line = eventFile.readLine()) != null) {
                if (line.length() != 52) {
                    return Error.E_MALFORMED;
                }
                String eventName  = line.substring(0, 25).trim();
                String owner      = line.substring(26, 41).trim();
                int numTickets    = new Integer(line.substring(42, 44).trim()).intValue();
                double ticketPrice = new Double(line.substring(46)).doubleValue();
                events.add(new Event(eventName, numTickets, ticketPrice, owner));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Error.E_SUCCESS;
    }

    private int parseTransactionFile() {
        String line = null;
        try {
            while((line = transactionFile.readLine()) != null){
                Transaction transaction = new Transaction();
                int transType = new Integer(line.substring(0, 2)).intValue(); // first two chars are always type
                if (transType    == Transaction.T_LOGOUT
                    || transType == Transaction.T_CREATE
                    || transType == Transaction.T_DELETE
                    || transType == Transaction.T_CREDIT) {
                    // logout, create, delete, add credit

                    String username = line.substring(3,  18).trim();
                    String userType = line.substring(19, 21).trim();
                    String credit   = line.substring(22);
                    transaction.setUserTransaction(transType, username, userType, credit);
                    transactions.add(transaction);

                } else if (transType == Transaction.T_SELL
                           || transType == Transaction.T_BUY) {
                    // sell, buy

                    String eventName      = line.substring(3,  28).trim();
                    String sellerUsername = line.substring(29, 44).trim();
                    String numTickets     = line.substring(45, 48);
                    String ticketPrice    = line.substring(49);
                    transaction.setPurchaseTransaction(transType, eventName,
                                                       sellerUsername, numTickets,
                                                       ticketPrice);
                    transactions.add(transaction);

                } else if (transType == Transaction.T_REFUND) {
                    // refund

                    String buyerUsername  = line.substring(3, 18).trim();
                    String sellerUsername = line.substring(19, 34).trim();
                    String credit         = line.substring(35);
                    transaction.setRefundTransaction(buyerUsername, sellerUsername, credit);
                    transactions.add(transaction);

                } else {
                    // error
                    return Error.E_INVALID_TRANS;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Error.E_SUCCESS;
    }

    /**
     * Writes the newly updated event info to the available tickets file
     */

    private void writeEventFile(){
      try{
        PrintWriter writer=new PrintWriter(this.eventLog);
        for (Event e:events){
          int numChar=e.getName().length();
          String name=e.getName();
          for(int i=0;i<25-numChar;i++){
            name.concat(" ");
          }
          writer.println(name+" "+e.getTickets()+" "+e.getTicketPrice()+" "+searchUsers(e.getOwner()).getName());
        }
        writer.close();

      }catch(Exception e){
        e.printStackTrace();
      }
    }

    /**
     * Writes the newly updated user accounts info to the current user
     * accounts file
     */

    private void writeUserFile(){
      try{
        PrintWriter writer=new PrintWriter(this.userLog);
        for (User u:users){
          int numChar=u.getName().length();
          String name=u.getName();
          for(int i=0;i<15-numChar;i++){
            name.concat(" ");
          }
          writer.println(name+" "+u.getType()+" "+u.getBalance());
        }
        writer.close();

      }catch(Exception e){
        e.printStackTrace();
      }
    }

    /**
     * Returns the user name of the current user by parsing until the next logout string 00
     * and getting that username
     *
     * @param {int} currentPosition - the current position in the array
     * @return {String} - the user name of the current user
     */
    private User getNextLogout(int currentPosition) {
        if (currentPosition == transactions.size()-1) {
            return null;
        }
        // upon request and the current position we want to find
        // the next logout line
        int i = 1;
        Transaction t = transactions.get(currentPosition + i);

        while (t.getId() != Transaction.T_LOGOUT) {
            i++;
            t = transactions.get(currentPosition + i);
        }
        return searchUsers(t.getPrimaryUser());
    }

    public void runCreate(Transaction trans, User issuer) {
		int err = validateCreate(trans, issuer);
		if (err == Error.E_SUCCESS) {
			User user = new User(trans.getPrimaryUser(),
                                 trans.getUserType(),
                                 trans.getCredit());
			users.add(user);
		} else {
			Error.generateError(err, "validation of create function failed");
		}
        return;
    }

    public void runDelete(Transaction transaction, User currentUser) {
		int err = validateDelete(transaction, currentUser);
		String username = transaction.getPrimaryUser();
		if (err == Error.E_SUCCESS) {
			for(int i = 0; i < users.size(); i++){
				if(username.equals(users.get(i).getName())) {
					//delete that user
					users.remove(i);
					return;
				}
			}
			Error.generateError(Error.E_NO_SUCH_USER, "validation of delete function failed");
		} else {
			Error.generateError(err, "validation of delete function failed");
		}
        return;
    }

    /**
     * Executes the sell transaction
     *
     * @param {Transaction} transaction - the current transaction
     * @param {User} currentUser - the current user
     *
     */
    public void runSell(Transaction transaction, User currentUser) {
        int error = validateSell(transaction, currentUser);
        if (error == 0) {
            // get parameters for new event
            String eventName = transaction.getEventName();
            int numTickets = transaction.getNumberOfTickets();
            double price = transaction.getTicketPrice();
            String sellerUsername = transaction.getPrimaryUser();

            // create and add event
            Event e = new Event(eventName, numTickets, price, sellerUsername);
            events.add(e);
        } else {
            Error.generateError(error, "validation of sell function failed");
        }

        return;
    }

    /**
     * Executes the buy transaction
     *
     * @param {Transaction} transaction - the current transaction
     * @param {User} currentUser - the current user
     *
     */
    public void runBuy(Transaction transaction, User currentUser) {
	    int error = validateBuy(transaction, currentUser);
	    if (error == 0) {
            Event event = searchEvents(transaction.getEventName(),
                                       transaction.getPrimaryUser());
            User seller = searchUsers(transaction.getPrimaryUser());
            double totalValue = transaction.getNumberOfTickets() * transaction.getTicketPrice();

            // update the event's number of tickets
            event.decrementTickets(transaction.getNumberOfTickets());

            // update the buyer's balance
            // validation is done in validateBuy
            currentUser.decrementBalance(totalValue);

            // update the seller's balance
            // validation is done in validateBuy
            seller.incrementBalance(totalValue);

	    } else {
		    Error.generateError(error, "validation of buy function failed");
	    }
    }

    public void runRefund(Transaction transaction, User currentUser) {
        int error = validateRefund(transaction, currentUser);
        if (error == Error.E_SUCCESS) {
            double refundAmount = transaction.getCredit();
            // incerement seller
            User seller = searchUsers(transaction.getPrimaryUser());
            seller.incrementBalance(refundAmount);

            // decrement buyer
            User buyer = searchUsers(transaction.getSecondaryUser());
            buyer.decrementBalance(refundAmount);
        } else {
		    Error.generateError(error, "validation of refund function failed");
        }
    }

    public void runAddCredit(Transaction transaction, User currentUser) {
        int error = validateAddcredit(transaction, currentUser);
        if (error == Error.E_SUCCESS) {
            // add credit to the recipient
            User recipient = searchUsers(transaction.getPrimaryUser());
            double creditAmount = transaction.getCredit();
            recipient.incrementBalance(creditAmount);

        } else {
		    Error.generateError(error, "validation of addcredit function failed");
        }
    }

    // Validation functions
    private int validateCreate(Transaction transaction, User currentUser) {
		if(transaction.getId() != Transaction.T_CREATE) {
            // must be a create transaction
            return Error.E_INV_TRAN_TYPE;
        }
		if(!currentUser.getType().equals("AA")) {
            // must be an admin to run this command
            return Error.E_UNPRIVILEGED;
        }
		
        // search through users to find whether or not this user exists
        if (searchUsers(transaction.getPrimaryUser()) != null) {
            return Error.E_DUPLICATE_USER;
        }
		
        return Error.E_SUCCESS;
    }
	
    private int validateDelete(Transaction transaction, User currentUser) {
		if(transaction.getId() != Transaction.T_DELETE) {
            return Error.E_INV_TRAN_TYPE; //must be a delete transaction
        }

		if(!currentUser.getType().equals("AA")) {
            // must be AA to run this command
            return Error.E_UNPRIVILEGED;
        }
		
		if(searchUsers(transaction.getPrimaryUser()) == null) {
            // no such user found
            return Error.E_NO_SUCH_USER;
        }
        return Error.E_SUCCESS;
    }
	
    /**
     * Ensures that the information in the sell transaction is valid
     *
     * @param {Transaction} transaction - the current transaction
     * @param {User} currentUser - the current user
     *
     * @return {int} error code - the error code
     */
    public int validateSell(Transaction transaction, User currentUser) {
        if (transaction.getId() != Transaction.T_SELL) {
            return Error.E_INV_TRAN_TYPE; //must be a sell transaction
        }
        if (!(currentUser.getType().equals("AA")
              || currentUser.getType().equals("SS")
              || currentUser.getType().equals("FS"))) {
            //must be AA, SS, or FS to run this command
            return Error.E_UNPRIVILEGED;
        }

        // ticket price checking happens in frontend and is static

        if (searchEvents(transaction.getEventName(), transaction.getPrimaryUser()) != null) {
            return Error.E_DUPLICATE_EVENT;
        }
        return Error.E_SUCCESS;
    }

    /**
     * Ensures that the information in the buy transaction is valid
     *
     * @param {Transaction} transaction - the current transaction
     * @param {User} currentUser - the current user
     *
     * @return {int} error code - the error code
     */
    public int validateBuy(Transaction transaction, User currentUser) {
        if(transaction.getId() != Transaction.T_BUY) {
            //must be a buy transaction
            return Error.E_INV_TRAN_TYPE;
        }

        if (!(currentUser.getType().equals("AA")
              || currentUser.getType().equals("BS")
              || currentUser.getType().equals("FS"))) {
            //must be AA, SS, or FS to run this command
            return Error.E_UNPRIVILEGED;
        }

        double totalValue = transaction.getNumberOfTickets() * transaction.getTicketPrice();
        User seller = searchUsers(transaction.getPrimaryUser());
        if (seller.getBalance() + totalValue > User.UPPER_CREDIT) {
            // if this would put the seller over their limit, reject the transaction
            return Error.E_EXCESS_CREDIT;
        }

        // current user is buyer for buy transactions
        if ((currentUser.getBalance() - totalValue) < User.LOWER_CREDIT) {
            // buyer does not currently have enough credit
            return Error.E_INSUFF_CREDIT;
        }

        Event event = searchEvents(transaction.getEventName(), transaction.getPrimaryUser());
        if (event != null) {
            // if the event exists
            int numTickets = transaction.getNumberOfTickets();
            if(currentUser.getBalance() < event.getTicketPrice() * numTickets) {
                return Error.E_NO_CREDIT;
            }
            if(event.getTickets() < numTickets) {
                return Error.E_SOLD_OUT;
            }
        } else {
            return Error.E_NO_SUCH_EVENT;
        }
        return Error.E_SUCCESS;
    }

    private int validateRefund(Transaction transaction, User currentUser) {
        if (!currentUser.getType().equals("AA")) {
            // priveleged transaction
            return Error.E_UNPRIVILEGED;
        }

        User buyer = searchUsers(transaction.getPrimaryUser());
        User seller = searchUsers(transaction.getSecondaryUser());
        if (buyer == null || seller == null) {
            return Error.E_NO_SUCH_USER;
        }

        double refundAmount = transaction.getCredit();
        // will the refund put the buyer under the limit?
        if (buyer.getBalance() < refundAmount) {
            return Error.E_INSUFF_CREDIT;
        }

        // will the refund put the seller over the limit?
        if (seller.getBalance() + refundAmount > User.UPPER_CREDIT) {
            return Error.E_EXCESS_CREDIT;
        }
        return Error.E_SUCCESS;
    }

    private int validateAddcredit(Transaction transaction, User currentUser) {
        User recipient = searchUsers(transaction.getPrimaryUser());

        if (currentUser.getType().equals("AA")) {
            if (recipient == null) {
                return Error.E_NO_SUCH_USER;
            }
        }
        if (!(transaction.getPrimaryUser().equals(currentUser.getName())
            || currentUser.getType().equals("AA"))) {
            return Error.E_UNPRIVILEGED;
        }

        double addAmount = transaction.getCredit();

        // credit add of less than 0.01 omitted, checked in front end,
        // no feasible way of conveying to back end given logging format
        if (recipient.getBalance() + addAmount > User.UPPER_CREDIT) {
            return Error.E_EXCESS_CREDIT;
        }
        return Error.E_SUCCESS;
    }

    public User searchUsers(String username) {
        for (User user : users) {
            if (user.getName().equals(username)) {
                return user;
            }
        }
        return null;
    }

    public Event searchEvents(String eventName, String eventOwner) {
        for (Event event : events) {
            if (event.getName().equals(eventName) && event.getOwner().equals(eventOwner)) {
                return event;
            }
        }
        return null;
    }

}
