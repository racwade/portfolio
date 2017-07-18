package main.java.backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {

	public static void main(String[] args) {

        // load relevant log files
        String transactionLog;
        String userLog;
        String eventLog;

        // this is pretty naive
        if (args.length == 3) {
            transactionLog = args[0];
            userLog        = args[1];
            eventLog       = args[2];
        } else {
            transactionLog = "./logs/transactions.log";
            userLog        = "./logs/accounts.log";
            eventLog       = "./logs/tickets.log";
        }

        TransactionProcessor tp = new TransactionProcessor(transactionLog, userLog, eventLog);
        //int status=tp.run(transactionLog, userLog, eventLog);
        int status = tp.run();
        if (status != Error.E_SUCCESS) {
            Error.generateError(status, "transaction processor exited with error code in main");
        }
    }
}
