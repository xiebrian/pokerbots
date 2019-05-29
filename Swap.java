import java.util.*;

public class Swap {

	//returns the index of the card that will be swapped, -1 if neither card will be swapped
	public static int swapAlg(Card[] board, Card c1, Card c2){
		boolean flag1 = true; // c1 will be swapped if flag1 == true, not swapped otherwise
		boolean flag2 = true; // """"

		//hole cards that are pairs or pair community cards will not be swapped
		for (int i = 0; i < board.length; i++) {
			if (board[i].value == c1.value) { flag1 = false; }
			if (board[i].value == c2.value) { flag2 = false; }
			if (c1.value == c2.value) { 
				flag1 = false;
				flag2 = false;
			}
		}

		int[] values = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}; //stores number of instances of each value
		int[] suits = {0, 0, 0, 0}; //{'s', 'h', 'd', 'c'}
		initialize(board, c1, c2, values, suits);

		//flushes or flush draws:
		for (int i = 0; i < suits.length; i++) {
			if (suits[i] >= 4) {
				if (c1.suit == i) { flag1 = false; }
				if (c2.suit == i) { flag2 = false; }
			}
		}

		//straights or straight draws:
		int c1_power = 0; //tracks # of draws associated with each card
		int c2_power = 0; //only treats card as an actual drawing card if power == 2
		if (!threeOfSuit(board)) {
			ArrayList<Integer> draw_outs = new ArrayList<Integer>();
			for (int i = 12; i >= 3; i--) { //since we count down, we ensure only the highest straights are considered
				int count = 0; 
				for (int j = 0; j < 5; j++) {
					if (values[(i - j + 13) % 13] > 0) { count++; }
				}
				if (count == 5) { //straight exists
					for (int j = 0; j < 5; j++) {
						if ((i - j + 13) % 13 == c1.value) {flag1 = false;}
						if ((i - j + 13) % 13 == c2.value) {flag2 = false;}
					}
				}
				if (count == 4) { //straight draw exists
					int missing_val = -1;
					for (int j = 0; j < 5; j++) { //find the missing value or card that completes a straight
						int rank = (i - j + 13) % 13;
						if (values[rank] == 0) {
							missing_val = rank;
						}
					}
					if (!draw_outs.contains(missing_val)) { //only check if that out has not been checked yet
						draw_outs.add(missing_val);
						for (int j = 0; j < 5; j++) {
							int rank = (i - j + 13) % 13;
							if (values[rank] > 0) {
								if (rank == c1.value) { c1_power++; }
								if (rank == c2.value) { c2_power++; }
							}
						}
					}
				}
			}
			if (c1_power == 2) { flag1 = false; }	
			if (c2_power == 2) { flag2 = false; }
		}
		return determineSwap(flag1, flag2, c1, c2, c1_power, c2_power);
	}
	

	//goes through all the cards and initializes value and suits arrays
	private static void initialize(Card[] board, Card c1, Card c2, int[] values, int[] suits) {
		for (Card card : board) {
			values[card.value] ++;
			suits[card.suit] ++;
		}
		values[c1.value] ++;
		suits[c1.suit] ++;
		values[c2.value] ++;
		suits[c2.suit] ++;
	}

	//returns true if there are three of a suit on board, false otherwise
	private static boolean threeOfSuit(Card[] board) {
		int[] suits = {0, 0, 0, 0};
		for (Card card : board) {
			suits[card.suit] ++;
		}
		for (int i = 0; i < suits.length; i++) {
			if (suits[i] >= 3) { return true; }
		}
		return false;
	}

	//determines which card should be swapped
	private static int determineSwap(boolean flag1, boolean flag2, Card c1, Card c2, int c1_power, int c2_power) {
		if (flag1 && flag2) { //both cards are swappable; swap lower card, or card not associated with draws
			if (c1_power > c2_power) { return 1; }
			else if (c2_power > c1_power) { return 2; }
			else if (c1.value < c2.value) { return 1; }
			else return 2;
		}
		if (flag1 && (!flag2)) {return 1;} //swap first card
		if ((!flag1) && (flag2)) {return 2;} //swap second card
		return -1; //neither card should be swapped
	}

	
}
