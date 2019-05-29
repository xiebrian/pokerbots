import java.util.*;

public class PokerTrainer {
	
	private static Player button = new Player(true);
	private static Player big = new Player(false);
	
	public static int chatter = 0;
	public static final int PREFLOP = 0, FDISC = 1, FBET = 2, TDISC = 3, TBET = 4, RBET = 5;
	
	//--------------------------PRIMARY METHODS--------------------------//
	
	//Update weights (updates) times, iterate (iterations) times before updating
	private void train(int updates) {
		int iterations = 100000;
		for (int i = 0; i < updates; i++) {
			for (int j = 0; j < iterations; j++)
				playHand();
			button.updateWeights();
			big.updateWeights();
		}
	}
	
	private void playHand() {
		button.refresh();
		big.refresh();
		ArrayList<Integer> cards = new ArrayList<Integer>();
		for (int i = 0; i < 13; i++) {
			drawCard(cards);
		}
		button.deal(new Card(cards.get(0)), new Card(cards.get(1)), new Card(cards.get(2)), new Card(cards.get(3)));
		big.deal(new Card(cards.get(4)), new Card(cards.get(5)), new Card(cards.get(6)), new Card(cards.get(7)));
		
		//post blinds
		ArrayList<PerformedAction> prev = new ArrayList<PerformedAction>();
		prev.add(new PerformedAction(Player.POST, Player.BUTTON, 1));
		prev.add(new PerformedAction(Player.POST, Player.BIG, 2));
		
		//preflop betting
		Card[] board = new Card[0];
		boolean folded = playPreflop(board, prev);
		printState(prev, board, 0);
		if(folded){
			boolean buttonWin = prev.get(prev.size() - 1).actor == Player.BIG;
			if(chatter >= 1){
				if(buttonWin)
					System.out.println("Big blind folds");
				else
					System.out.println("Button folds");
			}
			runFolded(buttonWin);
			return;
		}
		
		//flop
		prev.add(new PerformedAction(Player.DEAL, Player.NONE, 0));
		board = flop(cards);
		//flop discard
		big.playDiscard(prev, board);
		button.playDiscard(prev, board);
		printState(prev, board, FDISC);
		//flop betting
		folded = playStreet(board, prev);
		printState(prev, board, FBET);
		if(folded){
			boolean buttonWin = prev.get(prev.size() - 1).actor == Player.BIG;
			if(chatter >= 1){
				if(buttonWin)
					System.out.println("Big blind folds");
				else
					System.out.println("Button folds");
			}
			runFolded(buttonWin);
			return;
		}
		
		//turn
		prev.add(new PerformedAction(Player.DEAL, Player.NONE, 1));
		board = turn(cards);
		//turn discard
		big.playDiscard(prev, board);
		button.playDiscard(prev, board);
		printState(prev, board, TDISC);
		//turn betting
		folded = playStreet(board, prev);
		printState(prev, board, TBET);
		if(folded){
			boolean buttonWin = prev.get(prev.size() - 1).actor == Player.BIG;
			if(chatter >= 1){
				if(buttonWin)
					System.out.println("Big blind folds");
				else
					System.out.println("Button folds");
			}
			runFolded(buttonWin);
			return;
		}
		
		//river
		prev.add(new PerformedAction(Player.DEAL, Player.NONE, 2));
		board = river(cards);
		//river betting
		folded = playStreet(board, prev);
		printState(prev, board, RBET);
		if(folded){
			boolean buttonWin = prev.get(prev.size() - 1).actor == Player.BIG;
			if(chatter >= 1){
				if(buttonWin)
					System.out.println("Big blind folds");
				else
					System.out.println("Button folds");
			}
			runFolded(buttonWin);
			return;
		}
		//showdown
		runFolded(showdown(board));
	}
	
	private static boolean playStreet(Card[] board, ArrayList<PerformedAction> prev){
		boolean buttonAction = false;
		while (true) {
			if(buttonAction)
				button.playStreet(prev, board);
			else 
				big.playStreet(prev, board);
			int lastAction = prev.get(prev.size() - 1).action;
			int amount = prev.get(prev.size() - 1).value;
			if(lastAction == Player.BET)
				buttonAction = !buttonAction;
			else if(lastAction == Player.CALL)
				return false;
			else if(lastAction == Player.CHECK) {
				if(prev.get(prev.size() - 2).action == Player.CHECK)
					// both checked
					return false;
				buttonAction = !buttonAction;
			} 
			else if(lastAction == Player.RAISE)
				buttonAction = !buttonAction;
			else if(lastAction == Player.FOLD){
				return true;
			}
		}
	}
	
	private static boolean playPreflop(Card[] board, ArrayList<PerformedAction> prev) {
		boolean buttonAction = true;
		int currentLv = 1;
		while (true) {
			if (buttonAction) 
				button.playPreflop(prev, currentLv); 
			else
				big.playPreflop(prev, currentLv);
			int lastAction = prev.get(prev.size() - 1).action;
			int prevAction = prev.get(prev.size() - 2).action;
			
			if(lastAction == Player.BET || lastAction == Player.RAISE) {
				buttonAction = !buttonAction;
				currentLv++;
			}
			else if(lastAction == Player.CALL) {
				if (prevAction != Player.POST) //button calls
					return false;
				buttonAction = !buttonAction;
			}
			else if(lastAction == Player.CHECK) {
				if(prevAction == Player.CALL) //limped pot
					return false;
				buttonAction = !buttonAction;
			} 
			else if(lastAction == Player.FOLD){
				return true;
			}
		}
	}
	
	//--------------------------HELPER METHODS--------------------------//
	
	//Converts a 2-card preflop hand into its approriate index
	public static int handToIndex(Card hole1, Card hole2) {
		int v1 = hole1.getValue();
		int s1 = hole1.getSuit();
		int v2 = hole2.getValue();
		int s2 = hole2.getSuit();
		if (v1 == v2) 
			return 14 * (12 - v1);  //pocket pair
		else {
			int larger = -1;
			int smaller = -1;
			if (v1 > v2) {
				larger = v1;
				smaller = v2;
			}
			else {
				larger = v2;
				smaller = v1;
			}
			if (s1 == s2) 
				return 13 * (12 - larger) + (12 - smaller); //suited cards in top right
			else 
				return 13 * (12 - smaller) + (12 - larger); //unsuited cards in bottom left
		}
	}
	
	//Draws a random card from the remaining undrawn cards, returns index of card
	public static int drawCard(ArrayList<Integer> cards) {
		boolean b = true;
		int i = -1;
		while (b) {
			i = (int) (Math.random() * 52);
			if (!cards.contains(i)) {
				cards.add(i);
				b = false;
			}
		}
		return i;
	}
	
	private static void printState(ArrayList<PerformedAction> prev, Card[] board, int street) {
		
		if(chatter == 0)
			return;
		else if(chatter == 1 && street != 5)
			return;
		switch(street){
		case 0:
			System.out.println("Pre-flop betting");
			break;
		case 1:
			System.out.println("Flop discards");
			break;
		case 2:
			System.out.println("Flop betting");
			break;
		case 3:
			System.out.println("Turn discards");
			break;
		case 4:
			System.out.println("Turn betting");
			break;
		case 5:
			System.out.println("River betting");
			break;
		}
		System.out.println(prev);
		System.out.println(button);
		System.out.println(big);
		System.out.println(Arrays.toString(board));
		System.out.println();
		
	}

	private static boolean showdown(Card[] board){
		
		Card[] buttonHand = new Card[5];
		Card[] blindHand = new Card[5];
		
		int bButton = Card.bestHand(button.hole1, button.hole2, board, buttonHand);
		int bBBlind = Card.bestHand(big.hole1, big.hole2, board, blindHand);

		if (bButton > bBBlind){
			if(chatter >= 1)
				System.out.println("Button wins with " + Card.hand(bButton) + " over " + Card.hand(bBBlind));
			return true;
		} else if (bButton < bBBlind){
			if(chatter >= 1)
				System.out.println("Big blind wins with " + Card.hand(bBBlind) + " over " + Card.hand(bButton));
			return false;
		} else {
//			for(int i = 0; i < buttonHand.length; i++){
//				if(buttonHand[i].value > blindHand[i].value){
//					winner = 0;
//					break;
//				} else if(buttonHand[i].value < blindHand[i].value) {
//					winner = 1;
//					break;
//				}
//			}
			int start = 0;
			switch(bButton/169){
			case 8:
				// straight flush to x
				start = 5;
				break;
			case 7:
				// quads of x, 1 kicker
				start = 4;
				break;
			case 6:
				// full house (3 x, 2 y)
				start = 5;
				break;
			case 5:
				// flush
				break;
			case 4:
				// straight to x
				start = 5;
				break;
			case 3:
				// triple x, 2 kickers
				start = 3;
				break;
			case 2:
				// 2 x, 2 y, 1 kicker
				start = 4;
				break;
			case 1:
				// 2 x, 3 kickers
				start = 2;
				break;
			case 0:
				start = 0;
				break;
			}
			for(int i = start; i < buttonHand.length; i++){
				if(buttonHand[i].value > blindHand[i].value){
					if(chatter >= 1)
						System.out.println("Button wins with " + Card.hand(bButton) + " tie, kicker " + buttonHand[i]);
					return true;
				} else if(buttonHand[i].value < blindHand[i].value) {
					if(chatter >= 1)
						System.out.println("Big blind wins with " + Card.hand(bBBlind) + " tie, kicker " + blindHand[i]);
					return false;
				}
			}
		}
		button.stack = 200;
		big.stack = 200;
		if(chatter >= 1)
			System.out.println("Players tie with " + Card.hand(bButton));
		return true;
	}
	
	private static void runFolded(boolean buttonWin){
		
		int val = 200 - button.stack;
		if(buttonWin){
			val += button.stack - big.stack;
		}
		endRound(buttonWin, val);
		
	}

	//ends round and accumulates profits
	private static void endRound(boolean buttonWin, int amount){
		if(!buttonWin)
			amount *= -1;
		button.netWin += amount;
		big.netWin -= amount;
		if(chatter >= 1){
			System.out.println("Button has netted " + button.netWin);
			System.out.println("Big blind has netted " + big.netWin);
			System.out.println();
		}
		button.accumulateProfits(amount, Player.lastLv);
		big.accumulateProfits(-amount, Player.lastLv);
	}
	
	private static Card[] flop(ArrayList<Integer> cards){
		Card[] board = new Card[3];
		board[0] = new Card(cards.get(8));
		board[1] = new Card(cards.get(9));
		board[2] = new Card(cards.get(10));
		return board;
	}
	
	private static Card[] turn(ArrayList<Integer> cards){
		Card[] board = new Card[4];
		board[0] = new Card(cards.get(8));
		board[1] = new Card(cards.get(9));
		board[2] = new Card(cards.get(10));
		board[3] = new Card(cards.get(11));
		return board;
	}
	
	private static Card[] river(ArrayList<Integer> cards){
		Card[] board = new Card[5];
		board[0] = new Card(cards.get(8));
		board[1] = new Card(cards.get(9));
		board[2] = new Card(cards.get(10));
		board[3] = new Card(cards.get(11));
		board[4] = new Card(cards.get(12));
		return board;
	}
	
	//-------------------------------MAIN-------------------------------//
	
	public static void main(String[] args) {
		PokerTrainer trainer = new PokerTrainer();
		int updates = 100;
		System.out.println("Training started");
		trainer.train(updates);
		for (int i = 0; i < 169; i++) {
			for (int j = 0; j < 6; j++) {
				System.out.printf("%.5f" + "\t", button.preflopTree[i][j]);
			}
			System.out.println();
		}
	}
}
