/**
* Name: NQueens
* Author: cardell
* Description: 
* Tags: Tag1, Tag2, TagN
*/
model NQueens_Creative
global {
	int N; //Size of chess board
	geometry shape <- cube(N) ;
	
	list<list< list<chessCube> > > chessBoard;
	
	init {
	//Create N^3 cubes
		list<list< list<chessCube> > > x_axis;
		loop i from:0 to:N-1{
			list<list<chessCube> > y_axis;
			loop j from:0 to: N-1{
				list<chessCube> z_axis;
				loop k from:0 to:N-1{
				  create chessCube returns: c{
				  	x_index <-i mod N;
				  	y_index <- j mod N;
				  	z_index <- k mod N;
					location <-{x_index, y_index, z_index};
					
				  }
				  add c[0] to: z_axis;
				}
				add z_axis to: y_axis;
		    }
		    add y_axis to: x_axis;
		}
		chessBoard <- x_axis;
		
		create queen number: 1 returns: firstQueen{
			myCell <- chessBoard[0][0][0];
			myCell.taken <- true;
			myCell.color <- #red;
			prev <- self;
			iteration <- 0;
		}
	}
	
	reflex globalPrint {
		write "Step of simulation: " + time;
	}
}

species chessCube{
	rgb color <- #white;
	point location;
	int x_index;
	int y_index;
	int z_index;
	bool taken <- false;
	aspect default {
		draw cube(1) color:color border:#black at:location;
	}
}

species queen skills:[fipa] {
	int iteration;
	float end_iteration;
	message get_pos_message;
	bool nextPosLegal <- false;
	
	float size <- 0.1;
	chessCube myCell <- nil;
	queen prev <- nil;
	queen next <- nil;
	bool sent <- false;
	rgb color <- #grey;
	aspect base {
		draw geometry:sphere(size) color: color at:{location.x, location.y, location.z};
	}
	init{
		end_iteration<- N^2;
	}
	//Helper function
	int min(int x, int y) {
		return x > y ? x : y;
	}
	//FIPA related actions ______________________________________________________________________
	// Inform previous queen about yourself
	reflex inform_new_queen when: (myCell = nil and sent != true and prev != self) {
		sent <- true;
		write 'inform: queen number: ' + iteration + ' asks to confirm prev-neighbour';
		do start_conversation with: [to :: [prev], protocol :: 'fipa-inform', performative :: 'inform', contents :: ['new queen', iteration ]];
	}
	
	// Collect the agreed neighbour which are those agreed on the "inform" message
	reflex read_agree_tobe_neighbour when: !(empty(agrees)) {
		loop a over: agrees {
			write 'Queen ' + iteration + ' recieves agree message with content: ' + string(a.contents) + 'from: ' + string(a.sender);
			prev <- a.sender;
			do get_position;
		}
	}
	
   // Collect refused inform messages
	reflex read_refuse_tobe_neighbour when: !(empty(refuses)) {
		loop r over: refuses {
			write 'Queen ' + iteration + ' recieves refuse message with content: ' + string(r.contents) + 'from: ' + string(r.sender);
			write ' Cannot proceed without valid neighbour';
			myCell.taken <- false;
			myCell.color <- #white;
			do die;
		}
	}
	
	//Receive inform message and accept next-neighbour invitation
	reflex accept_auction_invitation when: (!empty(informs)) {
		write '\t' + name + ' accepted neighbour invitation';
		message m <- informs at 0; 
		self.next <- m.sender;
		do agree with: [message :: m, contents :: ['YES']];
	}
	
	//Send cfp message to get position from prev-neigbour
	action get_position{
		if(prev != self){
			point sendPos <- nil;
			if(myCell != nil){
				sendPos <- {myCell.x_index, myCell. y_index, myCell.z_index};
				myCell.taken <- false;
				myCell.color <- #white;
				myCell <- nil;
			}
			do start_conversation with: [ to :: [prev], protocol :: 'fipa-contract-net', performative :: 'cfp', contents :: ['Give me cell position', sendPos] ];
		}
		else{
			myCell.taken <- false;
			myCell.color <- #white;
			if(myCell.z_index >= (N-1)){
				myCell <- chessBoard[myCell.x_index][myCell.y_index+1][0];
			}
			else if(myCell.y_index >= (N-1)){
				myCell <- chessBoard[myCell.x_index+1][0][0];
			}
			else{
				myCell <- chessBoard[myCell.x_index][myCell.y_index][myCell.z_index +1];
			}
			myCell.taken <- true;
			myCell.color <- #red;
			next <- nil;
		}
	}
	
	//Respond to cfp message from next-neighbour
	reflex reply_on_get_position when: (!empty(cfps)) {
		get_pos_message <- cfps at 0;
		nextPosLegal <- false;
		write '\t' + name + ' receives a cfp message from ' + get_pos_message.sender + ' with content ' + get_pos_message.contents;
		point nextCell <- get_pos_message.contents at 1;
		int startX;
		int startY;
		int startZ;
		if(nextCell = nil){
			if(myCell.y_index >= (N-1)){
				startX <- myCell.x_index +1;
				startY <- 0;
				startZ <- 0;
			}

			else{
				startX <- myCell.x_index;
				startY <- myCell.y_index+1;
				startZ <- 0;
				
			}
		}
		else if(nextCell.z >= (N-1)){
			startX <- N;
			startY <- N;
			startZ <- N;
		}
		else{
			startX <- nextCell.x;
			startY <- nextCell.y;
			startZ <- nextCell.z +1;
		}
		
		if(startX < (N) and startY < (N) and startZ < (N)){
			bool breaker <- false;
			int sendX;
			int sendY;
			int sendZ;
			loop z from: startZ to: (N-1){
				if(breaker = true){
					break;
				}
				if(legalCell(startX, startY, z) = true){
					sendX <- startX;
					sendY <- startY;
					sendZ <- z;
					breaker <- true;
				}
			}
			if(breaker = true){
				do propose with: [message :: get_pos_message, contents :: ['Position!', chessBoard[sendX][sendY][sendZ]]];
				get_pos_message <- nil;
			}
		
		}
		if(nextPosLegal = false){
			do refuse with: [message :: get_pos_message, contents :: ['No position!']];
			do get_position;
		}
	}
	
	// Collect messages that prev-neighbour propose on my position.
	reflex read_position_proposal when: !(empty(proposes)) {
		next <- nil;
		loop p over: proposes {
			message prop <- p;
			write 'Proposal message from ' +  prop.sender + ' with content: ' + string(prop.contents);
			myCell.taken <- false;
			myCell.color <- #white;
			myCell <- chessCube(prop.contents at 1);
			myCell.taken <- true;
			myCell.color <- #red;
			write string(myCell.x_index) + ' ' + string(myCell.y_index) + ' ' + string(myCell.z_index);
		}
	}
	
	//Non-FIPA related actions__________________________________________________________________
	reflex gotoMyCell when: (myCell != nil) {
		location <- myCell.location;
	}
	
	//Spawn the next queen
	reflex spawn_next_queen when: ( next = nil and myCell != nil and iteration < (end_iteration -1)) {
		create queen returns: q{
			iteration <- myself.iteration + 1;
			prev <- myself;
			location <- {-4,-4};
		}
		next <- q at 0;
	}

	bool legalCell(int x, int y, int z) {
		bool retVal <- true;
		int i <- 0;
		//1. ROWS
		if(retVal = true){
		//check for queens on the the rows: x, y, and z
			i <- 0;
			loop while: (i < N ){
				if( chessBoard[i][y][z].taken = true or chessBoard[x][i][z].taken = true or chessBoard[x][y][i].taken = true){
					retVal <- false;
					break;
				}
				i <- i +1;
			}
		}
		//2. DIAGONALS ON THE SAME PLANE
		if(retVal = true){
			//check for diagonals [XY] 
			i <- 0;
			loop while: ((x-i)>=0 and (y+i)<N ){
				if( chessBoard[x-i][y+i][z].taken = true ){
					retVal <- false;
					break;
				}
				i <- i + 1;
			}
			i <- 0;
			loop while: ((x-i)>=0 and (y-i)>=0 ){
				if( chessBoard[x-i][y-i][z].taken = true ){
					retVal <- false;
					break;
				}
				i <- i + 1;
			}
		}

		if(retVal = true){
			//check for diagonals [XZ] 
			i <- 0;
			loop while: ((x-i)>=0 and (z+i)<N ){
				if( chessBoard[x-i][y][z+i].taken = true ){
					retVal <- false;
					break;
				}
				i <- i + 1;
			}
			i <- 0;
			loop while: ((x-i)>=0 and (z-i)>=0 ){
				if( chessBoard[x-i][y][z-i].taken = true ){
					retVal <- false;
					break;
				}
				i <- i + 1;
			}
		}
		if(retVal = true){
			//check for diagonals [YZ] 
			i <- 0;
			loop while: ((y-i)>=0 and (z+i)<N ){
				if( chessBoard[x][y-i][z+i].taken = true ){
					retVal <- false;
					break;
				}
				i <- i + 1;
			}
			i <- 0;
			loop while: ((y-i)>=0 and (z-i)>=0 ){
				if( chessBoard[x][y-i][z-i].taken = true ){
					retVal <- false;
					break;
				}
				i <- i + 1;
			}
		}
		
		//3. DIAGONALS BETWEEN PLANES [XYZ]
		if(retVal = true){
			i <- 0;
			loop while: ((x+i)<N and (y+i)<N and (z+i)<N ){
				if( chessBoard[x+i][y+i][z+i].taken = true ){
					retVal <- false;
					break;
				}
				i <- i + 1;
			}
			i <- 0;
			loop while: ((x+i)<N and (y+i)<N and (z-i)>=0 ){
				if( chessBoard[x+i][y+i][z-i].taken = true ){
					retVal <- false;
					break;
				}
				i <- i + 1;
			}
			i <- 0;
			loop while: ((x+i)<N and (y-i)>=0 and (z+i)<N ){
				if( chessBoard[x+i][y-i][z+i].taken = true ){
					retVal <- false;
					break;
				}
				i <- i + 1;
			}
			i <- 0;
			loop while: ((x+i)<N and (y-i)>=0 and (z-i)>=0 ){
				if( chessBoard[x+i][y-i][z-i].taken = true ){
					retVal <- false;
					break;
				}
				i <- i + 1;
			}
		}
		
		if (retVal = true){
			nextPosLegal <- true;
		}
		return retVal;
	}
}
experiment nQUeens3D type: gui {
	parameter 'Size of the cube:' var: N init: 14;
	output {
		display View1 type:opengl draw_env:false{
			species chessCube transparency:0.5;
			species queen aspect: base;
			
		}
	}
}