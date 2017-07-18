//Team Second Place's ticket frontend
//Alexander Crawford - 100569102

#include <stdlib.h>
#include <stdio.h>
#include <iostream>
#include <string>

class Event;

class User {
	int id;
	std::string username;
	float balance;
	std::string usertype;
	Event* events;
	float getBalance(){
		//returns the user's balance
		return balance;
	}
	int credit(float amount){
		//adds credit to the user
		balance += amount;
		return 0;
	}
	int debit(float amount){
		//removes credit from the user
		if(amount > balance){
			//can't take money that isn't there
			return -1;
		} else {
			balance -= amount;
		}
		return 0;
	}
	bool canBuy(){
		//to do: check if the user can buy
		return true;
	}
	bool canSell(){
		//to do: check if the user can sell
		return true;
	}
	bool isAdmin(){
		//to do: check if the user is an admin
		return true;
	}
};

class Event {
	int id;
	std::string name;
	int numberTickets;
	float ticketPrice;
	int buyTickets(User buyer, int number){
		//to do: buy tickets
		return 0;
	}
};

class Session {
	int currentUserId;
	int login(){
		//to do: login properly
		printf("enter username:\n");
		std::string username = "admin";
		if(currentUserId != -1){
			printf("error: already logged in\n");
			return -1;
		} else {
			printf("logged in as %s\n", username.c_str());
			currentUserId = 1;
		}
		return 0;
	}
	int logout(){
		//logs out
		if(currentUserId == -1){
			printf("error: not logged in\n");
			return -1;
		} else {
			currentUserId = -1;
			printf("bye\n");
		}
		return 0;
	}
	int create_account(){
		//to do: create account
		return 0;
	}
	int delete_account(){
		//to do: delete account
		return 0;
	}
	int buy(){
		//to do: buy
		return 0;
	}
	int sell(){
		//to do: sell
		return 0;
	}
	int refund(){
		//to do: refund
		return 0;
	}
	int addcredit(){
		//to do: add credit
		return 0;
	}
	public:
	void parseInput(std::string input){
		//parses input, and redirects to a member function
		if(input == "login") login();
		else if (input == "logout") logout();
		else if (input == "create") create_account();
		else if (input == "delete") delete_account();
		else if (input == "buy") buy();
		else if (input == "sell") sell();
		else if (input == "refund") refund();
		else if (input == "addcredit") addcredit();
		else printf("Unknown command: %s\n",input);
	}
	Session(){
		currentUserId = -1;
	}
};

int main(){
	//to do: thorough testing
	std::string input;
	Session* s = new Session();
	printf("welcome to the ticket seller system");
	while(true){
		std::getline(std::cin,input);
		s->parseInput(input);
	}
	delete s;
	return 0;
}