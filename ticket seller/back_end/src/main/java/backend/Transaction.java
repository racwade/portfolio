package main.java.backend;

import java.util.*;

public class Transaction {
    /*** Variables ***/
    // static variables for convenience
    public static final int T_LOGOUT = 0;
    public static final int T_CREATE = 1;
    public static final int T_DELETE = 2;
    public static final int T_SELL   = 3;
    public static final int T_BUY    = 4;
    public static final int T_REFUND = 5;
    public static final int T_CREDIT = 6;

    //Represents a single transaction.
    private int transaction_id; // Type of transaction.
    private HashMap<String,String> attributes; //Map of transaction values.

    /*** Methods ***/
    public Transaction() {
        this.transaction_id = -1;
        this.attributes = new HashMap<String, String>();
    }

    public int getId(){
        return this.transaction_id;
    }

    public String getPrimaryUser(){
        //return primary user
        Object result = attributes.get("primary user");
        if(result != null){
            return (String)result;
        }
        //TODO: throw error
        return "";
    }
    public String getSecondaryUser(){
        //return secondary user (if present)
        Object result = attributes.get("secondary user");
        if(result != null){
            return (String)result;
        }
        //TODO: throw error
        return "";
    }
    public String getUserType(){
        //return user type (if present)
        Object result = attributes.get("user type");
        if(result != null){
            return (String)result;
        }
        //TODO: throw error
        return "";
    }
    public double getCredit(){
        //return credit (if present)
        Object result = attributes.get("credit");
        if(result != null){
            return Double.parseDouble((String)result);
        }
        //TODO: throw error
        return 0.0f;
    }
    public String getEventName(){
        //return event name (if present)
        Object result = attributes.get("event name");
        if(result != null){
            return (String)result;
        }
        //TODO: throw error
        return "";
    }
    public int getNumberOfTickets(){
        //return number of tickets (if present)
        Object result = attributes.get("number of tickets");
        if(result != null){
            return Integer.parseInt((String)result);
        }
        //TODO: throw error
        return 0;
    }
    public double getTicketPrice(){
        //return ticket price (if present)
        Object result = attributes.get("ticket price");
        if(result != null){
            return Double.parseDouble((String)result);
        }
        //TODO: throw error
        return 0.0f;
    }

    /**
     * For transactions of type 00, 01, 02, 06
     * @param {int} code - the transaction code
     * @param {String} username - the username to be deleted, created, 
     *                            logged out, or to whom to add credit
     * @param {String} userType - the type of the user AA, FS, BS, SS
     * @param {String} credit - the amount of credit the user has or 
     *                          is to be added
     */
    public int setUserTransaction(int code, String username, String userType, String credit) {
        if (this.transaction_id != -1) {
            return Error.E_TRANSACTION_SET;
        }
        if (!(code == T_LOGOUT || code == T_CREATE || code == T_DELETE || code == T_CREDIT)) {
            return Error.E_INV_TRAN_TYPE;
        }
        this.transaction_id = code;
        this.attributes.put("primary user", username);
        this.attributes.put("user type", userType);
        this.attributes.put("credit", credit);
        return Error.E_SUCCESS;
    }

    /**
     * For transactions of type 05 create a transaction record
     *
     * @param {String} buyerUsername - the buyer's username
     * @param {String} sellerUsername - the seller's username
     * @param {String} credit - the credit of the return
     */
    public int setRefundTransaction(String buyerUsername, String sellerUsername, String credit) {
        if (this.transaction_id != -1) {
            return Error.E_TRANSACTION_SET;
        }
        this.transaction_id = T_REFUND;
        this.attributes.put("primary user", buyerUsername);
        this.attributes.put("secondary user", sellerUsername);
        this.attributes.put("credit", credit);
        return Error.E_SUCCESS;
    }

    /**
     * For transactions of type 03, 04 create a transaction record
     *
     * @param {int} code - the transaction code 03 or 04
     * @param {String} eventName - the name of the event
     * @param {String} sellerUsername - the seller's username
     * @param {String} numTickets - the number of tickets being purchased or sold
     * @param {String} ticketPrice - the price per ticket
     */
    public int setPurchaseTransaction(int code, String eventName, String sellerUsername, String numTickets, String ticketPrice) {
        if (this.transaction_id != -1) {
            return Error.E_TRANSACTION_SET;
        }
        if (!(code == T_SELL || code == T_BUY)) {
            return Error.E_INV_TRAN_TYPE;
        }
        this.transaction_id = code;
        this.attributes.put("event name", eventName);
        this.attributes.put("primary user", sellerUsername);
        this.attributes.put("number of tickets", numTickets);
        this.attributes.put("ticket price", ticketPrice);
        return Error.E_SUCCESS;
    }

    public String toString() {
        String id = "0" + (new Integer(this.transaction_id).toString());
        // TODO Michael implement this
        if (this.transaction_id == T_LOGOUT
            || this.transaction_id == T_CREATE
            || this.transaction_id == T_DELETE
            || this.transaction_id == T_CREDIT) {
            return id
                + " " + this.attributes.get("primary user")
                + " " + this.attributes.get("user type")
                + " " + this.attributes.get("credit") ;

        } else if (this.transaction_id == T_SELL
                   || this.transaction_id == T_BUY) {
            return id
                + " " + this.attributes.get("event name")
                + " " + this.attributes.get("primary user")
                + " " + this.attributes.get("number of tickets")
                + " " + this.attributes.get("ticket price");

        } else if (this.transaction_id == T_REFUND) {
            return id
                + " " + this.attributes.get("primary user")
                + " " + this.attributes.get("secondary user")
                + " " + this.attributes.get("credit");

        } else {
            // error or unset
            return "Unset Transaction";
        }
    }
}
