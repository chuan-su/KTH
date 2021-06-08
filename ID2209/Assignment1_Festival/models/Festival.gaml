/**
* Name: Festival
* Authors: cardel, chuan
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
			location <- {(worldDimension - 50#m), (worldDimension/2)};
			//storeLocations <- storeLocations;
		}
	}
	reflex globalPrint
	{
		write "Step of simulation: " + time;
	}
}

species FestivalGuest skills: [moving] {
	float size <- 1#m;
	float speed <- 2#km/#h;
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
	point store <- nil;
	
	aspect base{
		draw geometry:sphere(size) color: color border: #black;
	}
	
	reflex beIdle when: infoCenter = nil{
		//do goto target:{ rnd(worldDimension), rnd(worldDimension) };
		do wander;	
		color <- #black;
	}
	
	reflex search_infoCenter when:infoCenter = nil and (thirst < 5 or hunger < 5){
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
		}
		
		ask FoodStore at_distance (0) {
			myself.hunger <- rnd(400, 4000, 10);
		}
		store <- nil;
		infoCenter <- nil;
	}
	
	reflex consumeThirstEnergy when: store = nil or location distance_to(store) != 0 {
		thirst <- (thirst - 1);	
	}
	
	reflex consumeHungerEnergy when: store = nil or location distance_to(store) != 0 {
		hunger <- (hunger - 1);	
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
	
	aspect base {
		draw cube(4#m) color: color border: #black;
	}

}

experiment main type: gui {
	output {
		display map type: opengl {
			species FestivalGuest aspect: base;
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
