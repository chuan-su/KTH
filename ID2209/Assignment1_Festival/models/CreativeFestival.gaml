/***
* Name: CreativeFestival
* Author: chuan
* Description: 
* Tags: Tag1, Tag2, TagN
***/

model CreativeFestival
global {
	float worldDimension <- 0.5 #km;
	geometry shape <- square(worldDimension);
	
	int numberOfGuests <- 10;
	int numberOfWaitresses <- 2;
	int numberOfFoodStores <- 2;
	int numberOfDrinkStores <- 2;
	int numberOfInformationCenters <- 1;
	
	list<point> drinkStoreLocationsRAW <- [{50,100},{100,400}];
	list<point> foodStoreLocationsRAW <- [{300,450}, {400,60}];
	int storeIndexA <- 0;
	int storeIndexB <- 0;
	int waitressStoreIndex <-0;
	init {
		create FestivalGuest number: numberOfGuests; //apparently they distribute themselves evenly
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
			location <- {(worldDimension - 50), (worldDimension/2)};
			//storeLocations <- storeLocations;
		}
		create Waitress number: numberOfWaitresses
		{
			storeLocation <- drinkStoreLocationsRAW at 0;
			drinksToSell <- 10;
			storeLocation <- drinkStoreLocationsRAW at waitressStoreIndex;
			waitressStoreIndex <- waitressStoreIndex +1;
			location <- {storeLocation.x + 20, storeLocation.y};
		}
	}
	reflex globalPrint
	{
		write "Step of simulation: " + time;
	}
}

/* Festival Guest - moving around
 *  Actions {beIdle, moveToTarget, enterStore
 */
species FestivalGuest skills: [moving] {
	float size <- 10.0;
	float speed <- 20#km/#h;
	rgb color <- #black;
	//float energy <- (rnd(100));
	float food_transfer <- -1.0;
	float drink_transfer <- -4.0;
	int thirst;
	int hunger;
	
	init {
		thirst <- rnd(200, 300, 10);
		hunger <- rnd(300, 400, 10);
	}
	
	InformationCenter infoCenter <-nil;	
	
	point drinkStoreLocation <- nil;
	point foodStoreLocation <- nil;
	
	Waitress waitress <- nil;
	
	aspect base{
		draw circle(size) color: color border: #black;
	}
	
	reflex beIdle when: thirst > 40 and hunger > 60 {
		do goto target:{ rnd(worldDimension), rnd(worldDimension) };
		do wander;	
	}
	
	reflex search_infoCenter when:infoCenter = nil {
		ask InformationCenter {
			myself.infoCenter <- self;
		}
	}
	reflex goto_infoCenter when:infoCenter != nil and (drinkStoreLocation = nil or foodStoreLocation = nil) and (thirst < 40 or hunger < 60) and waitress = nil{
		do goto target:infoCenter;	
	}
	
	reflex askInfoCenter when: infoCenter != nil and location distance_to(infoCenter) = 0 {
		ask infoCenter {
			if (myself.drinkStoreLocation = nil) {
				if myself.thirst < 40 {
					int rndIndex <- rnd(1);
					myself.drinkStoreLocation <- self.drinkStoreLocations at rndIndex;
				}	
			}
			if (myself.foodStoreLocation = nil) {
				if myself.hunger < 60 {
					int rndIndex <- rnd(1);
					myself.foodStoreLocation <- self.foodStoreLocations at rndIndex;
				}	
			}
		}
	}
	
	reflex goto_drinkstore when: drinkStoreLocation != nil and (thirst < 40) and (waitress = nil or (location distance_to(drinkStoreLocation) < location distance_to(Waitress closest_to(self)))) {
	  do goto target:drinkStoreLocation;
	}
	
	reflex goto_foodstore when: foodStoreLocation != nil and (hunger < 60) {
	  do goto target:foodStoreLocation;
	}
	
	reflex enter_drinkstore when: drinkStoreLocation != nil and location distance_to(drinkStoreLocation) = 0 {
		ask DrinkStore at_distance (0) {
			myself.thirst <- rnd(200, 300, 10);
		}
		drinkStoreLocation <- nil;
		//self.waitress <- nil;
	}
	reflex enter_foodstore when: foodStoreLocation != nil and location distance_to(foodStoreLocation) = 0 {
		ask FoodStore at_distance (0) {
			myself.hunger <- rnd(400, 400, 10);
		}
		foodStoreLocation <- nil;
	}
	
	reflex search_waitress when: (thirst < 40) and (waitress = nil) and (drinkStoreLocation = nil or (location distance_to(drinkStoreLocation) > location distance_to(Waitress closest_to(self)))){
		Waitress awaitress <- Waitress closest_to(self); 
		 ask awaitress {
		 	if self.guest = nil {
		 		myself.waitress <- self;
		   		self.guest <- myself;	
		 	} else if self.guest.thirst >= 40 {
		 		myself.waitress <- self;
		 		self.guest.waitress <- nil;
		 		self.guest <- myself;
		 	}
		 }	
	}
	
	reflex follow when: (thirst < 40) and (waitress != nil) {
		do goto target: waitress;
	}
	
	
	reflex buy when: (thirst < 40) and (waitress != nil) and (location distance_to(waitress) < 5){
		ask waitress {
			if self.drinksToSell > 0 {
				self.drinksToSell <- (self.drinksToSell -1);
				myself.thirst <- rnd(200, 300, 10);
			}
			myself.waitress <- nil;
			self.guest <- nil;
		}
	}
	
	reflex consumeThirstEnergy when: (drinkStoreLocation = nil or location distance_to(drinkStoreLocation) != 0) and waitress = nil {
		thirst <- (thirst - 1);	
	}
	
	reflex consumeHungerEnergy when: (foodStoreLocation = nil or location distance_to(foodStoreLocation) != 0) and waitress = nil {
		hunger <- (hunger - 1);	
	}
}

species Waitress skills: [moving] {
	float size <- 10.0;
	rgb color <- #pink;
	
	int drinksToSell;
	point storeLocation;
	FestivalGuest guest <- nil;
	
	aspect base {
		draw circle(size) color: color border: #black; 
	}
	
	reflex goto_store when: drinksToSell = 0 {
		do goto target:storeLocation speed: 20#km/#h;
	}
	
	reflex fillDrinks when: drinksToSell = 0 and location distance_to(storeLocation) = 0 {
		ask DrinkStore at_distance(0) {
			myself.drinksToSell <- 10;
		}
	}
	
	reflex sell when: drinksToSell > 0 and guest = nil {
		do goto target:{ rnd(worldDimension), rnd(worldDimension) };
		do wander amplitude: 180;
	}
	
	reflex serve when: (drinksToSell > 0) and (guest != nil) {
		do goto target: guest speed: 20#km/#h;
	}
}

species Store {
	rgb color;	
	string attribute;
	
	aspect base {
		draw triangle(30) color: color border: #black; 
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
	
	aspect base {
		draw square(100) color: color border: #black;
	}

}

experiment main type: gui {
	output {
		display map type: opengl {
			species FestivalGuest aspect: base;
			species FoodStore aspect: base;
			species DrinkStore aspect: base;
			species InformationCenter aspect: base;
			species Waitress aspect: base;
		}
	}
	
}
/* Insert your model definition here */

