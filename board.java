public class Board {
	
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

	public static void main(String args[]) throws IOException {
		Scanner sc = new Scanner(System.in);
		System.out.print("Board? ");
		String board = sc.nextLine();
		
	}

}