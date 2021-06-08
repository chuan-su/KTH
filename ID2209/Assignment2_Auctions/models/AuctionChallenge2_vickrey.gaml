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

/* VICKREY auction */
species Auctioneer skills: [fipa] {
	float size <- 1#m;
	rgb color <- #yellow;
	
	SalesRoom salesRoom;
	list<Participant> possibleBuyers <- []; // participants that are in the sales room to bid	
	map<string, int> Bids <- []; // empty map for storing bids	
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
	reflex read_agree_message when: !(empty(agrees)) {
		loop a over: agrees {
			write 'Actioneer recieves agree message with content: ' + string(a.contents) + 'from: ' + string(a.sender);
			add a.sender to: possibleBuyers;
		}
	}
	// Collect messages that participants refused on the selling price. used to determine whether to continue with next selling price or not.
	reflex read_willing_tobuy_message when: !(empty(proposes)) {
		loop p over: proposes {
			message prop <- p;
			string n <- prop.sender;
			write 'proposal message from ' +  n + ' with content: ' + string(prop.contents);
			add n::int(p.contents at 1) to: Bids;
		}
	}
	
	// Start aution when all possible buyers reached the sales room.
	reflex start_auction when: empty(Bids) and !(empty(possibleBuyers)) and ((possibleBuyers where (each.location = salesRoom.location)) contains_all (self.possibleBuyers)) {
		write name + ' sends a cfp message to all participants';
		do sendBegin;
	}

	// Terminates if the price went below threshold (last price in the pricelist) or any participants agrees the price.
	reflex stop_auction when: !empty(Bids) and !finished {
		write "***...........";
		if (length(Bids) > 0) {
			list<int> PriceDescending <- reverse(Bids sort(each));
			list<string> Participators <- Bids.keys sort_by Bids[each];
			write Bids;
			write '*** Winner is: ' + Participators at 0 + ' with price: ' + PriceDescending at 0 + '. Next highest price is: ' + PriceDescending at 1;
		} else {
			write '*** no participant wants to buy. stop...';	
		}
		finished <- true;
	}
	
	action sendBegin {
		do start_conversation with: [ to :: list(possibleBuyers), protocol :: 'fipa-contract-net', performative :: 'cfp', contents :: ['Send bids'] ];
	}
}

species Participant skills: [fipa, moving] parallel: 10{
	float size <- 1#m;
	float speed <- 2#km/#h;
	rgb color <- #black;
	point startingPoint;
	
	int priceBid <- rnd(1000, 3000); // price that participant evaluates.
	
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
		write name + ' willing to buy for ' + priceBid;
		do propose with: [message :: cfp_message, contents :: ['Buy!', priceBid]];
		
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