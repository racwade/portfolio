//Team Second Place's ticket frontend
//Alexander Crawford - 100569102
//Omar Khan - 100523629
//Samuel McCabe - 100558610

#include <stdlib.h>
#include <iostream>
#include <string>
#include <fstream>
#include "stdafx.h"
using namespace std;
string* accounts;
string* deleted;



//program intentions are strictly to outline all instances of a user and user action
//corresponding to that users possible capabilities as outlined in the prior instructions




//user capable of buying and selling
class FullStandardUser {

	int ticket_number_b;
	int ticket_number_s;
	float balance;
	float price;
	string username;
	string event_title;

	//admin = AA, Full Standard = FS buy = BS, sell = SS
	string usertype;

public:
	FullStandardUser(){
		return;
	}
	FullStandardUser(string name, string type, float bal) {
		username == name;
		balance == bal;
		usertype == type;
		return;
	}
	string getName() {
		//returns the user's name
		return username;
	}
	float getBalance() {
		//returns the user's balance
		return balance;
	}
	string getUserType() {
		//get the current usertype
		return usertype;
	}
	int credit(float amount) {
		//adds credit to the user
		balance += amount;
		return 0;
	}
	int debit(float amount) {
		//removes credit from the user
		if (amount > balance) {
			//can't take money that isn't there
			return -1;
		}
		else {
			balance -= amount;
		}
		return 0;
	}
	bool canBuy() {
		//user is capable of purchasing
		if (getUserType() != "SS"){
			return true;
		}
		else {
			return false;
		}
	}

	bool canSell() {
		//user is capable of seslling
		if (getUserType() != "BS") {
			return true;
		}
		else {
			return false;
		}
	}

	string sell() {
		if (canSell() == true) {

		}
		else {
			printf("This function is not available from your account!");
		}
	}
	string buy() {
		if (canBuy() == true) {

		}
		else {
			printf("This function is not available from your account!");
		}
	}

	bool isAdmin() {
		//user is an admin?
		if (getUserType() == "AA") {
			return true;
		}
	}

	float addCredit() {
		printf("Amount of credit wishing to add");
		int amount;
		std::cin >> amount;
		balance += amount;
	}
	//save_to_trans_file();

	float setBalance() {
		//declare a certain balance
		printf("enter balance:");
		std::cin >> balance;
	}

	float setBalance(float bal) {
		//set balance
		balance = bal;
	}
	string setName() {
		//declare a certain name
		printf("enter username:");
		std::cin >> username;
		for (int x = 15 - username.length(); x > 0; x--) {
			username = username + " ";
		}
	}

	string setName(string name) {
		//given name
		username = name;
		for (int x = 15 - username.length(); x > 0; x--) {
			username = username + " ";
		}
	}
	void setUserType() {
		//set the usertype for the account
		printf("enter usertype; admin, full, buy, sell:");
		string temp;
		bool correct = false;
		while (!correct) {
			std::cin >> temp;
			if (temp == "admin") {
				usertype = "AA";
				correct = true;
			}
			else if ( temp == "full"){
				usertype = "FS";
				correct = true;
			}
			else if (temp == "buy") {
				usertype = "BS";
				correct = true;
			}
			else if (temp == "sell") {
				usertype = "SS";
				correct == true;
			}
			else {
				printf("Please only enter a proper usertype; admin, full, buy, sell");
			}
		}
		int buy() {
			printf("Enter event name:");
			std::cin >> event_title;
			printf("Enter number of tickets:");
			std::cin >> ticket_number_b;
			printf("Enter username:");
			std::cin >> username;

			std::string buy_string1 = "Purchase - ";

			std::string buy_string2 = "Event title: ";
			//event_title
			std::string buy_string3 = "Buyer: ";
			//username
			std::string buy_string4 = "Number of tickets: ";
			//ticket_number_b

			appendLineToFile(string transFile, string buy_string1);
			appendLineToFile(string transFile, string buy_string2);
			appendLineToFile(string transFile, string event_title);
			appendLineToFile(string transFile, string buy_string3);
			appendLineToFile(string transFile, string username);
			appendLineToFile(string transFile, string buy_string4);
			appendLineToFile(string transFile, int ticket_number_b);
			//save_to_trans_file();
		}
		int sell() {
			printf("Enter event name:");
			std::cin >> event_title;
			printf("Enter sale price:");
			std::cin >> price;
			printf("Enter number of tickets:");
			std::cin >> ticket_number_s;

			std::string sell_string1 = "Selling - ";
			std::string sell_string2 = "Event title: ";
			//event_title
			std::string sell_string3 = "Sale Price: ";
			//price
			std::string sell_string4 = "Number of tickets: ";
			//ticket_number_s

			appendLineToFile(string transFile, string sell_string1);
			appendLineToFile(string transFile, string sell_string2);
			appendLineToFile(string transFile, string event_title);
			appendLineToFile(string transFile, string sell_string3);
			appendLineToFile(string transFile, float price);
			appendLineToFile(string transFile, string sell_string4);
			appendLineToFile(string transFile, int ticket_number_s);
			//save_to_trans_file();
		}
	}

	string setUserType(string type) {
		//given type
		usertype = type;
	}
};

static void appendLineToFile(string filepath, string line)
{
	std::ofstream file;
	
	file.open(filepath, std::ios::out | std::ios::app);
	if (file.fail())
		throw std::ios_base::failure(std::strerror(errno));

	//make sure write fails with exception if something is wrong
	file.exceptions(file.exceptions() | std::ios::failbit | std::ifstream::badbit);

	file << line << std::endl;
}

//the admin sub class
class Admin : public FullStandardUser {
	string username;
	string username_b;
	string username_s;
	float balance;
	string usertype;

public:
	int create_account() {
		FullStandardUser::setName();
		FullStandardUser::setUserType();
		
		std::string create_string1 = "Account Created for user:";
		//username
		std::string create_string2 = "With User Type:";
		//usertype

		appendLineToFile(string transFile, string create_string1);
		appendLineToFile(string transFile, string username);
		appendLineToFile(string transFile, string create_string2);
		appendLineToFile(string transFile, string usertype);
		
		//save_to_trans_file();
	}
	int delete_account() {
		int i = 0;
		printf("Enter username");
		string temp;
		cin >> temp;
		deleted = new string[5];
		deleted[i] = temp;
		i++;

		std::string delete_string1 = "Account deleted for user:";
		//temp
		appendLineToFile(string transFile, string delete_string1);
		appendLineToFile(string transFile, string temp)
		//save_to_trans_file();
	}
	int refund(float amount) {
		printf("Enter buyer's username:");
		std::cin >> username_b;
		printf("Enter seller's username:");
		std::cin >> username_s;
		printf("Enter credit amount:");
		std::cin >> amount;

		std::string refund_string1 = "Refund requested - ";
		std::string refund_string2 = "Buyer: ";
		//username_b
		std::string refund_string3 = "Seller: ";
		//username_s
		std::string refund_string4 = "Amount of credit transfered: ";
		//amount
		appendLineToFile(string transFile, string refund_string1);
		appendLineToFile(string transFile, string refund_string2);
		appendLineToFile(string transFile, string username_b);
		appendLineToFile(string transFile, string refund_string3);		appendLineToFile(string transFile, string username)
		appendLineToFile(string transFile, string username_s);
		appendLineToFile(string transFile, string refund_string4);		appendLineToFile(string transFile, string username)
		appendLineToFile(string transFile, float amount);
			
			//save_to_trans_file();
	}
	float addCredit() {
		printf("Enter target account's username:");
		std::cin >> username;
		printf("Amount of credit wishing to add");
		float amount;
		cin >> amount;
		balance += amount;

		std::string addCredit_string1 = "addCredit requested - ";
		
		std::string addCredit_string2 = "Amount of credit to add: ";
		//amount
		std::string addCredit_string3 = "Buyer: ";
		//username

		appendLineToFile(string transFile, string addCredit_string1);
		appendLineToFile(string transFile, string addCredit_string2);
		appendLineToFile(string transFile, float amount);
		appendLineToFile(string transFile, string addCredit_string3);
		appendLineToFile(string transFile, string username);


		//save_to_trans_file();	
	}
	
};

class FileHandler {
	float num;
	string report;
	string name;
	string transaction_file;
	int numberTickets;
	float ticketPrice;
	
	public:
	void read_account_file(string fileName) {
		ifstream myReadFile;
		string line;
		int lineCount = 0;
		myReadFile.open(fileName);
		if (myReadFile.is_open()) {
			while (getline(myReadFile, line)) {
				lineCount++;
				//name stores the name from account file
			}
			accounts = new string[lineCount];
			for (int x = 0; x < lineCount; x++) {
				myReadFile >> accounts[x];
			}
		}
	}
	int read_ticket_file(int num) {
		string FileName;
		ifstream myReadFile;
		myReadFile.open(FileName);
		if (myReadFile.is_open()) {
			while (!myReadFile.eof()) {
				myReadFile >> num;
				//num stores the number from ticket file
			}
		}
	}
	string write_trans_file(string data) {
		ofstream myfile;
		std::string transFile = "transaction_file.txt";
		myfile.open(transFile);
		myfile << data;
		myfile.close();
		return 0;
		//outputs that transaction data to a file
	}
	string save_to_transaction_file(string report) {
		ofstream outfile;
		outfile.open("transaction_file.txt", ios_base::app);
		outfile << report;
		return 0;
		//append data to transaction file
	}
};
//the class that handles user related actions

class EventHandler {
	int currentUser = -1;
	FullStandardUser user;
	Admin adminUser;
public:
	EventHandler(){
		return;
	}
	string login() {
		//to do: login properly
		if (currentUser != -1) {
			printf("error: already logged in\n");
			return "";
		}
		string temp;
		printf("enter username:");
		std::cin >> temp;
		for (int x = 15 - temp.length(); x > 0; x--) {
			temp = temp + " ";
		}
		//search for username in accounts
		for (int i = 0; i < sizeof(accounts); i++) {
			if (temp.compare(0, 14, accounts[i])) {
				printf("logged in as ", temp + "\n");
				if (temp == "admin") {
					adminUer.setName(temp);
					adminUser.setUserType(accounts[i].substr(16, 17));
					float temp1 = stof(accounts[i].substr(19, 27));
					adminUser.setBalance(temp1);
				}
				else {
					user.setName(temp);
					user.setUserType(accounts[i].substr(16, 17));
					float temp1 = stof(accounts[i].substr(19, 27));
					user.setBalance(temp1);
					currentUser = 1;
					return temp;
				}
			}
		}
		//no name found, ask if they want to use admin
		printf("Account name not found, would you like to login as admin? (Y/N)");
		string loginTemp;
		if (loginTemp == "Y") {
			printf("logged in as admin\n");
			adminUser.setName("admin          ");
			adminUser.setUserType("AA");
			currentUser = 1;
			return "admin";
		}
		else {
			//not logging in as admin means no new account can be made. exit program
			printf("Please try logging in again");
			login();
		}
	}
	int logout() {
		//logs out
		if (currentUser == -1) {
			printf("error: not logged in\n");
			return -1;
		}
		else {
			currentUser = -1;
			printf("bye\n");
		}
		return 0;
	}
	bool is_account_valid() {
		if (user.getUserType() == "AA" || user.getUserType() == "FS" || user.getUserType() == "BS" || user.getUserType() == "SS") {
			//checks if account already has type, therefore created
			return true;
		}
	}
	bool is_amount_valid(float amount) {
		//999,999 is amount limit
		if (amount <= 1000000) {
			return true;
		}
	}
	bool is_disabled() {
		//in process of being deleted corresponding to certain time period allowed (assumption)
		for (int i = 0; i<sizeof(deleted); i++)
			if (user.getName() == deleted[i]) {
				return true;
			}
			else {
				return false;
			}
	}

	void parseInput(string input) {
		//parses input, and redirects to a member function
		if (input == "buy") {runTransaction(1);}
		else if (input == "sell") { runTransaction(2); }
		else if (input == "addcredit") { runTransaction(3); }
		else if (input == "refund") { runTransaction(4); }
		else if (input == "create") { runTransaction(5); }
		else if (input == "delete") { runTransaction(6); }
		
	}
	void runTransaction(int trans) {
		switch (trans) {
		case 1:
			user.buy();
			break;
		case 2:
			user.sell();
			break;
		case 3:
			user.addCredit();
			break;
		case 4:
			printf("how much is being refuned?");
			float amount;
			cin >> amount;
			adminUser.refund(amount);
			break;
		case 5:
			adminUser.create_account();
			break;
		case 6:
			adminUser.delete_account();
			break;
		}
	}
};


int main() {
	//to do: thorough testing
	FileHandler openfile = {};
	EventHandler session = {};
	openfile.read_account_file("user_accounts.txt");
	
	string input;
	printf("welcome to the ticket seller system\n");
	printf("please login\n");
	if (session.login() == "admin") {
		printf("Please choose a transaction from:\n",
			"buy\n",
			"sell\n",
			"addcredit\n",
			"refund\n",
			"create\n",
			"delete\n");
		getline(cin, input);
		session.parseInput(input);
	}
	else {
		printf("Please choose a transaction from:\n",
			"buy\n",
			"sell\n",
			"addcredit\n");
		getline(cin, input);
		session.parseInput(input);
	}
	return 0;
}