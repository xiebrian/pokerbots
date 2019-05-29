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
			//System.out.println(cards);

			Card a1 = new Card(cards.get(0));
			Card a2 = new Card(cards.get(1));
			Card b1 = new Card(cards.get(2));
			Card b2 = new Card(cards.get(3));

			Card f1 = new Card(cards.get(4));
			Card f2 = new Card(cards.get(5));
			Card f3 = new Card(cards.get(6));
			Card t = new Card(cards.get(7));
			Card r = new Card(cards.get(8));

			int count = 9;

			//Flop
			//System.out.println();
			//System.out.println("Flop");
			//System.out.println("Button\t\t\tBB");
			//String str = a1.toString() + " " + a2.toString() + "\t";
			//str += f1.toString() + " " + f2.toString() + " " + f3.toString();
			//str += "\t" + b1.toString() + " " + b2.toString();
			//System.out.println(str);

			ArrayList<Card> onboard = new ArrayList<Card>();
			onboard.add(f1);
			onboard.add(f2);
			onboard.add(f3);

			//Flop swaps
			int swap1 = swap_alg(onboard, a1, a2);
			if (swap1 == 1) { //swap first card
				a1 = new Card(cards.get(count));
				count++;
			} 
			if (swap1 == 2) { //swap second card
				a2 = new Card(cards.get(count));
				count++;
			}
			int swap2 = swap_alg(onboard, b1, b2);
			if (swap2 == 1) { //swap first card
				b1 = new Card(cards.get(count));
				count++;
			}
			if (swap2 == 2) { //swap second card
				b2 = new Card(cards.get(count));
				count++;
			}

			//Turn
			//System.out.println();
			//System.out.println("Turn");
			//System.out.println("Button\t\t\tBB");
			//str = a1.toString() + " " + a2.toString() + "\t";
			//str += f1.toString() + " " + f2.toString() + " " + f3.toString() + " " + t.toString();
			//str += "\t" + b1.toString() + " " + b2.toString();
			//System.out.println(str);
			onboard.add(t);

			//Turn swaps
			swap1 = swap_alg(onboard, a1, a2);
			if (swap1 == 1) { //swap first card
				a1 = new Card(cards.get(count));
				count++;
			} 
			if (swap1 == 2) { //swap second card
				a2 = new Card(cards.get(count));
				count++;
			}
			swap2 = swap_alg(onboard, b1, b2);
			if (swap2 == 1) { //swap first card
				b1 = new Card(cards.get(count));
				count++;
			}
			if (swap2 == 2) { //swap second card
				b2 = new Card(cards.get(count));
				count++;
			}

			//River
			/*
			System.out.println();
			System.out.println("River");
			System.out.println("Button\t\t\tBB");
			String str = a1.toString() + " " + a2.toString() + "\t";
			str += f1.toString() + " " + f2.toString() + " " + f3.toString() + " " + t.toString() + " " + r.toString();
			str += "\t" + b1.toString() + " " + b2.toString();
			System.out.println(str);
			*/
			onboard.add(r);

			//System.out.println();

			Card[] button = {f1, f2, f3, t, r, a1, a2};
			Card[] bigblind = {f1, f2, f3, t, r, b1, b2};

			ArrayList<Integer> bhandbutton = bestHand(button);
			ArrayList<Integer> bhandbigblind = bestHand(bigblind);

			//System.out.println("Button Here: " + bhandbutton);
			//System.out.println("BB Here: " + bhandbigblind);
			//System.out.println();

			int winner = determineWinner(bhandbutton, bhandbigblind);
			/*
			for (int i = 0; i < 4; i++) {
				System.out.println("Card #" + i+ ": " + cards.get(i));
				Card alpha = new Card(cards.get(i));
				System.out.println("Card: " + alpha.toString());
			}
			*/
			updateResults(a1, a2, b1, b2, winner, cards, wins, played);
			if (times % 10000 == 0) System.out.println("Progress: " + (1.0 * times / n));
		}
		printResults(wins, played);
	}