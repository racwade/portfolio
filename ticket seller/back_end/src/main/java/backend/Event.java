package main.java.backend;

import java.util.*;

public class Event {
    /*** Variables ***/
    private String name;
    private int tickets;
    private double ticketPrice;
    private String owner;

    // less than or equal to
    public static final double MAX_PPT = 999.99;
    // strictly greater than
    public static final double MIN_PPT =   0.00;

    /*** Methods ***/
    // constructor
    public Event(String name, int tickets, double ticketPrice, String owner) {
        this.name = name;
        this.tickets = tickets;
        this.ticketPrice = ticketPrice;
        this.owner = owner;
    }
    public String getName() {
        return this.name;
    }
    public int getTickets() {
        return this.tickets;
    }
    public double getTicketPrice() {
        return this.ticketPrice;
    }
    public String getOwner() {
        return this.owner;
    }

    public void decrementTickets(int num) {
        // validation for this should be done before this function is ever called
        this.tickets -= num;
    }
	public int setName(String n) {
		// TODO: set name
		return 0;
	}
	public int setTickets(int num) {
		// TODO: set tickets
		return 0;
	}
	public int setTicketPrice(double price) {
		// TODO: set ticket price
		return 0;
	}
	public int setOwner(User owner){
		// TODO: set owner
		return 0;
	}
}
