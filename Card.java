import java.util.*;

public class Card implements Comparable<Card>{

	public static final char[] ORDER = { '2', '3', '4', '5', '6', '7', '8', '9', 'T', 'J', 'Q', 'K', 'A' };
	public static final char[] SUITS = { 's', 'h', 'd', 'c' };
	
	int value;
	int suit;

	public Card(int n) {
		value = n / 4;
		suit = n % 4;
	}

	public Card(String s) {
		switch (s.charAt(0)) {
		case 'T':
			value = 8; // weird indexing in order array
			break;
		case 'J':
			value = 9;
			break;
		case 'Q':
			value = 10;
			break;
		case 'K':
			value = 11;
			break;
		case 'A':
			value = 12;
			break;
		default:
			value = Integer.parseInt(s.substring(0, 1)) - 2;
		}
		
		switch (s.charAt(1)){
		case 's':
			suit = 0;
			break;
		case 'h':
			suit = 1;
			break;
		case 'd':
			suit = 2;
			break;
		default:
			suit = 3;
			break;
		}
	}
	
	public Card(Card c){
		value = c.value;
		suit = c.suit;
	}

	public int toInt(){
		return 4*value + suit;
	}
	
	public String toString() {
		return "" + ORDER[value] + SUITS[suit];
	}
	
	public int compareTo(Card c){
		if(this.value == c.value)
			return this.suit - c.suit;
		return this.value - c.value;
	}

	public static int bestHand(Card hole1, Card hole2, Card[] board){
		
		Card[] temp = new Card[5];
		return bestHand(hole1, hole2, board, temp);
		
	}

	public static int bestHand(Card hole1, Card hole2, Card[] board, Card[] hand){
		
		int[] ans = new int[6];
		
		// lump cards into one group
		Card[] cards = new Card[2 + board.length];
		cards[0] = new Card(hole1);
		cards[1] = new Card(hole2);
		for(int i = 0; i < board.length; i++){
			cards[i + 2]= new Card(board[i]);
		}
		
		// compute suit and value frequencies, check for flush suit
		// easily check which card is which
		int[] suitFreqs = new int[4];
		int[] numFreqs = new int[13];
		int[] cardInds = new int[52];
		for(int i = 0; i < 52; i++){
			cardInds[i] = -1;
		}
		int flush = -1;
		for(int i = 0; i < cards.length; i++){
			suitFreqs[cards[i].suit]++;
			if(suitFreqs[cards[i].suit] >= 5){
				flush = cards[i].suit;
			}
			// System.out.println(i + " " + cards[i].value);
			numFreqs[cards[i].value]++;
			cardInds[cards[i].value * 4 + cards[i].suit] = i;
		}
		
		// check for most frequent number and second-most frequent number
		int[] temp = new int[numFreqs.length];
		System.arraycopy(numFreqs, 0, temp, 0, temp.length);
		Arrays.sort(temp);
		boolean quad = false;
		boolean full = false;
		boolean trip = false;
		boolean twoPair = false;
		boolean pair = false;
		if(temp[temp.length - 1] == 4){
			quad = true;
		} else if(temp[temp.length - 1] == 3){
			if(temp[temp.length - 2] >= 2)
				full = true;
			else
				trip = true;
		} else if(temp[temp.length - 1] == 2){
			if(temp[temp.length - 2] >= 2)
				twoPair = true;
			else
				pair = true;
		}
		
		// check for straights
		int straight = straight(numFreqs);
		
		// return answer
		if(flush >= 0 && straight >= 0){
			// straight flush
//			System.out.println(straight);
			if(cardInds[flush]>=0 && cardInds[flush+4]>=0 && cardInds[flush+8]>=0 &&
					cardInds[flush+12]>=0 && cardInds[flush+48] >= 0)
				straight = 3;
			for(int i = 12; i >= 4; i--){
				boolean yes = true;
				for(int j = 0; j < 5; j++){
					if(cardInds[4*(i - j)+flush] < 0){
						yes = false;
						break;
					}
				}
				if(yes){
					straight = i;
					break;
				}
			}
//			System.out.println(straight);
			
			ans[0] = straightFlush(straight);
			int ind = 1;
			for(int i = 0; i < cards.length && ind < 6; i++){
				boolean inStraight = straight >= cards[i].value && straight - cards[i].value < 5;
				if(cards[i].value == 12 && straight == 3)
					inStraight = true;
				if(cards[i].suit == flush && inStraight){
					ans[ind++] = i;
//					System.out.println(cards[i]);
				}
			}
			if(ind >= 6) { // if not it can't be a straight flush
				for(int i = 0; i < 5; i++){
					hand[i] = new Card(cards[ans[i + 1]]);
				}
				return ans[0];
			}
		} 
		ans = new int[6];
		if(quad){
			// quads
			int num = -1;
			for(int i = 0; i < numFreqs.length; i++){
				if(numFreqs[i] == 4){
					num = i;
					break;
				}
			}
			ans[0] = quads(num);
			int ind = 1;
			for(int i = 0; i < cards.length; i++){
				if(cards[i].value == num){
					ans[ind++] = i;
					cardInds[cards[i].value*4 + cards[i].suit] = -1;
				}
			}
			for(int i = 51; i >= 0 && ind < 6; i--){
				if(cardInds[i] >= 0)
					ans[ind++] = cardInds[i];
			}
		} else if(full){
			// full house
			int triple = -1;
			int doub = -1;
			for(int i = 12; i >= 0; i--){
				if(numFreqs[i] == 3){
					triple = i;
					break;
				}
			}
			for(int i = 12; i >= 0; i--){
				if(numFreqs[i] >= 2 && i != triple){
					doub = i;
					break;
				}
			}
			ans[0] = fullHouse(triple, doub);
			int ind = 1;
			for(int i = 0; i < cards.length; i++){
				if(cards[i].value == triple)
					ans[ind++] = i;
			}
			for(int i = 0; i < cards.length && ind < 6; i++){
				if(cards[i].value == doub)
					ans[ind++] = i;
			}
		} else if(flush >= 0){
			// flush
			ans[0] = FLUSH;
			int ind = 1;
			for(int i = 12; i >= 0 && ind < 6; i--){
				if(cardInds[i * 4 + flush] >= 0)
					ans[ind++] = cardInds[i * 4 + flush];
			}
			boolean straightFlush = true;
			for(int i = 2; i < ans.length; i++){
				if(cards[ans[i-1]].value - cards[ans[i]].value != 1){
					straightFlush = false;
					break;
				}
			}
			if(cards[ans[1]].value == 12 && cards[ans[2]].value == 3 && cards[ans[3]].value == 2 && cards[ans[4]].value == 1 && cards[ans[5]].value == 0)
				straightFlush = true;
			if(straightFlush)
				ans[0] = straightFlush(cards[ans[1]].value);
		} else if(straight >= 0){
			// straight
			ans[0] = straight(straight);
			int ind = 1;
			boolean[] hit = new boolean[5];
			for(int i = 0; i < cards.length && ind < 6; i++){
				int diff = straight - cards[i].value;
				if(diff >= 0 && diff < 5 && !hit[diff]){
					ans[ind++] = i;
					hit[diff] = true;
				}
			}
		} else if(trip){
			// triple
			int triple = -1;
			for(int i = 12; i >= 0; i--){
				if(numFreqs[i] == 3){
					triple = i;
					break;
				}
			}
			ans[0] = triple(triple);
			int ind = 1;
			for(int i = 0; i < 4; i++){
				if(cardInds[triple*4 + i] >= 0){
					ans[ind++] = cardInds[triple*4 + i];
					cardInds[triple*4 + i] = -1;
				}
			}
			for(int i = 51; i >= 0 && ind < 6; i--){
				if(cardInds[i] >= 0)
					ans[ind++] = cardInds[i];
			}
		} else if(twoPair){
			// two pair
			int high = -1;
			int low = -1;
			for(int i = 12; i >= 0; i--){
				if(numFreqs[i] == 2){
					if(high < 0)
						high = i;
					else {
						low = i;
						break;
					}
				}
			}
			ans[0] = twoPair(high, low);
			int ind = 1;
			for(int i = 0; i < cards.length; i++){
				if(cards[i].value == high || cards[i].value == low)
					ans[ind++] = i;
			}
			for(int i = 51; i >= 0; i--){
				if(cardInds[i] >= 0 && i / 4 != high && i / 4 != low){
					ans[ind++] = cardInds[i];
					break;
				}
			}
		} else if(pair){
			// pair
			int num = -1;
			for(int i = 12; i >= 0; i--){
				if(numFreqs[i] == 2){
					num = i;
					break;
				}
			}
			ans[0] = pair(num);
			int ind = 1;
			for(int i = 0; i < cards.length; i++){
				if(cards[i].value == num)
					ans[ind++] = i;
			}
			for(int i = 51; i >= 0 && ind < 6; i--){
				if(cardInds[i] >= 0 && i / 4 != num){
					ans[ind++] = cardInds[i];
				}
			}
		} else {
			// high card
			int ind = 1;
			for(int i = 51; i >= 0 && ind < 6; i--){
				if(cardInds[i] >= 0){
					if(ind == 1){
						ans[0] = high(i / 4);
					}
					ans[ind++] = cardInds[i];
				}
			}
		}
		
		for(int i = 0; i < 5; i++){
			hand[i] = new Card(cards[ans[i + 1]]);
		}
		return ans[0];
		
	}
	
	public static int straight(int[] numFreqs){
		
		for(int i = 12; i >= 4; i--){
			boolean yes = true;
			for(int j = 0; j < 5; j++){
				if(numFreqs[i - j] == 0){
					yes = false;
					break;
				}
			}
			if(yes){
				return i;
			}
		}
		
		if(numFreqs[0]*numFreqs[1]*numFreqs[2]*numFreqs[3]*numFreqs[12] > 0)
			return 3;
		return -1;
		
	}
	
	public static int straightFlush(int high){
		return high + 169 * 8;
	}
	
	public static int quads(int num){
		return num + 169 * 7;
	}
	
	public static int fullHouse(int trip, int doub){
		return (trip*13 + doub) + 169*6;
	}
	
	public static final int FLUSH = 169*5;
	
	public static int straight(int high){
		return high + 169*4;
	}
	
	public static int triple(int num){
		return num + 169*3;
	}
	
	public static int twoPair(int high, int low){
		return high*13 + low + 169*2;
	}
	
	public static int pair(int num){
		return num + 169;
	}
	
	public static int high(int num){
		return num;
	}
	
	public static String hand(int a){
		switch(a / 169){
		case 8:
			return "Straight flush to " + ORDER[a % 169];
		case 7:
			return "Quads of " + ORDER[a % 169];
		case 6:
			return "Full house: Three " + ORDER[(a % 169) / 13] + "'s, two " + ORDER[a % 13] + "'s";
		case 5:
			return "Flush";
		case 4:
			return "Straight to " + ORDER[a % 169];
		case 3:
			return "Triple of " + ORDER[a % 169];
		case 2:
			return "Two pairs: High " + ORDER[(a % 169) / 13] + ", low " + ORDER[a % 13];
		case 1:
			return "Pair of " + ORDER[a % 169];
		}
		return "High card " + ORDER[a];
	}
	
	public static double riverOdds (Card a1, Card a2, Card b1, Card b2, Card[] board){
		
		if(board.length == 5){
			// river
			Card[] aHand = new Card[5];
			Card[] bHand = new Card[5];
			int aBest = bestHand(a1, a2, board, aHand);
			int bBest = bestHand(b1, b2, board, bHand);
			if(aBest > bBest)
				return 1;
			if(aBest < bBest)
				return 0;
			if(aBest == FLUSH && bBest == FLUSH){
				for(int i = 0; i < aHand.length; i++){
					if(aHand[i].compareTo(bHand[i]) < 0)
						return 0;
					if(aHand[i].compareTo(bHand[i]) > 0)
						return 1;
				}
				return 0.5;
			}
		}
		
		return 0;
		
	}
}