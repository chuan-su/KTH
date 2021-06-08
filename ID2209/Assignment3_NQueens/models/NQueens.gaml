/**
* Name: NQueens
* Author: cardell
* Description: 
* Tags: Tag1, Tag2, TagN
*/
model NQueens

global {
	int N; //Size of chess board
	init {
		create queen number: 1 returns: firstQueen{
			myCell <- chessBoard[0,0];
			prev <- self;
			iteration <- 0;
		}
	}
	
	reflex globalPrint
	{
		write "Step of simulation: " + time;
	}
}

species queen skills:[fipa] {
	int iteration;
	message get_pos_message;
	point nextPos <- nil;
	bool nextPosLegal <- false;
	
	float size <- 3.0;
	chessBoard myCell <- nil;
	queen prev <- nil;
	queen next <- nil;
	bool sent <- false;
	rgb color <- #grey;
	aspect base {
		draw geometry:sphere(size) color: color at:{location.x, location.y, location.z+5};
		draw geometry:cone3D(size, size+1) color: color at:{location.x, location.y, location.z};
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
			do start_conversation with: [ to :: [prev], protocol :: 'fipa-contract-net', performative :: 'cfp', contents :: ['Give me cell position', myCell] ];
		}
		else{
			myCell <- chessBoard[myCell.grid_x,myCell.grid_y +1];
			next <- nil;
		}
	}
	
	//Respond to cfp message from next-neighbour
	reflex reply_on_get_position when: (!empty(cfps)) {
		get_pos_message <- cfps at 0;
		nextPosLegal <- false;
		write '\t' + name + ' receives a cfp message from ' + get_pos_message.sender + ' with content ' + get_pos_message.contents;
		chessBoard nextCell <- get_pos_message.contents at 1;
		int startRow;
		int startCol;
		if(nextCell = nil){
			startRow <- 0;
			startCol <- myCell.grid_x +1;
		}
		else{
			startRow <- nextCell.grid_y + 1;
			startCol <- nextCell.grid_x;
		}
		
		if(startRow < (N)){
			loop k from: startRow to: (N-1){
				if(legalCell(startCol, k) = true){
					do propose with: [message :: get_pos_message, contents :: ['Position!', {startCol, k}]];
					get_pos_message <- nil;
					break;
				}	
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
			point newPos <- point(prop.contents at 1);
			myCell <- chessBoard grid_at newPos;
		}
	}
	
	//Non-FIPA related actions__________________________________________________________________
	reflex gotoMyCell when: (myCell != nil) {
		location <- myCell.location;
	}
	
	//Spawn the next queen
	reflex spawn_next_queen when: ( next = nil and myCell != nil and iteration < (N-1)) {
		create queen returns: q{
			iteration <- myself.iteration + 1;
			prev <- myself;
			location <- {-4,-4};
		}
		next <- q at 0;
	}

	bool legalCell(int col, int row) {
		bool retVal <- true;
		//check for queens on the same row, to the left
		loop i from:0 to: col{
			queen temp <- first(queen inside(chessBoard grid_at {i,row}));
			//chessBoard n <- chessBoard grid_at {i,row};
			//n.color <- #red;
			if( temp != nil ){
				retVal <- false;
				break;
			}
		}
		if(retVal = true){
			//check for upper diagonal queens to the left
			int tempCol <- col;
			int tempRow <- row;
			loop times: (row+1){
				queen temp <- first(queen inside(chessBoard grid_at {tempCol,tempRow}));
				//chessBoard n <- chessBoard grid_at {tempCol,tempRow};
				//n.color <- #yellow;
				if( temp != nil ){
					retVal <- false;
					break;
				}
				tempCol <- tempCol -1;
				tempRow <- tempRow -1;
			}
		}
		if(retVal = true){
			//check for lower diagonal queens to the left
			int tempCol <- col;
			int tempRow <- row;
			loop times: (col+1){
				queen temp <- first(queen inside(chessBoard grid_at {tempCol,tempRow}));
				//chessBoard n <- chessBoard grid_at {tempCol,tempRow};
				//n.color <- #pink;
				if( temp != nil ){
					retVal <- false;
					break;
				}
				tempCol <- tempCol -1;
				tempRow <- tempRow +1;
			}
		}
		if (retVal = true){
			nextPos <- {col, row};
			nextPosLegal <- true;
		}
		return retVal;
	}
}

//construct a N x N chess board
grid chessBoard width: N height: N {
	int type <- (grid_x + grid_y) mod 2;
	map<int,rgb> EvenOddColor <- [0::#white, 1:: #black];
	rgb color <- EvenOddColor[type];	
	
}

experiment NQueens type: gui {
	parameter 'Size of the grid:' var: N init: 10;
	
    output {
    	display map type: opengl {
			grid chessBoard lines: #black;
			species queen aspect: base;
        }

    }
}