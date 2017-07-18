package main.java.backend;

public class User {
    /*** Variables ***/
    private String name;
    private String type;
    private double balance;

    public static final double UPPER_CREDIT = 999999.00;
    public static final double LOWER_CREDIT =      0.00;

    /*** Methods ***/
    public User(String name, String type, double balance) {
        this.name = name;
        this.type = type;
        this.balance = balance;
    }

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the balance
	 */
	public double getBalance() {
		return balance;
	}

	/**
	 * @param balance the balance to set
	 */
	public void setBalance(double balance) {
		this.balance = balance;
	}

    public void incrementBalance(double balance) {
        // validation for this should be done before the function is called
        this.balance += balance;
    }

    public void decrementBalance(double balance) {
        // validation for this should be done before the function is called
        this.balance -= balance;
    }
}
