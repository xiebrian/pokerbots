import java.io.*;
import java.util.*;

public class equity {

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


	public static ArrayList<Integer> bestHand(Card[] cards) {
		ArrayList<Integer> hand = new ArrayList<Integer>();
		if (cards.length != 7) return hand;
		ArrayList<Integer> values = new ArrayList<Integer>();
		ArrayList<Integer> suits = new ArrayList<Integer>();
		for (int i = 0; i < 7; i++) {
			values.add(cards[i].value);
			suits.add(cards[i].suit);
		}
		int[] flush_suits = {0, 0, 0, 0};
		for (int i = 0; i < 7; i++) {
			flush_suits[suits.get(i)]++;
		}
		boolean isflush = false;
		int flushsuit = -1;
		if (flush_suits[0] >= 5) {
			isflush = true;
			flushsuit = 0;
		}
		else if (flush_suits[1] >= 5) {
			isflush = true;
			flushsuit = 1;
		}
		else if (flush_suits[2] >= 5) {
			isflush = true;
			flushsuit = 2;
		}
		else if (flush_suits[3] >= 5) {
			isflush = true;
			flushsuit = 3;
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
				hand.add(9);
				hand.add(straight);
				return hand;
			}
			else {
				hand.add(6);
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
				hand.add(5);
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
						hand.add(8);
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
					hand.add(7);
					hand.add(values.get(5) + 2);
					hand.add(values.get(1) + 2);
					return hand;
				}
				else if (trips == 1 && pairs >= 1) { // full house
					hand.add(7);
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
					hand.add(4);
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
					hand.add(3);
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
					hand.add(2);
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
				else {
					hand.add(1);
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

	public static void main(String[] args) throws IOException {
		Scanner sc = new Scanner(System.in);
		System.out.print("How many iterations? ");
		int n = sc.nextInt();
		double[] wins = new double[169];
		int[] played = new int[169];
		for (int i = 0; i < 169; i++) {
			wins[i] = 0.0;
			played[i] = 0;
		}

		for (int times = 0; times < n; times++) {
			ArrayList<Integer> cards = new ArrayList<Integer>();
			for (int i = 0; i < 13; i++) {
				int k = drawCard(cards);
			}
			int count = 9;

			Card a1 = new Card(cards.get(0));
			Card a2 = new Card(cards.get(1));
			Card b1 = new Card(cards.get(2));
			Card b2 = new Card(cards.get(3));

			Card f1 = new Card(cards.get(4));
			Card f2 = new Card(cards.get(5));
			Card f3 = new Card(cards.get(6));
			Card t = new Card(cards.get(7));
			Card r = new Card(cards.get(8));

			//Flop play
			ArrayList<Card> onboard = new ArrayList<Card>();
			onboard.add(f1);
			onboard.add(f2);
			onboard.add(f3);
			int swap1 = swap_alg(onboard, a1, a2);
			if (swap1 == 1) {
				a1 = new Card(cards.get(count));
				count++;
			} 
			if (swap1 == 2) {
				a2 = new Card(cards.get(count));
				count++;
			}
			int swap2 = swap_alg(onboard, b1, b2);
			if (swap2 == 1) {
				b1 = new Card(cards.get(count));
				count++;
			}
			if (swap2 == 2) {
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
			
			updateResults(a1, a2, b1, b2, winner, cards, wins, played);
			if (times % 10000 == 0) System.out.println("Progress: " + (1.0 * times / n));
		}
		printResults(wins, played);
	}
	
	//input: two sets of 5 card hands; output: int winner (0 for BU, 1 for BB)
	private static int determineWinner(ArrayList<Integer> bHandButton, ArrayList<Integer> bHandBigBlind) {
		if (bHandButton.get(0) > bHandBigBlind.get(0)) return 0;
		else if (bHandButton.get(0) < bHandBigBlind.get(0)) return 1;
		else {
			for (int j = 1; j < bHandButton.size(); j++) {
				if (bHandButton.get(j) > bHandBigBlind.get(j)) { return 0; }
				else if (bHandButton.get(j) < bHandBigBlind.get(j)) { return 1; }
			}
		}
		return -1;
	}

	private static void updateResults(Card a1, Card a2, Card b1, Card b2, int winner, ArrayList<Integer> cards, double[] wins, int[] played) {
		int fv1 = cards.get(0) / 4;
		int fs1 = cards.get(0) % 4;
		int fv2 = cards.get(1) / 4;
		int fs2 = cards.get(1) % 4;

		int sv1 = cards.get(2) / 4;
		int ss1 = cards.get(2) % 4;
		int sv2 = cards.get(3) / 4;
		int ss2 = cards.get(3) % 4;

		int first = determineHandIndex(fv1, fv2, fs1, fs2);
		int second = determineHandIndex(sv1, sv2, ss1, ss2);

		played[first]++;
		played[second]++;
		if (winner == 0) wins[first] += 1.0;
		else if (winner == 1) wins[second] += 1.0;
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
			if (s1 == s2) return 13 * (12 - larger) + (12 - smaller); //suited cards in top right
			else return 13 * (12 - smaller) + (12 - larger); //unsuited cards in bottom left
		}
	}

	private static void printResults(double[] wins, int[] played) {
		for (int i = 0; i < 13; i++) {
			for (int j = 0; j < 13; j++) {
				double dec = 1.0 * wins[13 * i + j] / played[13 * i + j];
				System.out.printf("%.5f" + "\t", dec);
			}
			System.out.println();
		}
	}
}