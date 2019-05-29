public class BetSizes {
	
	//given the pot size, starting betting level, and desired end betting level,
	//returns the maximum amount of money player is willing to put into pot.
	public int maxBetSize(int pot, int startLv, int endLv) {
		int stack = 200 - pot/2; //each player's stack left behind
		if (startLv == endLv) { //check, treat any bet under 0.2*pot as a check
			return pot / 5; 
		}
		else { //maxBets go pot (bet), 3pot (raise), 9pot (reraise), 27pot (re-reraise)...
			int maxBet = pot; 
			while (startLv < endLv - 1) { 
				maxBet *= 3;
				startLv++;
				if (maxBet > stack) { //cannot bet more than stack
					return stack;
				}
			}
			return maxBet;
		}
	}
}