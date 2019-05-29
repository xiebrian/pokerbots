import java.util.*;
import java.io.*;

public class SelfPlay {
	
	private static PlayerCFR button, big;
	private static int bigNet = 0;
	private static int buttonNet = 0;
	private static int chatter = 1; // 0 = no output except final net, 1 = output at the end of rounds
	// 2 = output all the time
	
	public static void main (String[] args) throws IOException{ 
		
		int N = 10;
		button = new PlayerCFR(true);
		big = new PlayerCFR(false);
		for(int it = 0; it < N; it++){
			// initialization
			button.refresh();
			big.refresh();
			
			ArrayList<Integer> cards = new ArrayList<Integer>();
			for (int i = 0; i < 13; i++) {
				drawCard(cards);
			}
			button.deal(new Card(cards.get(0)), new Card(cards.get(1)),
					new Card(cards.get(2)), new Card(cards.get(3)));
			big.deal(new Card(cards.get(4)), new Card(cards.get(5)),
					new Card(cards.get(6)), new Card(cards.get(7)));
			
			// post blinds
			ArrayList<PerformedAction> prev = new ArrayList<PerformedAction>();
			prev.add(new PerformedAction(7, 1, 1));
			prev.add(new PerformedAction(7, 2, 2));
						
			//pre flop
			Card[] board = new Card[0];
			boolean folded = playStreet(board, prev, true);
			printState(prev, board, 0);
			if(folded){
				boolean buttonWin = prev.get(prev.size() - 1).actor == 2;
				if(chatter >= 1){
					if(buttonWin)
						System.out.println("Big blind folds");
					else
						System.out.println("Button folds");
				}
				runFolded(buttonWin);
				continue;
			}
			
			// flop
			prev.add(new PerformedAction(6, -1, 0));
			board = flop(cards);
			// discard
			big.playDiscard(prev, board);
			button.playDiscard(prev, board);
			printState(prev, board, 1);
			// betting
			folded = playStreet(board, prev, false);
			printState(prev, board, 2);
			if(folded){
				boolean buttonWin = prev.get(prev.size() - 1).actor == 2;
				if(chatter >= 1){
					if(buttonWin)
						System.out.println("Big blind folds");
					else
						System.out.println("Button folds");
				}
				runFolded(buttonWin);
				continue;
			}
			
			// turn
			prev.add(new PerformedAction(6, -1, 1));
			board = turn(cards);
			// discard
			big.playDiscard(prev, board);
			button.playDiscard(prev, board);
			printState(prev, board, 3);
			// betting
			folded = playStreet(board, prev, false);
			printState(prev, board, 4);
			if(folded){
				boolean buttonWin = prev.get(prev.size() - 1).actor == 2;
				if(chatter >= 1){
					if(buttonWin)
						System.out.println("Big blind folds");
					else
						System.out.println("Button folds");
				}
				runFolded(buttonWin);
				continue;
			}
			
			// river
			prev.add(new PerformedAction(6, -1, 2));
			board = river(cards);
			folded = playStreet(board, prev, false);
			printState(prev, board, 5);
			if(folded){
				boolean buttonWin = prev.get(prev.size() - 1).actor == 2;
				if(chatter >= 1){
					if(buttonWin)
						System.out.println("Big blind folds");
					else
						System.out.println("Button folds");
				}
				runFolded(buttonWin);
				continue;
			}
			
			runFolded(showdown(board));
		}
		System.out.println("Overall:");
		System.out.println("Button has netted " + buttonNet);
		System.out.println("Big blind has netted " + bigNet);
		
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
		button.amount = 200;
		big.amount = 200;
		if(chatter >= 1)
			System.out.println("Players tie with " + Card.hand(bButton));
		return true;
	}
	
	private static void runFolded(boolean buttonWin){
		
		int val = 200 - button.amount;
		if(buttonWin){
			val += button.amount - big.amount;
		}
		endRound(buttonWin, val);
		
	}
	
	private static boolean playStreet(Card[] board, ArrayList<PerformedAction> prev, boolean preflop){
		boolean buttonAction = preflop;
		while (true){
			if(buttonAction){
				button.playStreet(prev, board);
			} else {
				big.playStreet(prev, board);
			}
			int lastAction = prev.get(prev.size() - 1).action;
			int amount = prev.get(prev.size() - 1).value;
			if(lastAction == 0){
				// bet
				buttonAction = !buttonAction;
			} else if(lastAction == 1){
				// call
				return false;
			} else if(lastAction == 2){
				// check
				if(prev.get(prev.size() - 2).action == 2){
					// both checked
					return false;
				}
				buttonAction = !buttonAction;
			} else if(lastAction == 3){
				// raise
				buttonAction = !buttonAction;
			} else if(lastAction == 4){
				// fold
				return true;
			}
		}

	}
	
	private static void endRound(boolean buttonWin, int amount){
		
		if(!buttonWin)
			amount *= -1;
		buttonNet += amount;
		bigNet -= amount;
		if(chatter >= 1){
			System.out.println("Button has netted " + buttonNet);
			System.out.println("Big blind has netted " + bigNet);
			System.out.println();
		}
		
	}
	
	private static int drawCard(ArrayList<Integer> cards) {
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
	
}
