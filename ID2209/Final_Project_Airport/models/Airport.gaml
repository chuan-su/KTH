/**
* Name: Airport
* Author: cardell and chuan
* Description: 
* Tags: Tag1, Tag2, TagN
*/
model Airport


global {
	float worldDimension <- 100 #m;
	geometry shape <- square(worldDimension);
	
	// act start time
	int numberOfGates <- 4;
	int numberOfPassengers <- 40;
	int numberOfCheckInReceptionists <- 4;
	int numberOfSecurityGuards <- 4;
	int numberOfWaitresses <- 4;
	
	list<Gate> gates;
	list<CheckInReceptionist> CheckInReceptionists;
	list<Passenger> passengers;
	list<SecurityGuard> securityGuards;
	SecurityLine securityLine;
	
	float gateDimension <- worldDimension / (numberOfGates + 1);
	float checkinPlaceDimension <- worldDimension / (numberOfCheckInReceptionists + 1); 
	 
	int gateIndex <- 0;
	int passengerIndex <- 0;
	int CheckInReceptionistIndex <- 0;
	int securityGuardIndex <- 0;
	int waitressIndex <- 0;
	
	//Broadcaser has time awareness
	//float step<-60000.0#ms; //--if we need to speed up simulation, every cycle = 60 cycles
	int minutes <- 0 update:  int(cycle * step);
	
	point barLocation <- {(worldDimension - 10#m), (worldDimension / 4)};
	
	init {
		create SecurityLine returns: sl {
			location <- point(0, worldDimension / 2);
		}
		
		create SecurityGuard number: numberOfSecurityGuards returns: sgs {
			color <- #black;
			securityGuardIndex <- securityGuardIndex + 1;
			
			origin <- {securityGuardIndex * gateDimension, worldDimension/2};
			location <- {securityGuardIndex * gateDimension, worldDimension/2};	
		}
		
		create Gate number: numberOfGates returns: gts {
			color <- rgb(rnd(0,255),rnd(0,255),rnd(0,255));
			gateIndex <- gateIndex + 1;
			location <- { gateIndex * gateDimension, 0 };
		}
		
		create Broadcaster returns: br{
			location <- {worldDimension,0,0};
		}
		
		create CheckInReceptionist number: numberOfCheckInReceptionists returns: rps {
			color <- #green;
			CheckInReceptionistIndex <- CheckInReceptionistIndex + 1;
			location <- { CheckInReceptionistIndex * checkinPlaceDimension, worldDimension };
			
			gates <- gts;
			securityGuards <- sgs;
			broadcaster <- br at 0;
		}
		
		create Bar returns: b {
			location <- barLocation;
		}
		
		create Waitress number: numberOfWaitresses {
			bar <- (b at 0);
			waitressIndex <- waitressIndex + 1;
			location <- {barLocation.x - waitressIndex * 8#m, barLocation.y - waitressIndex * 2#m };
		}
		
		gates <- gts;
		CheckInReceptionists <- rps;
		securityLine <- sl at 0;
	}
	//Every 4 hours a new batch of 40 passsengers are spawned
	reflex spawnPassengers when: (minutes mod 240 =0) {
		create Passenger number: numberOfPassengers returns: prs {
			color <- #blue;
			originColor <- #blue;
			location <- {rnd(0, worldDimension), rnd(worldDimension+8#m, worldDimension + 100#m, (worldDimension - 5#m)) };
			
			passengerIndex <- passengerIndex + 1;
			
			id <- passengerIndex;
			create Ticket returns: t;
			t[0].departure <- rnd(minutes +100, minutes +500);
			ticket <- t[0];
			checkIn <- CheckInReceptionists[rnd(0, numberOfCheckInReceptionists -1)]; // passenger knows where to check in.
			
		}
	}
	//Reflex to finish the simulation after 3 days
	reflex end when: (minutes >= 4320) {
		do pause;	
	}
}

species Ticket{
	int departure;
	Gate gate;
}

species Bar {
	rgb color <- #purple;	
	float size <- 4#m;
	
	aspect base {
		draw triangle(size) color: color border: #black; 
	}
}

species Waitress skills: [moving, fipa] {
	float size <- 1#m;
	rgb color <- #purple;
	
	Passenger passenger;
	Bar bar;
	
	reflex sell when: passenger = nil and location distance_to(bar) <= 10#m {
		do wander;
	}
	
	reflex stay when: passenger = nil and location distance_to(bar) > 10#m {
		do goto target: bar;
	}
	
	reflex gotoPassenger when: passenger != nil and location distance_to(passenger) > 1#m {
		do goto target: passenger speed: 10#km/#h;
	}
	
	reflex serve when: passenger != nil and location distance_to(passenger) <= 1#m {
		ask passenger {
			self.thirsty <- 0.0;
			myself.passenger <- nil;
		}
	} 
	
	reflex beingAskedForDrinks when: (!empty(queries)) {
		message query <- queries at 0;
		
		if (passenger = nil) {
		  passenger <- query.sender;
		  do agree with: [ message :: query, contents :: ['DRINK'] ];	
		} else {
		  do refuse with: [ message :: query, contents :: ['DRINK'] ];
		}
	}
	
	reflex clearPassenger when: passenger != nil and dead(passenger) {
		passenger <- nil;
	}
	aspect base {
		draw geometry:sphere(size) color: color border: color;
		draw cone3D(size, size+1) color: color at:{location.x, location.y, location.z-2};
	}
}

species SecurityLine {
	rgb color <- #red;
	float size <- 1#m;
	
	aspect base {
		draw square(size) color: color border: color at: point(0, worldDimension / 2);
		draw square(size) color: color border: color at: point(worldDimension, worldDimension / 2);
	}
}

species SecurityGuard skills:[moving, fipa] {
	float size <- 1#m;
	rgb color;
	point origin;
	
	Passenger stressedPassenger;
	
	// Security check. normal process
	reflex check when: (!empty(queries)) {
		message query <- queries at 0;
		if rnd (0,5) = 1 { // 20% chance to refuse
			do refuse with: [ message :: query, contents :: ['SECURITY_CHECK', {rnd(worldDimension), rnd(securityLine.location.y, worldDimension)}] ];	
		} else {
			do agree with: [ message :: query, contents :: ['SECURITY_CHECK'] ];
		}
	}
	
	// Some passenger tries to pass the secuirity line without security check.
	reflex catchStressedPassenger when: stressedPassenger != nil and location distance_to(stressedPassenger) > 0.5#m {
		do goto target: stressedPassenger speed: stressedPassenger.speed + 3#m;
	}
    
    // Warn passenget and let him stay away.
	reflex catchedStressedPassenger when: stressedPassenger != nil and location distance_to(stressedPassenger) <= 0.5#m {
		ask stressedPassenger {
			self.beingWarned <- true;
			self.targetBeingWarnedToGo <- {rnd(worldDimension), rnd(securityLine.location.y + securityLine.size + 10#m, worldDimension)}; // spot to go to calm down.
			myself.stressedPassenger <- nil;
		}
	}
	
	reflex backToSecurityLine when: stressedPassenger = nil and location distance_to(origin) != 0 {
		do goto target: origin;
	}
	
	aspect base {
		draw geometry:sphere(size) color: color border: color;
		draw cone3D(size, size+1) color: color at:{location.x, location.y, location.z-2};
	}
}

species Passenger skills:[moving, fipa] {
	float size <- 1#m;
	rgb color;
	rgb originColor;
	float stressUpperBound <- 10.0;
	
	int id;
	
	Gate gate;
	SecurityGuard securityGuard;
	CheckInReceptionist checkIn;
	Ticket ticket;
	
	int securityCheckStatus <- 0; // 0: not checked; 1: approved; -1: rejected;
	
	float thirsty <- 0.0;
	float stress <- 0.0;
	
	float delta <- 0.05;
	bool timeToBoard <- false;
	bool beingWarned <- false;
	point targetBeingWarnedToGo;
	point targetToGoAfterRejected;
	
	// Checkin process
	reflex goToCheckIn when: location distance_to(checkIn) > 2#m and gate = nil {
		do goto target: checkIn;	
	}
	// Checkin process
	reflex checkIn when: location distance_to(checkIn) <= 2#m and gate = nil {
		do start_conversation with: [ to :: [checkIn], protocol :: 'fipa-query', performative :: 'query', contents :: [self.id, ticket.departure] ];
	}
	// Checkin process and boarding process
	reflex getGateInform when: (!empty(informs)) {
		message m <- (informs at 0);
		string type <- m.contents at 0;
		if (type = 'CHECK_IN') {
			self.gate <- m.contents at 1;
			self.securityGuard <- m.contents at 2;
		}else if(type = 'BOARDING'){
			int boardTime <- m.contents at 1;
			if (boardTime = ticket.departure){
				timeToBoard <- true;	
			}
		}
	}
	
	// Security Check process. If not stressed then will go to security guard to check.
	reflex gotoSecurityLine when: securityCheckStatus = 0 and !beingWarned and stress   and securityGuard != nil and location distance_to(securityGuard) > 2#m {
		do goto target: securityGuard;
	}
	
	// Security Check process
	reflex securityCheck when: securityCheckStatus = 0 and stress <= stressUpperBound  and securityGuard != nil and location distance_to(securityGuard) <= 2#m {
		do start_conversation with: [ to :: [securityGuard], protocol :: 'fipa-query', performative :: 'query', contents :: [self.id] ];
	}
	// Security Check process success
	reflex securityCheckPassed when: (!empty(agrees)) {
		message m <- (agrees at 0);
		string type <- m.contents at 0;
		if (type = 'SECURITY_CHECK') {
			self.securityCheckStatus <- 1;
			self.stress <- 0.0;
		}
	}
	// Security Check process rejection
	reflex securityCheckRejected when:(!empty(refuses)) {
		message m <- (refuses at 0);
		string type <- m.contents at 0;
		if (type = 'SECURITY_CHECK') {
			self.targetToGoAfterRejected <- m.contents at 1;
			self.securityCheckStatus <- -1;
		}
	}
	
	// "Stress" behavior
	reflex stressIncreased when: securityCheckStatus = 0 and !beingWarned {
		stress <- stress + delta;
	}
	
    // stress increased more when rejected by security guard.
	reflex stressIncreasedMore when: securityCheckStatus = -1 and !beingWarned and stress <= stressUpperBound {
		stress <- stress + 2 * delta;
		do goto target: targetToGoAfterRejected;
	}
	
	// stressed and will try to go to gate without security check.
	reflex goToGateStressed when: stress > stressUpperBound  and securityCheckStatus != 1 and gate != nil and !beingWarned  {
		// Here we let the security guard know this passenger is trying to escape secuity check so that he can catch the stressed passenger.
		if(location distance_to(securityLine) < 2#m or location.y < securityLine.location.y) {
			ask SecurityGuard closest_to self {
				if (self.stressedPassenger = nil) {
					self.stressedPassenger <- myself;	
				}
			}	
		} 
		do goto target: gate;
	}

	// When arriving at given target after security rejection, just wander
	reflex wanderWhenRejected when: securityCheckStatus = -1 and !beingWarned and location distance_to(targetToGoAfterRejected) = 0 {
		do wander;
	}
	
	// Being warned by security guard. start calming down now. (decrease stress)
	reflex beingWarned when: beingWarned and targetBeingWarnedToGo != nil and location distance_to(targetBeingWarnedToGo) != 0 {
		do goto target: targetBeingWarnedToGo;
		stress <- stress - 2 * delta;  // calm down.
	}
	
	// Get to the spot where security guard asked to do. Now can redo security check. 
	reflex redoSecurityCheck when: beingWarned and targetBeingWarnedToGo != nil  and location distance_to(targetBeingWarnedToGo) = 0 {
		beingWarned  <- false;
		targetBeingWarnedToGo  <- nil;
		securityCheckStatus <- 0;
		self.color <- #blue;
	}
	
	// Boarding process
	// When all passenger has passed the security line and wait to be informed by broadcaster to board (timeToBoard = false).
	reflex waitingToBoard when: securityCheckStatus = 1 and !timeToBoard {
		thirsty <- thirsty + delta;
		if (location.y < gate.location.y - 10#m or location.y > securityGuard.location.y - 10#m) {
			do goto target: {rnd(0, worldDimension), rnd(securityGuard.location.y - 20#m - gate.location.y)};
		} else if (thirsty < 10) {
			do wander;
		} else {
			do start_conversation with: [ to :: [Waitress closest_to self], protocol :: 'fipa-query', performative :: 'query', contents :: ['drinks', self.id] ];
		}
	}
	
	//got the board command
	reflex Board when: timeToBoard{
		do goto target: gate;
	}
	reflex Fly when: timeToBoard and (location distance_to(gate) <= 1#m) and (minutes mod 200 = 0){
		do die;
	}
	
	reflex colorUpdate {
		if (stress > stressUpperBound ) {
			color <- #orange;	
		} else if (securityCheckStatus = -1 ){ // rejected by security guard.
			color <- #grey;
		} else {
			color <- originColor;
		}
	}
	
	aspect base {
		draw geometry:sphere(size) color: color border: #black;
		draw cone3D(size, size+1) color: color at:{location.x, location.y, location.z-2};
	}
}

species CheckInReceptionist skills:[fipa] {
	float size <- 1#m;
	rgb color;
	
	Broadcaster broadcaster;
	list<Gate> gates;
	list<SecurityGuard> securityGuards;
	
	reflex checkIn when: (!empty(queries)) {
		loop q over: queries{
			//message query <- queries at 0;
			do agree with: [ message :: q, contents :: ['CHECK_IN'] ];
			int passengerId <- q.contents at 0;
			int d <- q.contents at 1;
			
			Gate gate <- gates at (passengerId mod length(gates));
			SecurityGuard securityGuard <- securityGuards at (passengerId mod length(securityGuards));
				
			do inform with: [message :: q, contents :: ['CHECK_IN', gate, securityGuard]];
			do start_conversation with: [ to :: [broadcaster], protocol :: 'fipa-query', performative :: 'query', contents :: [d, q.sender] ];
		}
		
	}
	
	aspect base {
		draw geometry:sphere(size) color: color border: #black;
		draw cone3D(size, size+1) color: color at:{location.x, location.y, location.z-2};
	}
}

species Gate {
	float size <- 10#m;
	rgb color;
	
	aspect base {
		draw square(size) color: color border: color;
	}
}

species Broadcaster skills:[fipa] {
	list<Gate> gates;
		
	int start_days <- 0;
	int start_hours <- 8;
	int start_minutes <- 0;
	
	int curr_days <- 0 update: int(start_days + (curr_hours/24));
	int curr_hours <- 0 update: int(start_hours + (curr_minutes/60));
	int curr_minutes <- 0 update: int(start_minutes + minutes);
			
	list<int> timetable <- [];
	list<Passenger> checkedinCrowd; 
	
	reflex addToMemory when: (!empty(queries)) {
		message query <- queries at 0;
		do agree with: [ message :: query, contents :: ['WILL_BROADCAST'] ];
		int t <- query.contents at 0;
		Passenger p <- query.contents at 1;
		add t to: timetable;
		add p to: checkedinCrowd;
		write t;
		timetable <- timetable sort_by each;
	}
	
	reflex doNextBC when: length(checkedinCrowd) > 0 and first(timetable) <= minutes{
		int curr <- first(timetable);
		remove first(timetable) from: timetable;
		do start_conversation with: [ to :: checkedinCrowd, protocol :: 'fipa-query', performative :: 'inform', contents :: ['BOARDING', curr] ];
	}

	aspect base {
		draw "Day: " + string(curr_days) + ", " + string(curr_hours mod 24) + ":" + string(curr_minutes mod 60#s)
		 	color:# black font: font('Default ', 36, #bold);
		draw " Total minutes passed: (" + string(minutes) + ")" at: {location.x, location.y +5, 0}
			color:# black font: font('Default ', 36, #bold);
	}
}

experiment Airport type: gui {
	output {
		display map type: opengl {
			species Gate aspect: base;
			species Passenger aspect: base;
			species CheckInReceptionist aspect: base;
			species SecurityLine aspect: base;
			species SecurityGuard aspect: base;
			species Bar aspect: base;
			species Waitress aspect: base;
			species Broadcaster aspect: base;
		}
	}
}