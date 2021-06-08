/**
* Name: Festival
* Authors: cardel
* Description: A model for the festival assignment
* Tags: Tag1, Tag2, TagN
*/

model Festival

global {
	float worldDimension <- 50 #m;
	geometry shape <- square(worldDimension);
	
	int numberOfGuests <- 10;
	int numberOfFoodStores <- 2;
	int numberOfDrinkStores <- 2;
	int numberOfInformationCenters <- 1;
	
	list<point> drinkStoreLocationsRAW <- [{25,20},{10,40}];
	list<point> foodStoreLocationsRAW <- [{30,10}, {40,10}];
	int storeIndexA <- 0;
	int storeIndexB <- 0;
	
	float total_distance <- 0.0;
	init {
		create FestivalGuest number: numberOfGuests; //apparently they distribute themselves
		create FestivalGuard number: 1;
		create FoodStore number: numberOfFoodStores
		{
			location <- foodStoreLocationsRAW  at storeIndexA;
			storeIndexA <- storeIndexA + 1;
		}
		create DrinkStore number: numberOfDrinkStores
		{
			location <- drinkStoreLocationsRAW at storeIndexB;
			storeIndexB <- storeIndexB + 1;
		}
		create InformationCenter number: numberOfInformationCenters
		{
			location <- {(worldDimension - 50#m), (worldDimension/2)};
			//storeLocations <- storeLocations;
		}
	}
	reflex globalPrint
	{
		//write "Step of simulation: " + time;
	}
}

species FestivalGuest skills: [moving] {
	float size <- 1#m;
	float speed <- 2#km/#h;
	rgb color <- #black;
	float food_transfer <- -1.0;
	float drink_transfer <- -4.0;
	int thirst;
	int hunger;
	map<string, point> memory <- [];
	bool bad_apple;
	
	init {
		thirst <- rnd(50, 300, 10);
		hunger <- rnd(30, 400, 10);
		bad_apple <- flip(0.5);
	}
	
	InformationCenter infoCenter <-nil;	
	point store <- nil;
	
	aspect base{
		if(bad_apple = true){
			color <-#red;
		}
		draw geometry:sphere(size) color: color border: #black;
	}
	
	reflex beIdle when: infoCenter = nil and store = nil{
		do wander;			
	}
	
	
	reflex remember when: memory != [] and (thirst < 5 or hunger < 5) and infoCenter = nil and store = nil{
		bool re <- flip(0.5);
		point food_loc <- memory['food'];
		point drink_loc <- memory['drink'];
		
		if(re = true and thirst <= hunger and drink_loc != nil){
			write "remembering!!!";
			store <- memory["drink"];
			color <- #blue;			
		}
		else if(re = true and !(thirst <= hunger) and food_loc != nil){
			write "remembering!!!";
			store <- memory["food"];
			color <- #yellow;
		}
		else{
			write "do not remember";
			ask InformationCenter {
				myself.infoCenter <- self;
			}
			if(thirst < 5){
				color <- #blue;
			}
			if(hunger < 5){
				color <- #yellow;
			}
		}
	}

	reflex goto_infoCenter when:infoCenter != nil and store = nil {
		do goto target:infoCenter;
		total_distance <- total_distance+1;
	}
	
	reflex askInfoCenter when: infoCenter != nil and location distance_to(infoCenter) = 0 {
		ask infoCenter {
			if myself.store = nil {
				if myself.thirst < 40 {
					int rndIndex <- rnd(1);
					myself.store <- self.drinkStoreLocations at rndIndex;
				}
				else if myself.hunger < 60 {
					int rndIndex <- rnd(1);
					myself.store <- self.foodStoreLocations at rndIndex;
				}
			}
		}
	}
	
	reflex goto_store when:store != nil {
	  do goto target:store;
	  total_distance <- total_distance+1;
	}
	
	reflex enterStore when: store != nil and location distance_to(store) = 0 {
		ask DrinkStore at_distance (0) {
			myself.thirst <- rnd(200, 2000, 10);
			add "drink"::myself.store to: myself.memory;
		}
		
		ask FoodStore at_distance (0) {
			myself.hunger <- rnd(400, 4000, 10);
			add "food"::myself.store to: myself.memory;
		}
		store <- nil;
		infoCenter <- nil;
		color <- #black;
		//write(memory);
	}
	
	reflex consumeThirstEnergy when: store = nil or location distance_to(store) != 0 {
		thirst <- (thirst - 1);	
	}
	
	reflex consumeHungerEnergy when: store = nil or location distance_to(store) != 0 {
		hunger <- (hunger - 1);	
	}
}

species FestivalGuard skills: [moving]{
	rgb color <- #pink;	
	float speed <- 4#km/#h;
	list<FestivalGuest> target_guest <-[];
	
	aspect base {
		draw cube(2#m) color: color border: #black; 
	}
	reflex findGuest when: target_guest != [] {
		do goto target:target_guest[0];
	}
	reflex removeGuest when: target_guest != [] and location distance_to(target_guest[0]) = 0{
		ask FestivalGuest at_distance(0){
			do die;
			myself.target_guest[0] <- nil;
			remove from:myself.target_guest index:0;
		}
	}
}

species Store {
	rgb color;	
	string attribute;
	
	aspect base {
		draw triangle(3#m) color: color border: #black; 
	}
}
species FoodStore parent:Store{
	init{
		color <- #yellow;
		attribute <- "food";
	}
}

species DrinkStore parent:Store{
	init{
		color <- #blue;	
		attribute <- "drink";
	}
}
species InformationCenter {
	rgb color <- #green;
	list<point> drinkStoreLocations <- drinkStoreLocationsRAW;
	list<point> foodStoreLocations <- foodStoreLocationsRAW;
	
	list<FestivalGuest> bad_apple <-[];
	aspect base {
		draw cube(4#m) color: color border: #black;
	}
	
	reflex checkStatus when: bad_apple = []{
		ask agents of_species FestivalGuest at_distance(1){
			if(self.bad_apple = true){
				add self  to: myself.bad_apple;
			}
		} 
	}
	reflex callSecurity when: bad_apple != []{
		write "SECURITY";
		ask FestivalGuard {
			self.target_guest <- myself.bad_apple;
			myself.bad_apple <- nil;
		}
	}

}

experiment main type: gui {
	output {
		display map type: opengl {
			species FestivalGuest aspect: base;
			species FestivalGuard aspect: base;
			species FoodStore aspect: base;
			species DrinkStore aspect: base;
			species InformationCenter aspect: base;
		}
		display chart {
			chart "Festival guest information"{
				data "Distance traveled" value: total_distance;
			}
		}
	}
	
}
