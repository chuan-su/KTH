/***
* Name: StageUtilityChallenge
* Author: chuan
* Description: 
* Tags: Tag1, Tag2, TagN
***/

model StageUtilityChallenge

/* Insert your model definition here */
global {
	float worldDimension <- 50 #m;
	geometry shape <- square(worldDimension);
	
	// act start time
	float start_t <- machine_time;
	
	int numberOfStages <- 4;
	int numberOfGuests <- 10;
	
	list<Stage> stages;
	list<Guest> guests;
	Lead lead;
	// locations for each stage
	list<point> stagesLocation <- [{worldDimension / 2, 0 },{worldDimension / 2 , worldDimension}, {0, worldDimension / 2}, {worldDimension, worldDimension /2 }];

	// some colors for each stage
	list<rgb> stageColor <- [#blue, #purple, #00A86B, #orange, #4169E1, #8A2BE2, #A0522D, #778899,#4B0082, #1E90FF, #008000, #DC143C, #191970];
	 
	int stageIndex <- 0;
	int guestIndex <- 0;
	init {
		create Stage number: numberOfStages returns: stgs{
			location <- stagesLocation[stageIndex];
			color <- stageColor[stageIndex];
			stageIndex <- stageIndex + 1;
			actStartsAt <- start_t; // they all start at the same time.
		}
		stages <- stgs;
		
		create Lead number: 1 returns: ld;
		lead <- ld[0];
		
		create Guest number: numberOfGuests returns: gs {
			preference <- ['band'::rnd(1,10) * 0.1, 'speaker'::rnd(1,10) * 0.1, 'lightshow'::rnd(1,10) * 0.1, 'background'::rnd(1,10) * 0.1, 'nationalFeature'::rnd(1,10) * 0.1, 'modernity'::rnd(1,10) * 0.1];
			if (guestIndex = 0) {
				crowdMass <- -3;
			} else {
				crowdMass <- 1;
			}
			lead <- ld[0];
			guestIndex <- guestIndex + 1;
		} 
		guests <- gs;	
	}
}

species Stage skills:[fipa] parallel: 4 {
	rgb color;
	
	// number of acts we have for each stage
	int numberOfActs <- 15;
	// start time for each act in a stage
	float actStartsAt;
	
	// act utilities, get changed for each act.
	map<string, float> utilities <- ['band':: rnd(1,10) * 0.1, 'speaker' :: rnd(1,10) * 0.1, 'lightshow' :: rnd(1,10) * 0.1, 'background' :: rnd(1,10) * 0.1, 'nationalFeature' :: rnd(1,10) * 0.1, 'modernity' :: rnd(1,10) * 0.1];
	
	aspect base {
		draw square(10#m) color: color border: color;
	}
	
	// Start next act, each act lasts for 5 seconds.
	reflex utilityUpdate when: numberOfActs > 0 and (machine_time - actStartsAt >= 5000) {
		// next act started! change some utilities settings
		utilities['band'] <- rnd(1, 10) * 0.1;
		utilities['speaker'] <- rnd(1, 10) * 0.1;
		utilities['lightshow'] <- rnd(1, 10) * 0.1;
		utilities['background'] <- rnd(1, 10) * 0.1;
		utilities['nationalFeature'] <- rnd(1, 10) * 0.1;
		utilities['modernity'] <- rnd(1, 10) * 0.1;
		
		// descrease the number of acts on this stage
		numberOfActs <- numberOfActs - 1;
		// set the start time for this act
		actStartsAt <- machine_time;
		
		if (numberOfActs <= 0) {
			// no acts left, close the stage.
			color <- #black;
			write name + ' all acts end. Stage closed';
		} else {
			// set some random color for next act
			color <- rgb(rnd(0,255),rnd(0,255),rnd(0,255));
			// inform guests we are starting a new act.
			do start_conversation with: [to :: guests, protocol :: 'fipa-inform', performative :: 'inform', contents :: ['act_starts', numberOfActs]];	
		}
	}
	
	// Answer the query from guests concerning act utilities.
	reflex reply_on_utility when: (!empty(queries)) {
		message queryFromGuest <- queries at 0;
		do agree with: [ message :: queryFromGuest, contents :: ['OK, I will answer you'] ];
		do inform with: [message :: queryFromGuest, contents :: ['utility', utilities]];
	}
}

species Lead skills: [fipa] parallel: 1 {
	rgb color <- #red;
	
	map<Stage, list<Guest>> crowdVotes <- [stages[0] :: [], stages[1]:: [], stages[2]:: [], stages[3] :: []];
	
	aspect base {
		draw geometry:sphere(1#m) color: color border: color;
	}
	
	reflex get_informed when: (!empty(informs)) {
		
		message m <- (informs at 0);
		
		string type <- m.contents at 0;
		
		do agree with: [message :: m, contents :: ['Got it']];
		
		loop s over: crowdVotes.keys {
			if (crowdVotes[s] contains m.sender) {
				remove m.sender from: crowdVotes[s]; 
			} 
		}
		
		Stage stage <- m.contents at 2;
		
		add m.sender to: crowdVotes[stage];
		
		if (length(crowdVotes[stages[0]]) + length(crowdVotes[stages[1]]) + length(crowdVotes[stages[2]]) + length(crowdVotes[stages[3]])) = numberOfGuests {
			do start_conversation with: [to :: guests, protocol :: 'fipa-inform', performative :: 'inform', contents :: ['stages', crowdVotes]];
			crowdVotes <- [stages[0] :: [], stages[1]:: [], stages[2]:: [], stages[3] :: []]; 		
		}
		
	}
}

species Guest skills: [fipa, moving] parallel: 10 {
	rgb color <- #black;
	// Run faster!
	float speed <- 10#km/#h;
	
	// my preferences for utilities
	map<string, float> preference;
	
	int crowdMass;
	Lead lead <- lead;
	
	// the stage I decided to go
	Stage toGo;
	// the max utilities among each stage.
	float max <- 0.0;
	
	// the number of act that I am watching at. 
	int myActNumber <- 0;
	bool voted <- false;
	
	aspect base {
		draw geometry:sphere(1#m) color: color border: color;
	}
	
	// get informed about the act is going to play, lets ask about utilities.
	reflex get_informed when: (!empty(informs)) {
		message m <- (informs at 0);
		string type <- m.contents at 0;
		if (type = 'act_starts') {
			// new act is going to play, we need to reset the calculated max utilities from previous act.
			int actNumber <- m.contents at 1;
			if (myActNumber != actNumber) {
				myActNumber <- actNumber;
				max <- 0.0;
				toGo <- nil;
			}
			
			// ok I will ask you
			do agree with: [message :: m, contents :: ['Ok, I will ask you about your utilities']];
			// tell me about your utitlites of the act
			do start_conversation with: [ to :: m.sender, protocol :: 'fipa-query', performative :: 'query', contents :: ['utilities?'] ];	
		} else if (type = 'utility') {
			// write name + 'utilities ' +  m.sender + ' with content: ' + string(m.contents);
			
			// The stage informed us of the act utilities
			map<string, float> utilities <- m.contents at 1;
			// calculate based on our preference
			float sum <- 0.0;
			loop u over: utilities.keys{
				sum <- sum + preference[u] * utilities[u];
			}
			// find the act/stage with the max utitlities according to our preference.
			if (sum > max) {
				max <- sum;
				toGo <- m.sender;
				//write name + " utilities mathes our preference best is " + max + " and the stage to go is " + toGo.name;
				voted <- false;
				do start_conversation with: [ to :: [lead], protocol :: 'fipa-inform', performative :: 'inform', contents :: ['stage', self, toGo] ];
			}
		} else if (type = 'stages') {
			// write 'lead message' + (m.contents);	
			
			do agree with: [message :: m, contents :: ['thanks for telling me']];
			
			map<Stage, list<Guest>> stgs <- m.contents at 1;
			
			int globalCrowdMassUtility <- 0;
			
			loop s over: stgs.keys {
				loop g over: stgs[s] {
					globalCrowdMassUtility <- globalCrowdMassUtility + g.crowdMass * length(stgs[s]);
				}
			}
				
			if (crowdMass < 0) {
				
				Stage min <- toGo;
				loop s over: stgs.keys {
					if (length(stgs[s]) < length(stgs[min])) {
						min <- s;
					}
				}
				
				if (toGo != min) {
					write name + " I wants go to place " + min + " instead of original decision " + toGo + " globalCrowdUitlity: " + globalCrowdMassUtility;
					int originalGlobalCrowdMassUtility <- globalCrowdMassUtility; 
					globalCrowdMassUtility <- 0;
					loop s over: stgs.keys {
						if (s = toGo) {
						 loop g over: stgs[s] {
						 	if (g != self) {
						 		globalCrowdMassUtility <- globalCrowdMassUtility + g.crowdMass * (length(stgs[s]) - 1);	
						 	}
						 }
						} else if (s = min) {
						  loop g over: stgs[s] {
							globalCrowdMassUtility <- globalCrowdMassUtility + g.crowdMass * (length(stgs[s]) + 1);
						  }
						  globalCrowdMassUtility <- globalCrowdMassUtility + crowdMass * (length(stgs[s]) + 1);
						} else {
						  loop g over: stgs[s] {
								globalCrowdMassUtility <- globalCrowdMassUtility + g.crowdMass * length(stgs[s]);
						  }	
						}
					}
					if (globalCrowdMassUtility < originalGlobalCrowdMassUtility ) {
						write name + " Ok I will sacrifice otherwise our globalUitlity becomes lower: " + globalCrowdMassUtility + "  compare to original: " + originalGlobalCrowdMassUtility;	
					} else {
						write name + " I will leave becuase now globalUitlity is much higher: " + globalCrowdMassUtility;
						toGo <- min;		
					}
				}
				do goto target: toGo;
			} else {
				list<Guest> companies <- stgs[toGo];
				if (length(companies) = 2) {
					Guest g;
					if (companies[0] = self) { g <- companies[1]; } else { g <- companies[0]; }
					if (g.crowdMass > 0) {
						Stage maxx <- toGo;
						loop s over: stgs.keys {
							if (length(stgs[s]) > length(stgs[maxx])) {
						  		maxx <- s;
							}
					  	}
					  	if (maxx != toGo) {
					  		write name + " OK I will leave you alone and go to" +  toGo  + " since " + g + " prefers " + g.toGo;
					  		write name + "Orginal global utility is " + globalCrowdMassUtility;
					  		loop s over: stgs.keys {
								if (s = toGo) {
						 			loop g over: stgs[s] {
						 				if (g != self) {
						 					globalCrowdMassUtility <- globalCrowdMassUtility + g.crowdMass * (length(stgs[s]) - 1);	
						 				}
						 			}
								} else if (s = maxx) {
						  			loop g over: stgs[s] {
										globalCrowdMassUtility <- globalCrowdMassUtility + g.crowdMass * (length(stgs[s]) + 1);
						  			}
						  			globalCrowdMassUtility <- globalCrowdMassUtility + crowdMass * (length(stgs[s]) + 1);
								} else {
						  			loop g over: stgs[s] {
										globalCrowdMassUtility <- globalCrowdMassUtility + g.crowdMass * length(stgs[s]);
						  			}	
								}
							}
							write name + " Now globalUitlity becomes " + globalCrowdMassUtility; 		
					 		toGo <- maxx;	
						}  		
				  }				
				}	
			}
		} 
		else {
			do refuse with: [message :: m, contents :: ['dont know what is this..']];
		}
	}
	
	// head off to the stage.
	reflex moveToStage when : toGo != nil {		
		do goto target: toGo;
	}
}

experiment StageUtilityChallenge type: gui {
	output {
		display map type: opengl {
			species Stage aspect: base;
			species Guest aspect: base;
			species Lead aspect: base;
		}
	}
}

