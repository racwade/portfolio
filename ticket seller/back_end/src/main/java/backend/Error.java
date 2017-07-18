package main.java.backend;

import java.lang.Math;
import java.lang.Double;

public class Error {
    /********* Variables *********/
    public static final int MAX_ERR           = 16384; // TODO Change as more errors are input

    public static final int E_SUCCESS         =     0;
    public static final int E_NO_CREDIT       =     1;
    public static final int E_DUPLICATE_USER  =     2;
    public static final int E_NO_SUCH_USER    =     4;
    public static final int E_MALFORMED       =     8;
    public static final int E_SOLD_OUT        =    16;
    public static final int E_UNPRIVILEGED    =    32;
    public static final int E_NO_SUCH_EVENT   =    64;
    public static final int E_MISSING_FILE    =   128;
    public static final int E_WRITE_FAILURE   =   256;
    public static final int E_DUPLICATE_EVENT =   512;
    public static final int E_INVALID_TRANS   =  1024;
    public static final int E_INV_TRAN_TYPE   =  2048;
    public static final int E_TRANSACTION_SET =  4096;
    public static final int E_EXCESS_CREDIT   =  8192;
    public static final int E_INSUFF_CREDIT   = 16384;

    private static final String[] E_ARR = new String[] {
        // automatic size allocation
        "Insufficient credit amount.",
        "This user already exists.",
        "This user does not exist.",
        "Malformed log entry.",
        "This event is sold out.",
        "User does not have appropriate priveleges.",
        "This event does not exist.",
        "The file does not exist.",
        "Writing to file failed.",
        "This event already exists.",
        "Invalid transaction.",
        "Invalid transaction type for this method.",
        "Transaction has already been set to a type.",
        "Transaction would cause overflow of credit.",
        "Transaction would cause underflow of credit."
    };


    /********* Methods *********/

    /**
     * Taking in a bitwise or of zero or more flags
     * prints to the console, all relevant error messages
     *
     * @param {int} errorCode - The bitwise or of zero or more flags
     * @param {String} message - The message containing information 
     *                           about where the error occured
     */
    static void generateError(int errorCode, String message) {
        if (errorCode <= 0) {
            return;
        }
        for (int i = 1; i <= MAX_ERR; i*=2) {
            if ((errorCode & i) == i) {
                int index = new Double((Math.log((double) i)
                                        / Math.log(2))).intValue();
                System.err.print("ERROR: " + E_ARR[index]);
                System.err.println(" " + message);
            }
        }
    }
}
