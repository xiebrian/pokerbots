import java.io.*;
import java.util.*;

public class equity {
	
	public static final int NUM_HANDS = 169; //number of distinct possible starting hands
	public static final int NUM_CARDS = 7; //number of cards (hole + community) at showdown
	public static final int NUM_VALUES = 13; 
	public static final int NUM_SUITS = 4;
	public static final int BUTTON = 0;
	public static final int BIG = 1;
	public static final int STRAIGHT_FLUSH = 9, QUADS = 8, FULL = 7, FLUSH = 6,
							STRAIGHT = 5, TRIPS = 4, TWO_PAIR = 3, PAIR = 2, HIGH = 1;


	private static double[] wins = new double[NUM_HANDS]; //stores number of wins per starting hand
	private static int[] played = new int[NUM_HANDS]; //stores number of times each starting hand is played

	//----------------------------CARD CLASS--------------------------//
	
	static class Card {
		static char[] order = {'2', '3', '4', '5', '6', '7', '8', '9', 'T', 'J', 'Q', 'K', 'A'};
		static char[] suits = {'s', 'h', 'd', 'c'};
		int value;
		int suit;

		public Card(int n) {
			value = n / 4;
			suit = n % 4;
		}
		public Card(int v, int s) {
			value = v;
			suit = s;
		}
		public String toString() {
			return "" + order[value] + suits[suit];
		}
	}

	//----------------------------MAIN--------------------------------//

	public static void main(String[] args) throws IOException {
		Scanner sc = new Scanner(System.in);
		System.out.print("How many iterations? ");
		int n = sc.nextInt();
		for (int i = 0; i < NUM_HANDS; i++) {
			wins[i] = 0.0;
			played[i] = 0;
		}

		//play n random hands against each other
		for (int times = 0; times < n; times++) {

			//initializes the dealt cards beforehand for easy access
			ArrayList<Integer> cards = new ArrayList<Integer>();
			for (int i = 0; i < 13; i++) {
				drawCard(cards);
			}
			int count = 9; //indexing variable to make sure correct card is dealt with discards

			Card a1 = new Card(cards.get(0)); //player a's 1st hole card
			Card a2 = new Card(cards.get(1)); //player a's 2nd hole card
			Card b1 = new Card(cards.get(2)); //player b's 1st hole card
			Card b2 = new Card(cards.get(3)); //player b's 2nd hole card

			Card f1 = new Card(cards.get(4)); //flop card 1
			Card f2 = new Card(cards.get(5)); //flop card 2
			Card f3 = new Card(cards.get(6)); //flop card 3
			Card t = new Card(cards.get(7)); //turn card
			Card r = new Card(cards.get(8)); //river card

			//Flop play
			ArrayList<Card> onboard = new ArrayList<Card>(); //stores community cards
			onboard.add(f1);
			onboard.add(f2);
			onboard.add(f3);
			int swap1 = swap_alg(onboard, a1, a2); //player a's swap
			if (swap1 == 1) { //swap 1st card
				a1 = new Card(cards.get(count));
				count++;
			} 
			if (swap1 == 2) { //swap 2nd card
				a2 = new Card(cards.get(count));
				count++;
			}
			int swap2 = swap_alg(onboard, b1, b2); //player b's swap
			if (swap2 == 1) { //swap 1st card
				b1 = new Card(cards.get(count));
				count++;
			}
			if (swap2 == 2) { //swap 2nd card
				b2 = new Card(cards.get(count));
				count++;
			}

			//Turn play
			onboard.add(t);
			swap1 = swap_alg(onboard, a1, a2);
			if (swap1 == 1) {
				a1 = new Card(cards.get(count));
				count++;
			} 
			if (swap1 == 2) {
				a2 = new Card(cards.get(count));
				count++;
			}
			swap2 = swap_alg(onboard, b1, b2);
			if (swap2 == 1) {
				b1 = new Card(cards.get(count));
				count++;
			}
			if (swap2 == 2) {
				b2 = new Card(cards.get(count));
				count++;
			}

			//River play
			onboard.add(r);

			Card[] button = {f1, f2, f3, t, r, a1, a2};
			Card[] bigBlind = {f1, f2, f3, t, r, b1, b2};
			ArrayList<Integer> bHandButton = bestHand(button);
			ArrayList<Integer> bHandBigBlind = bestHand(bigBlind);
			int winner = determineWinner(bHandButton, bHandBigBlind);
			
			updateResults(cards, winner);
			if (times % 10000 == 0) System.out.println("Progress: " + (1.0 * times / n));
		}
		printResults();
	}

	//------------------------SWAP ALGORITHM----------------------------//

	//returns the index of the card that will be swapped, -1 if neither card will be swapped
	public static int swap_alg(ArrayList<Card> board, Card c1, Card c2){
		boolean flag1 = true; // c1 will be swapped if flag1 == true, not swapped otherwise
		boolean flag2 = true; // """"

		//hole cards that are pairs or pair community cards will not be swapped
		for (int i = 0; i < board.size(); i++) {
			if (board.get(i).value == c1.value) { flag1 = false; }
			if (board.get(i).value == c2.value) { flag2 = false; }
			if (c1.value == c2.value) { 
				flag1 = false;
				flag2 = false;
			}
		}

		int[] values = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}; //stores number of instances of each value
		int[] suits = {0, 0, 0, 0}; //{'s', 'h', 'd', 'c'}
		initialize(board, c1, c2, values, suits);

		//flushes or flush draws:
		for (int i = 0; i < NUM_SUITS; i++) {
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
			for (int i = NUM_VALUES - 1; i >= 3; i--) { //since we count down, we ensure only the highest straights are considered
				int count = 0; 
				for (int j = 0; j < 5; j++) {
					if (values[(i - j + NUM_VALUES) % NUM_VALUES] > 0) { count++; }
				}
				if (count == 5) { //straight exists
					for (int j = 0; j < 5; j++) {
						if ((i - j + NUM_VALUES) % NUM_VALUES == c1.value) {flag1 = false;}
						if ((i - j + NUM_VALUES) % NUM_VALUES == c2.value) {flag2 = false;}
					}
				}
				if (count == 4) { //straight draw exists
					int missing_val = -1;
					for (int j = 0; j < 5; j++) { //find the missing value or card that completes a straight
						int rank = (i - j + NUM_VALUES) % NUM_VALUES;
						if (values[rank] == 0) {
							missing_val = rank;
						}
					}
					if (!draw_outs.contains(missing_val)) { //only check if that out has not been checked yet
						draw_outs.add(missing_val);
						for (int j = 0; j < 5; j++) {
							int rank = (i - j + NUM_VALUES) % NUM_VALUES;
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
	private static void initialize(ArrayList<Card> board, Card c1, Card c2, int[] values, int[] suits) {
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
	private static boolean threeOfSuit(ArrayList<Card> board) {
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

	//-------------------------HELPER METHODS---------------------------//

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

	//determines whether a straight exists; if it does, returns highest card in straight
	public static int hasStraight(ArrayList<Integer> cards) {
		if (cards.size() < 5) return -1;
		if (cards.contains(12)) cards.add(-1);
		Collections.sort(cards);
		for (int i = cards.size() - 1; i >= 4; i--) {
			if (cards.get(i) == cards.get(i - 1) + 1 && cards.get(i - 1) == cards.get(i - 2) + 1 && cards.get(i - 2) == cards.get(i - 3) + 1 && cards.get(i - 3) == cards.get(i - 4) + 1) {
				int n = cards.get(i);
				if (cards.get(0) == -1) cards.remove(0);
				return n + 2;
			}
		}
		if (cards.get(0) == -1) cards.remove(0);
		return -1;
	}

	//returns a 5-card ArrayList of the best possible hand made from Card[] cards
	public static ArrayList<Integer> bestHand(Card[] cards) {
		ArrayList<Integer> hand = new ArrayList<Integer>();
		if (cards.length != NUM_CARDS) return hand;
		ArrayList<Integer> values = new ArrayList<Integer>();
		ArrayList<Integer> suits = new ArrayList<Integer>();
		for (int i = 0; i < NUM_CARDS; i++) {
			values.add(cards[i].value);
			suits.add(cards[i].suit);
		}
		int[] flush_suits = {0, 0, 0, 0};
		for (int i = 0; i < NUM_CARDS; i++) {
			flush_suits[suits.get(i)]++;
		}
		boolean isflush = false;
		int flushsuit = -1;
		for (int i = 0; i < NUM_SUITS; i++) {
			if (suits.get(i) >= 5) {
				isflush = true;
				flushsuit = i;
			}
		}
		if (isflush) {
			int i = 0;
			while (i < values.size()) {
				if (suits.get(i) != flushsuit) {
					suits.remove(i);
					values.remove(i);
				}
				else i++;
			}
			Collections.sort(values);
			int straight = hasStraight(values);
			if (straight >= 0) {
				hand.add(STRAIGHT_FLUSH);
				hand.add(straight);
				return hand;
			}
			else { 
				hand.add(FLUSH);
				int s = values.size() - 1;
				for (int j = 0; j < 5; j++) {
					hand.add(values.get(s) + 2);
					s--;
				}
				return hand;
			}
		}
		else {
			int i = hasStraight(values); //values is now sorted
			if (i >= 0) { 
				hand.add(STRAIGHT);
				hand.add(i);
				return hand;
			}
			else {
				ArrayList<Integer> repeats = new ArrayList<Integer>();
				ArrayList<Integer> repeatvalues = new ArrayList<Integer>();
				int pos = 0;
				int number = values.get(0);
				int times = 0;
				while (pos < 7) {
					if (values.get(pos) == number) times++;
					else {
						repeats.add(times);
						repeatvalues.add(number);
						number = values.get(pos);
						times = 1;
					}
					pos++;
				}
				if (times != 0) {
					repeats.add(times);
					repeatvalues.add(number);
				}

				int len = repeats.size();
				int trips = 0;
				int pairs = 0;

				for (int j = len - 1; j >= 0; j--) {
					if (repeats.get(j) == 4) {
						hand.add(QUADS);
						int quad = values.get(3);
						hand.add(quad + 2);
						int k = 6;
						while (values.get(k) == quad) k--;
						hand.add(values.get(k) + 2);
						return hand;
					}
				}
				for (int j = len - 1; j >= 0; j--) {
					if (repeats.get(j) == 3) trips++;
					if (repeats.get(j) == 2) pairs++;
				}
				if (trips == 2) { // full house
					hand.add(FULL);
					hand.add(values.get(5) + 2);
					hand.add(values.get(1) + 2);
					return hand;
				}
				else if (trips == 1 && pairs >= 1) { // full house
					hand.add(FULL);
					for (int j = len - 1; j >= 0; j--) {
						if (repeats.get(j) == 3) hand.add(repeatvalues.get(j) + 2);
					}
					for (int j = len - 1; j >= 0; j--) {
						if (repeats.get(j) == 2) {
							hand.add(repeatvalues.get(j) + 2);
							return hand;
						}
					}
				}
				else if (trips == 1) { // 3 of a kind
					hand.add(TRIPS);
					for (int j = len - 1; j >= 0; j--) {
						if (repeats.get(j) == 3) hand.add(repeatvalues.get(j) + 2);
					}
					int many = 0;
					for (int j = len - 1; j >= 0; j--) {
						if (repeats.get(j) == 1) {
							hand.add(repeatvalues.get(j) + 2);
							many++;
							if (many == 2) return hand;
						}
					}
				}
				else if (pairs >= 2) { // 2 pair
					hand.add(TWO_PAIR);
					int many = 0;
					ArrayList<Integer> counted = new ArrayList<Integer>();
					for (int j = len - 1; j >= 0; j--) {
						if (repeats.get(j) == 2) {
							counted.add(j);
							hand.add(repeatvalues.get(j) + 2);
							many++;
							if (many == 2) break;
						}
					}
					for (int j = len - 1; j >= 0; j--) {
						if (repeats.get(j) == 1 && !counted.contains(j)) {
							hand.add(repeatvalues.get(j) + 2);
							return hand;
						}
					}
				}
				else if (pairs == 1) { // pair
					hand.add(PAIR);
					for (int j = len - 1; j >= 0; j--) {
						if (repeats.get(j) == 2) hand.add(repeatvalues.get(j) + 2);
					}
					int many = 0;
					for (int j = len - 1; j >= 0; j--) {
						if (repeats.get(j) == 1) {
							hand.add(repeatvalues.get(j) + 2);
							many++;
							if (many == 3) return hand;
						}
					}
				}
				else { //high card
					hand.add(HIGH);
					int many = 0;
					for (int j = len - 1; j >= 0; j--) {
						hand.add(repeatvalues.get(j) + 2);
						many++;
						if (many == 5) return hand;
					}
				}

			}
		}
		return hand;
	}

	//-------------------METHODS FOR RESULTS/DISPLAY--------------------//

	//input: two sets of best hands (first int indicates type, following indicate cards)
	//output: int winner (0 for BU, 1 for BB)
	private static int determineWinner(ArrayList<Integer> bHandButton, ArrayList<Integer> bHandBigBlind) {
		if (bHandButton.get(0) > bHandBigBlind.get(0)) return BUTTON;
		else if (bHandButton.get(0) < bHandBigBlind.get(0)) return BIG;
		else {
			for (int j = 1; j < bHandButton.size(); j++) {
				if (bHandButton.get(j) > bHandBigBlind.get(j)) { return BUTTON; }
				else if (bHandButton.get(j) < bHandBigBlind.get(j)) { return BIG; }
			}
		}
		return -1;
	}

	private static void updateResults(ArrayList<Integer> cards, int winner) {
		int first = determineHandIndex(cards.get(0) / 4, cards.get(1) / 4, cards.get(0) % 4, cards.get(1) % 4);
		int second = determineHandIndex(cards.get(2) / 4, cards.get(3) / 4, cards.get(2) % 4, cards.get(3) % 4);

		played[first]++;
		played[second]++;
		if (winner == BUTTON) wins[first] += 1.0;
		else if (winner == BIG) wins[second] += 1.0;
		else {
			wins[first] += 0.5;
			wins[second] += 0.5;
		}
	}

	private static int determineHandIndex(int v1, int v2, int s1, int s2) {
		if (v1 == v2) return 14 * (12 - v1);  //pocket pair
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
			if (s1 == s2) return NUM_VALUES * (12 - larger) + (12 - smaller); //suited cards in top right
			else return NUM_VALUES * (12 - smaller) + (12 - larger); //unsuited cards in bottom left
		}
	}

	private static void printResults() {
		for (int i = 0; i < NUM_VALUES; i++) {
			for (int j = 0; j < NUM_VALUES; j++) {
				double dec = 1.0 * wins[NUM_VALUES * i + j] / played[NUM_VALUES * i + j];
				System.out.printf("%.5f" + "\t", dec);
			}
			System.out.println();
		}
	}
}