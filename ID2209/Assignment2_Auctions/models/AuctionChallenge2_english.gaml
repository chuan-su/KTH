/***
* Name: Festival
* Author: chuan, cardell
* Description: 
* Tags: Tag1, Tag2, TagN
***/

model Auction

/* Insert your model definition here */

global {
	float worldDimension <- 50 #m;
	geometry shape <- square(worldDimension);
	
	int numberOfGuests <- 10;

	float total_distance <- 0.0;	
	list<Participant> participants;
	point salesRoomLocation <- {worldDimension / 2, worldDimension /2 }; 
	
	init {
		create SalesRoom returns: salesroom {
			location <- salesRoomLocation;
		} 
		create Auctioneer {
			salesRoom <- salesroom at 0;
			location <- {salesRoomLocation.x + 10, salesRoomLocation.y };
		}
		create Participant number: numberOfGuests returns: ps{
			location <- any_location_in(copy(shape) - envelope(salesroom));
			startingPoint <- location;
		}
		participants <- ps;
	}
	
	reflex globalPrint
	{
		write "Step of simulation: " + time;
	}
}

/* ENGLISH auction */
species Auctioneer skills: [fipa] {
	float size <- 1#m;
	rgb color <- #yellow;
	
	SalesRoom salesRoom;
	list<Participant> init;
	list<Participant> possibleBuyers <- []; // participants that are in the sales room to bid
	//list<Participant> refusedBuyers <- [];
	list<message> refusedMessages <- []; // refused messages on a selling price
	list<message> agreedMessages <- []; // agreed messages on a selling price
	//map<string, int> Bids <- []; // empty map for storing bids	
	bool finished <- false;
	int startingPrice <- 1500;
	int currentPrice <- startingPrice;
	
	aspect base{
		draw geometry:sphere(size) color: color;
	}
	reflex locaprint{
		write possibleBuyers;
	}
	// Inform Guests that auction is about to start
	reflex inform_auction_start when: (time = 1) {
		write 'inform auction start';
		write salesRoom.location;
		do start_conversation with: [to :: participants, protocol :: 'fipa-inform', performative :: 'inform', contents :: ['auction starts', salesRoom ]];
	}
	
	// Collect all the possible buyers which are those agreed on the "inform" message
	reflex read_agree_message when: !(empty(agrees)) {
		loop a over: agrees {
			write 'Actioneer recieves agree message with content: ' + string(a.contents) + 'from: ' + string(a.sender);
			add a.sender to: possibleBuyers;
		}
	}
	
	// Collect messages that participants aggreed on the selling price. used to determin whether to stop the auction or not.	
	reflex read_refuse_tobuy_message when: !(empty(refuses)) {
		loop r over: refuses {
			remove r.sender from: possibleBuyers;
			write 'Actioneer recieves refuse message from ' +  r.sender + ' with content: ' + string(r.contents);
			add r to: refusedMessages;
		}
	}
	
	// Collect messages that participants propose on the selling price. used to determine whether to continue with next selling price or not.
	reflex read_willing_tobuy_message when: !(empty(proposes)) {
		loop p over: proposes {
			message prop <- p;
			write 'proposal message from ' +  prop.sender + ' with content: ' + string(prop.contents);
			
			int proposal <- int(prop.contents at 1);
			if(proposal <= currentPrice){
				write '\t' + name + ' sends a reject_proposal message to ' + prop.sender;
				do reject_proposal with: [ message:: p, contents::['Not interested'] ];
			}
			else{
				add p to: agreedMessages;
				currentPrice <- proposal;
				write '\t' + name + ' sends a accept_proposal message to ' + prop.sender;
				do accept_proposal with: [ message:: p, contents::['Continue'] ];
			}
		}
	}
	
	// Start aution when all possible buyers reached the sales room.
	reflex start_auction when: startingPrice = currentPrice and (!(empty(possibleBuyers)) and ((possibleBuyers where (each.location = salesRoom.location)) contains_all (self.possibleBuyers))) {
		init <- copy(possibleBuyers);
		write name + ' sends a cfp message to all participants';
		do sendPrice;
	}
	
	// Continue auction if price not yet reaches the minimum and all participants refused the prices.
	reflex continue_auction when: startingPrice != currentPrice and (length(agreedMessages) > 0) and (length(possibleBuyers)>1) {
		write 'continue ' + currentPrice;
		do sendPrice;
	}

	// Terminates either if no one wants to propose and there is at least one past proposal, or noone has proposed anything.
	reflex stop_auction when: ((startingPrice != currentPrice) and (length(possibleBuyers) <= 1) and !finished) {
		write "***...........";
		if (length(possibleBuyers) <= 1) {
			write '*** stopped price is ' + agreedMessages max_of(each.contents at 1) + ' from buyer ' + first(possibleBuyers);
			//write '*** stopped price is ' + (auction.contents at 1) + ' from buyer ' + auction.sender;
		}
		else {
			write '*** no participant wants to buy. stop...';	
		}
		finished <- true;
	}
	
	action sendPrice {
		do start_conversation with: [ to :: list(possibleBuyers), protocol :: 'fipa-contract-net', performative :: 'cfp', contents :: ['Send bids', currentPrice] ];
	}
}

species Participant skills: [fipa, moving] parallel: 10{
	float size <- 1#m;
	float speed <- 2#km/#h;
	rgb color <- #black;
	point startingPoint;
	bool done <- false;
	
	int maxBid <- rnd(1000, 10000); // price that participant evaluates.
	
	SalesRoom salesRoom; // sales room to go
	message m; // auction invitation message.
	
	aspect base {
		draw geometry:sphere(size) color: color border: #black;
		draw cone3D(size, size+1) color: color at:{location.x, location.y, location.z-2};
	}
		
	reflex standby when: (empty(informs) and salesRoom = nil){
		color <- #black;
		do goto target: startingPoint;
	}
	
	reflex accept_auction_invitation when: (!empty(informs)) {
		write name + ' accepted auction invitation';
		self.m <- (informs at 0);
		self.salesRoom <- self.m.contents at 1;
		do agree with: [message :: m, contents :: ['YES']];
		color <- #grey;
	}
	
	
	reflex gotoSalesRoom when: (salesRoom != nil) {
		do goto target: salesRoom.location;
	}
		
	reflex reply_on_selling_price when: (!empty(cfps) and !done) {
		color <- #red;
		message cfp_message <- cfps at 0;
		write name + ' receives a cfp message from ' + cfp_message.sender + ' with content ' + cfp_message.contents;
		//write name + ' willing to max at ' + maxBid;
		int aPrice <- cfp_message.contents at 1;
		if aPrice > maxBid {
			//write name + ' refuses current price ' + cfp_message.contents;
			done <- true;
			do refuse with: [message :: cfp_message, contents :: ['Too Expensive!', aPrice]];
		} else {
			//write name + ' accepts' + cfp_message.contents;
			int newPrice <- aPrice + rnd(1000);
			do propose with: [message :: cfp_message, contents :: ['Buy!', newPrice]];
		}
	}
	
	reflex receive_reject_proposals when: !empty(reject_proposals) {
		message r <- reject_proposals at 0;
		write name + ' receives a reject_proposal message from with content ' + r.contents;
	}
	
	reflex receive_accept_proposals when: !empty(accept_proposals) {
		message a <- accept_proposals at 0;
		//write name + ' receives a accept_proposal message with content ' + a.contents;
		
		
	}
}

species SalesRoom {
	rgb color <- #blue;
	aspect base {
		draw square(10#m) color: color border: #black;
	}
}

experiment Auction type: gui {
	output {
		display map type: opengl {
			species SalesRoom aspect: base;
			species Auctioneer aspect: base;
			species Participant aspect: base;
		}
	}
}