//Team Second Place's ticket frontend
//Alexander Crawford - 100569102

#include <stdlib.h>
#include <iostream>
#include <string>
#include <fstream>


//program intentions are strictly to outline all instances of a user and user action
//corresponding to that users possible capabilities as outlined in the prior instructions

class FileHandler {
	float num;
	std::string report;
	std::string name;
	std::string transaction_file;
	int numberTickets;
	float ticketPrice;

	std::string read_account_file(std::string name) {
		std::string FileName;
		std::ifstream myReadFile;
		myReadFile.open(FileName);
		char name[15];
		if (myReadFile.is_open()) {
			while (!myReadFile.eof()) {
				myReadFile >> name;
				//name stores the name from account file
			}
		}
	}
	int read_ticket_file(int num) {
		std::string FileName;
		std::ifstream myReadFile;
		myReadFile.open(FileName);
		int num;
		if (myReadFile.is_open()) {
			while (!myReadFile.eof()) {
				myReadFile >> num;
				//num stores the number from ticket file
			}
		}
	}
	std::string write_trans_file(std::string data) {
		std::ofstream myfile;
		myfile.open("transaction_file.txt");
		myfile << data;
		myfile.close();
		return 0;
		//outputs that transaction data to a file
	}
	std::string save_to_transaction_file(std::string report) {
		std::ofstream outfile;
		outfile.open("transaction_file.txt", std::ios_base::app);
		outfile << report;
		return 0;
		//append data to transaction file
	}
};
//the class that handles user related actions
class EventHandler {
	int currentUserId = -1;
	int login() {
		//to do: login properly
		printf("enter username:");
		std::string username = "admin";
		if (currentUserId != -1) {
			printf("error: already logged in\n");
			return -1;
		}
		else {
			printf("logged in as %s\n", username.c_str());
			currentUserId = 1;
		}
		return 0;
	}
	int logout() {
		//logs out
		if (currentUserId == -1) {
			printf("error: not logged in\n");
			return -1;
		}
		else {
			currentUserId = -1;
			printf("bye\n");
		}
		return 0;
	}
	bool isAdmin() {
		if (usertype = admin) {
			return true;
		}
	}
	bool is_account_valid() {
		if (getUserType() == 0 || usertype = buying || usertype = selling) {
			//checks if account already has type, therefore created
			return true;
		}
	}
	bool is_amount_valid() {
		//999,999 is amount limit
		if (amount <= 1000000) {
			return true;
		}
	}
	bool is_disabled() {
		//in process of being deletedd corresponding to certain time period allowed (assumption)
		for (int i = 0; i<sizeof(deleted); i++)
			if (username = deleted[i]) {
				return true;
			}
			else {
				return false;
			}
	}


	void parseInput(std::string input) {
		//parses input, and redirects to a member function
		switch (input) {
		case "login":
			login();
			break;
		case "logout":
			logout();
			break;
		case "create":
			create_account();
			break;
		case "delete":
			delete_account();
			break;
		case "buy":
			buy();
			break;
		case "sell":
			sell();
			break;
		case "refund":
			refund();
			break;
		case "addcredit":
			addcredit();
			break;
		}
	}
};


//user capable of buying and selling
class FullStandardUser {

	int id;
	std::string username;
	float balance;
	//admin = 0, buy = 1, sell = 2
	int usertype;
	Event* FileHandler, EventHandler;

	int getID() {
		//returns the user's balance
		return id;
	}
	std::string getName() {
		//returns the user's name
		return username;

	}
	float getBalance() {
		//returns the user's balance
		return balance;
	}
	int getUserType() {
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
		if (getUserType() != 2) {
			return true;
		}
	}

	bool canSell() {
		//user is capable of seslling
		if (getUserType() != 1) {
			return true;
		}
		else {
			return false;
		}
	}

	std::string sell() {
		if (canSell() == true) {

		}
		else {
			printf("This function is not available from your account!");
		}

	}

	bool isAdmin() {
		//user is an admin?
		if (getUserType() == 0) {
			return true;
		}
	}

	float addCredit() {
		printf("Amount of credit wishing to add");
		int amount;
		cin >> amount;
		balance += amount;
	}
	//save_to_trans_file();

	float setBalance() {
		//declare a certain balance
		printf("enter balance:");
		cin >> balance;
	}
	std::string setName() {
		//declare a certain name
		printf("enter name:");
		cin >> username;
	}

	int setUserType() {
		//set the usertype for the account
		printf("enter usertype; admin, buy, sell:");
		std::string temp;
		bool correct = false;
		while (!correct) {
			cin >> temp;
			if (temp == "admin") {
				usertype = 0;
				correct = true;
			}
			else if (temp == "buy") {
				usertype = 1;
				correct = true;
			}
			else if (temp == "sell") {
				usertype = 2;
				correct == true;
			}
			else {
				printf("Please only enter a proper usertype; admin, buy, sell");
			}
		}
	}
	int setID() {
		//declare a certain ID
		printf("enter ID:");
		cin >> id;
	}
};


//the admin sub class
class Admin : FullStandardUser {
	int accountId;
	std::string username;
	std::string username_b;
	std::string username_s;
	float balance;
	std::string usertype;

	int create_account() {
		printf("Enter username");
		cin >> username;
		printf("Enter Account Type");
		cin >> usertype;
		//save_to_trans_file();
	}
	int delete_account() {
		int i = 0;
		printf("Enter username");
		cin >> username;
		std::string deleted[];
		deleted[i] = username;
		i++;
		//save_to_trans_file();
	}
	int refund(float amount) {
		printf("Enter buyer's username:");
		cin >> username_b;
		printf("Enter seller's username:");
		cin >> username_s;
		printf("Enter credit amount:");
		cin >> amount;
		//save_to_trans_file();
	}
	float addCredit() {
		printf("Enter target account's username:");
		cin >> username;
		printf("Amount of credit wishing to add");
		cin >> amount;
		balance += amount;
		//save_to_trans_file();	
	}
	bool canBuy() {
		//capable of buying
		if (usertype != selling) {
			return true;
		}
	}
};


void main() {
	//to do: thorough testing
	std::string input;
	Session s;
	printf("welcome to the ticket seller system");
	while (true) {
		std::getline(std::cin, input);
		s.parseInput(input);
	}
	return 0;
}