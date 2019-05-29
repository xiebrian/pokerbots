import java.util.*;

public class PlayerCFR {
	
	int amount;
	int button;
	Card hole1, hole2, d1, d2;
	String name;
	
	public PlayerCFR(boolean b){
		if(b){
			button = 1;
			amount = 199;
			name = "Button";
		} else {
			button = 2;
			amount = 198;
			name = "Big blind";
		}
	}
	
	public void refresh(){
		if(button == 1)
			amount = 199;
		else
			amount = 198;
	}
	
	public String toString(){
		return name + ": " + amount + ". Cards " + hole1 + ", " + hole2;
	}
	
	public void deal(Card c1, Card c2, Card c3, Card c4){
		hole1 = c1;
		hole2 = c2;
		d1 = c3;
		d2 = c4;
	}
	
	public void playStreet(ArrayList<PerformedAction> prev, Card[] board){
		int prevActs = 0;
		if(prev.size() > 2){
			for(int i = prev.size() - 1; i >= 0; i--){
				int act = prev.get(i).action;
				if(act == 6)
					break;
				prevActs++;
			}
		}
		if(board.length > 0 && board.length < 5)
			prevActs -= 2; // account for two discard/check actions on flop and turn
		// System.out.println(prevActs);
		if(board.length == 0 && button == 1)
			call(prev);
		else {
			if(button == 1){
				playUpTo(prev, 11);
			} else {
				playUpTo(prev, 12);
			}
		}
	}
	
	public void playDiscard(ArrayList<PerformedAction> prev, Card[] board){
		
		int disc = Swap.swapAlg(board, hole1, hole2);
		if (disc == -1)
			check(prev);
		else
			discard(prev, disc);
		
	}
	
	private void checkCall(ArrayList<PerformedAction> prev) {
		
		int action = prev.get(prev.size() - 1).action;
		if(action == 0 || action == 3)
			call(prev);
		else
			check(prev);
		
	}
	
	private void checkFold(ArrayList<PerformedAction> prev) {
		
		int action = prev.get(prev.size() - 1).action;
		if(action == 0 || action == 3)
			fold(prev);
		else
			check(prev);
		
	}

	private void bet(ArrayList<PerformedAction> prev, int a){
		if(a > amount)
			a = amount;
		prev.add(new PerformedAction(0, button, a));
		amount -= a;
	}
	
	private void call(ArrayList<PerformedAction> prev){
		if(prev.size() > 2){
			int totalPrev = 0;
			for(int i = prev.size() - 1; i >= 0; i--){
				int act = prev.get(i).action;
				if(act == 6)
					break;
				if((act == 0 || act == 3 || act == 7) && prev.get(i).actor == button)
					totalPrev += prev.get(i).value;
			}
			amount += totalPrev - prev.get(prev.size() - 1).value;
		}else
			amount -= 1;
		prev.add(new PerformedAction(1, button, -1));
	}
	
	private void check(ArrayList<PerformedAction> prev){
		prev.add(new PerformedAction(2, button, -1));
	}
	
	private void raise(ArrayList<PerformedAction> prev, int a){
		int totalPrev = 0;
		for(int i = prev.size() - 1; i >= 0; i--){
			int act = prev.get(i).action;
			if(act == 6)
				break;
			if((act == 0 || act == 3 || act == 7) && prev.get(i).actor == button){
				totalPrev += prev.get(i).value;
				break;
			}
		}
		if(a - totalPrev > amount){
			a = amount + totalPrev;	// shove instead
		}
		amount += totalPrev - a;
		prev.add(new PerformedAction(3, button, a));
	}
	
	private void fold(ArrayList<PerformedAction> prev){
		prev.add(new PerformedAction(4, button, -1));
	}
	
	private void discard(ArrayList<PerformedAction> prev, int c){
		if(c < 0)
			return;
		PerformedAction disc = new PerformedAction(5, button, -1);
		if(c == 1){
			disc.disc = hole1.toInt();
			if(d1 == null) {
				hole1 = d2;
				d2 = null;
			} else {
				hole1 = d1;
				d1 = null;
			}
			disc.gain = hole1.toInt();
		} else {
			disc.disc = hole2.toInt();
			if(d1 == null) {
				hole2 = d2;
				d2 = null;
			} else {
				hole2 = d1;
				d1 = null;
			}
			disc.gain = hole2.toInt();
		}
		prev.add(disc);
	}
	
	private void playUpTo(ArrayList<PerformedAction> prev, int max){
		int pot = pot(prev);
		int lastAct = prev.get(prev.size() - 1).action;
		if(max == 0)
			checkFold(prev);
		if(lastAct == 2 || lastAct == 7 || lastAct == 5) // if he checks/just posted/just discarded we bet
			bet(prev, Math.min(max, 2*pot/3));
		else if(lastAct == 0 || lastAct == 3) {// if he bet we triple
			int val = prev.get(prev.size() - 1).value; 
			if(val > max)
				fold(prev);
			else if(2 * val > max)
				call(prev);
			else
				raise(prev, Math.min(max, prev.get(prev.size() - 1).value * 3));
		}
	}
	
	private int pot(ArrayList<PerformedAction> prev){
		int pot = 0;
		int prevBet = 0;
		int currBet = 0;
		int totalPrev = 0;
		for(PerformedAction a : prev){
			int act = a.action;
			if(act == 0){
				currBet = a.value;
			} else if(act == 1){
				pot += 2*currBet;
				prevBet = 0;
				currBet = 0;
			} else if(act == 3){
				prevBet = currBet;
				currBet = a.value;
			} else if(act == 7)
				pot += a.value;
		}
		pot += currBet + prevBet;
		return pot;
	}
	
}
