import java.util.*;

public class Player {
	
	//------------------------------FIELDS-------------------------------//
	
	public static final int NUM_PF_BETS = 6;
	public static final int NUM_HANDS = 169;
	public static final int[] MAX_PF_BETS = {0, 2, 8, 24, 72, 200};
	public static final int[] IDEAL_PF_BETS = {0, 2, 6, 18, 46, 200};
	public static final int BET = 0, CALL = 1, CHECK = 2, RAISE = 3, FOLD = 4,
			DISCARD = 5, DEAL = 6, POST = 7, REFUND = 8, SHOW = 9, TIE = 10, WIN = 11; 
	
	public static final int NONE = -1;
	public static final int BUTTON = 1;
	public static final int BIG = 2;
	
	public static int lastLv = 0;
	
	public final Random random = new Random();
	
	public double[][] preflopTree = new double[NUM_HANDS][NUM_PF_BETS];
	public int[][] profit = new int[NUM_HANDS][NUM_PF_BETS];
	
	int netWin = 0;
	int stack;
	int position;
	Card hole1, hole2, d1, d2;
	String name;
	
	//----------------------------CONSTRUCTOR----------------------------//
	
	//Initializes strategy (preflopTree) and profits (profit)
	public Player(boolean button) {
		if (button) {
			position = BUTTON;
			stack = 199;
			name = "Button";
		}
		else {
			position = BIG;
			stack = 198;
			name = "Big Blind";
		}
		for (int i = 0; i < NUM_HANDS; i++) {
			for (int j = 0; j < NUM_PF_BETS; j++) {
				preflopTree[i][j] = 1.0 / NUM_PF_BETS;
				profit[i][j] = 0;
			}
		}
	}
	
	//-------------------------PRIMARY METHODS------------------------//

	public void playPreflop(ArrayList<PerformedAction> prev, int currentLv) {
		int max = getBetLevel();
		int lastAct = prev.get(prev.size() - 1).action;
		if (lastAct == POST) {
			if (max == 1) 
				call(prev);
			else
				raise(prev, IDEAL_PF_BETS[currentLv + 1]);
		}
		if (lastAct == CALL && prev.get(prev.size() - 2).action == POST) {
			if (max >= 2) 
				raise(prev, IDEAL_PF_BETS[currentLv + 1]);
			else 
				check(prev);
		}
		if (lastAct == RAISE) {
			if (max == currentLv) 
				call(prev);
			else if (max >= currentLv + 1)
				raise(prev, IDEAL_PF_BETS[currentLv + 1]);
			else 
				fold(prev);
		}
	}
	
	public void playStreet(ArrayList<PerformedAction> prev, Card[] board) {
		int prevActs = 0;
		if (prev.size() > 2) {
			for (int i = prev.size() - 1; i >= 0; i--) {
				int act = prev.get(i).action;
				if (act == DEAL)
					break;
				prevActs++;
			}
		}
		/*if (board.length > 0 && board.length < 5) 
			prevActs -= 2; //account for two discard/check actions on flop and turn
		//System.out.println(prevActs);*/
		if (board.length == 0 && position == BUTTON) { 
			playUpTo(prev, maxBetSize(pot(prev), 0, getBetLevel()));
		}
		if (board.length == 0 && position == BIG) {
			playUpTo(prev, maxBetSize(pot(prev), 1, getBetLevel()));
		}
		if (board.length > 0) {
			playUpTo(prev, 0);
		}
	}
	
	//Plays according to maximum bet amount
	private void playUpTo(ArrayList<PerformedAction> prev, int max) {
		int pot = pot(prev);
		int lastAct = prev.get(prev.size() - 1).action;
		if (max == 0) 
			checkFold(prev);
		if (lastAct == CHECK || lastAct == DISCARD || lastAct == DEAL) {
			if (max >= 2 * pot / 3) 
				bet(prev, 2 * pot / 3);
			else
				check(prev);
		}
		else if (lastAct == BET || lastAct == RAISE) {
			int val = prev.get(prev.size() - 1).value;
			if (val > max) 
				fold(prev);
			else if (3 * val > max) 
				call(prev);
			else
				raise(prev, Math.min(stack, prev.get(prev.size() - 1).value * 3));
		}
	}
	
	//Determines the preflop betting level from the preflopTree
	public int getBetLevel() {
		int index = PokerTrainer.handToIndex(hole1, hole2);
		int level = 0;
		double threshold = random.nextDouble();
		double cumProbability = 0;
		while (level < NUM_PF_BETS - 1) {
			cumProbability += preflopTree[index][level];
			if (threshold < cumProbability)
				break;
			level++;
		}
		return level;
	}
	
	//Given the pot size, starting betting level, and desired end betting level
	//returns the maximum amount of money player is willing to put into pot
	public static int maxBetSize(int pot, int startLv, int endLv) {
		int behind = 200 - pot/2; //how much money behind
		if (startLv == endLv) { 
			return 0;
		}
		else { //maxBets go pot(bet), 3pot (raise), 9pot (reraise), etc..
			int maxBet = pot;
			while (startLv < endLv - 1) {
				maxBet *= 3;
				startLv++;
				if (maxBet > behind) 
					return behind;
			}
			return maxBet;
		}
	}
	
	//Accumulates the profit over one hand
	public void accumulateProfits(int amt, int betLv) {
		int index = PokerTrainer.handToIndex(hole1, hole2);
		profit[index][betLv] += amt;
	}
	
	//Updates the weights in preflopTree using FSICFR
	public void updateWeights() {
		int normalizingSum = 0;
		for (int i = 0; i < NUM_HANDS; i++) 
			for (int j = 0; j < NUM_PF_BETS; j++) 
				normalizingSum += Math.abs(profit[i][j]);
		for (int i = 0; i < NUM_HANDS; i++) {
			double normalizingWeights = 0;
			//normalize the profits, and multiply weights by e^profits
			for (int j = 0; j < NUM_PF_BETS; j++) {
				profit[i][j] = profit[i][j] * NUM_HANDS / normalizingSum;
				preflopTree[i][j] *= Math.exp(profit[i][j]);
				normalizingWeights += preflopTree[i][j];
			}
			//normalize the weights for each hand
			for (int j = 0; j < NUM_PF_BETS; j++) {
				preflopTree[i][j] /= normalizingWeights;
			}
		}
		//clears profits
		for (int i = 0; i < NUM_HANDS; i++) {
			for (int j = 0; j < NUM_PF_BETS; j++) {
				profit[i][j] = 0;
			}
		}
	}
	
	//-------------------------HELPER METHODS-------------------------//

	//returns the size of the pot
	private int pot(ArrayList<PerformedAction> prev) {
		int pot = 0;
		int prevBet = 0;
		int currBet = 0;
		int totalPrev = 0;
		for (PerformedAction a : prev) {
			int act = a.action;
			if (act == BET) 
				currBet = a.value;
			else if (act == CALL) {
				pot += 2 * currBet;
				prevBet = 0;
				currBet = 0;
			}
			else if (act == RAISE) {
				prevBet = currBet;
				currBet = a.value;
			}
			else if (act == POST) 
				pot += a.value;
		}
		pot += currBet + prevBet;
		return pot;
	}
	
	public void playDiscard(ArrayList<PerformedAction> prev, Card[] board) {
		int disc = Swap.swapAlg(board, hole1, hole2);
		if (disc == -1)
			check(prev);
		else
			discard(prev, disc);
	}

	private void checkCall(ArrayList<PerformedAction> prev) {
		int action = prev.get(prev.size() - 1).action;
		if (action == BET || action == RAISE) 
			call(prev);
		else
			check(prev);
	}
	
	private void checkFold(ArrayList<PerformedAction> prev) {
		int action = prev.get(prev.size() - 1).action;
		if (action == BET || action == RAISE) 
			fold(prev);
		else
			check(prev);
	}
	
	private void bet(ArrayList<PerformedAction> prev, int amt) {
		if (amt > stack) 
			amt = stack;
		prev.add(new PerformedAction(BET, position, amt));
		stack -= amt;
	}
	
	private void call(ArrayList<PerformedAction> prev) {
		if (prev.size() > 2) {
			int totalPrev = 0;
			for (int i = prev.size() - 1; i >= 0; i--) {
				int act = prev.get(i).action;
				if (act == DEAL)
					break;
				if ((act == BET || act == RAISE || act == POST) && prev.get(i).actor == position) {
					totalPrev += prev.get(i).value;
					break;
				}
			}
			stack += totalPrev - prev.get(prev.size() - 1).value;
		}
		else
			stack -= 1;
		prev.add(new PerformedAction(CALL, position, -1));
	}
	
	private void check(ArrayList<PerformedAction> prev) {
		prev.add(new PerformedAction(CHECK, position, -1));
	}
	
	private void raise(ArrayList<PerformedAction> prev, int amt) {
		int totalPrev = 0;
		for (int i = prev.size() - 1; i >= 0; i--){
			int act = prev.get(i).action;
			if (act == DEAL)
				break;
			if((act == BET || act == RAISE || act == POST) && prev.get(i).actor == position) {
				totalPrev += prev.get(i).value;
				break;
			}
		}
		if (amt - totalPrev > stack) 
			amt = stack + totalPrev; //shove instead
		stack += totalPrev - amt;
		prev.add(new PerformedAction(RAISE, position, amt));
	}
	
	private void fold(ArrayList<PerformedAction> prev) {
		prev.add(new PerformedAction(FOLD, position, -1));
	}
	
	private void discard(ArrayList<PerformedAction> prev, int card) {
		if (card != 1 && card != 2)
			return;
		PerformedAction disc = new PerformedAction(DISCARD, position, -1);
		if (card == 1) {
			disc.disc = hole1.toInt();
			if (d1 == null) {
				hole1 = d2;
				d2 = null;
			} 
			else {
				hole1 = d1;
				d1 = null;
			}
			disc.gain = hole1.toInt();
		}
		else if (card == 2) {
			disc.disc = hole2.toInt();
			if (d1 == null) {
				hole2 = d2;
				d2 = null;
			}
			else {
				hole2 = d1;
				d1 = null;
			}
			disc.gain = hole2.toInt();
		}
		prev.add(disc);
	}
	
	public void refresh() {
		if(position == BUTTON) 
			stack = 199;
		else
			stack = 198;
	}
	
	public String toString() {
		return name + ": " + stack + ". Cards" + hole1 + ", " + hole2;
	}
	
	public void deal(Card hole1, Card hole2, Card d1, Card d2) {
		this.hole1 = hole1;
		this.hole2 = hole2;
		this.d1 = d1;
		this.d2 = d2;
	}
}
