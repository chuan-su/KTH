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

species Auctioneer skills: [fipa] {
	float size <- 1#m;
	rgb color <- #yellow;
	
	SalesRoom salesRoom;
	list<Participant> possibleBuyers <- []; // participants that are in the sales room to bid
	
	list<message> refusedMessages <- []; // refused messages on a selling price
	list<message> agreedMessages <- []; // agreed messages on a selling price
	
	list<int> aPrices <- [5000, 4000, 3000, 2000]; // reserverd selling price list, order: desc
	int pricelistCount <- length(aPrices); // used to determin if autioneer has run out prices or not.(also used to dertermin if auction has started or not)
	
	bool finished <- false;
	
	aspect base{
		draw geometry:sphere(size) color: color;
	}
	
	// Inform Guests that auction is about to start
	reflex inform_auction_start when: (time = 1) {
		write 'inform auction start';
		write salesRoom.location;
		do start_conversation with: [to :: participants, protocol :: 'fipa-inform', performative :: 'inform', contents :: ['auction starts', salesRoom ]];
	}
	
	// Collect all the possible buyers which are those agreed on the "inform" message
	reflex read_agree_message when: (length(aPrices) = pricelistCount) and !(empty(agrees)) {
		loop a over: agrees {
			write 'Actioneer recieves agree message with content: ' + string(a.contents) + 'from: ' + string(a.sender);
			//write a;
			add a.sender to: possibleBuyers;
		}
	}
	
   // Collect messages that participants aggreed on the selling price. used to determin whether to stop the auction or not.	
	reflex read_refuse_tobuy_message when: (length(aPrices) < pricelistCount) and !(empty(refuses)) {
		loop r over: refuses {
			write 'Actioneer recieves refuse message from ' +  r.sender + ' with content: ' + string(r.contents);
			add r to: refusedMessages;
		}
	}
	// Collect messages that participants refused on the selling price. used to determine whether to continue with next selling price or not.
	reflex read_willing_tobuy_message when: (length(aPrices) < pricelistCount) and !(empty(proposes)) {
		loop p over: proposes {
			write 'agree message from ' +  p.sender + ' with content: ' + string(p.contents);
			add p to: agreedMessages;
		}
	}
	
	// Start aution when all possible buyers reached the sales room.
	reflex start_auction when: !(empty(possibleBuyers)) and ((possibleBuyers where (each.location = salesRoom.location)) contains_all (self.possibleBuyers)) and (length(aPrices) = pricelistCount) {
		write name + ' sends a cfp message to all participants';
		do sendPrice;
	}
	
	// Continue auction if price not yet reaches the minimum and all participants refused the prices.
	reflex continue_auction when: length(aPrices) > 0 and (length(aPrices) < pricelistCount) and (length(refusedMessages) mod length(possibleBuyers) = 0) and (length(agreedMessages) = 0) {
		write 'continue ' + aPrices;
		do sendPrice;
	}
	
	// Terminates if the price went below threshold (last price in the pricelist) or any participants agrees the price.
	reflex stop_auction when: ((length(aPrices) = 0 and  (length(possibleBuyers) * pricelistCount = length(refusedMessages))) or (length(agreedMessages) > 0)) and !finished {
		write "***...........";
		if (length(agreedMessages) > 0) {
			message auction <- first(agreedMessages);
			write '*** stopped price is ' + (auction.contents at 1) + ' from buyer ' + auction.sender;
		} else {
			write '*** no participant wants to buy. stop...';	
		}
		finished <- true;
	}
	
	action sendPrice {
		int price <- first(aPrices);
		remove price from: aPrices;
		do start_conversation with: [ to :: list(possibleBuyers), protocol :: 'fipa-contract-net', performative :: 'cfp', contents :: ['Sell for price', price] ];
	}
}

species Participant skills: [fipa, moving] parallel: 10{
	float size <- 1#m;
	float speed <- 2#km/#h;
	rgb color <- #black;
	point startingPoint;
	
	int priceToBuy <- rnd(1000, 3000); // price that participant can afford.
	
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
		
	reflex reply_on_selling_price when: (!empty(cfps)) {
		color <- #red;
		message cfp_message <- cfps at 0;
		write name + ' receives a cfp message from ' + cfp_message.sender + ' with content ' + cfp_message.contents;
		write name + ' willing to buy for ' + priceToBuy;
		int aPrice <- cfp_message.contents at 1;
		if aPrice <= priceToBuy {
			do propose with: [message :: cfp_message, contents :: ['Buy!', aPrice, priceToBuy]];
		} else {
			do refuse with: [message :: cfp_message, contents :: ['Too Expensive!', aPrice, priceToBuy]];
		}
		
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