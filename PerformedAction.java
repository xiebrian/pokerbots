
public class PerformedAction {
	
	public static final String[] ACTIONS = {"BET", "CALL", "CHECK", "RAISE", "FOLD", "DISCARD", "DEAL", "POST", "REFUND", "SHOW", "TIE", "WIN"}; 
	public static final String[] STREETS = {"FLOP", "TURN", "RIVER"};
	
	int action;
	int actor;
	int value;
	int disc = -1;
	int gain = -1;
	
	public PerformedAction(int act, int player, int val){
		action = act;
		actor = player;
		value = val;
	}
	
	public PerformedAction(String s, String myName, String oppName){
		
		String[] words = s.split(":");
		actor = -1;
		switch(words[0]){
		case "BET":
			action = 0;
			value = Integer.parseInt(words[1]);
			if(words.length > 2){
				if(words[2].compareToIgnoreCase(oppName) == 0)
					actor = 2;
				else
					actor = 1;
			}
			break;
		case "CALL":
			action = 1;
			value = -1;
			if(words.length > 1){
				if(words[1].compareToIgnoreCase(oppName) == 0)
					actor = 2;
				else
					actor = 1;
			}
			break;
		case "CHECK":
			action = 2;
			value = -1;
			if(words.length > 1){
				if(words[1].compareToIgnoreCase(oppName) == 0)
					actor = 2;
				else
					actor = 1;
			}
			break;
		case "DEAL":
			action = 6;
			value = -1;
			for(int i = 0; i < STREETS.length; i++){
				if(STREETS[i].compareToIgnoreCase(words[1]) == 0){
					value = i;
					break;
				}
			}
			break;
		case "FOLD":
			action = 4;
			value = -1;
			if(words.length > 1){
				if(words[1].compareToIgnoreCase(oppName) == 0)
					actor = 2;
				else
					actor = 1;
			}
			break;
		case "POST":
			action = 7;
			value = Integer.parseInt(words[1]);
			if(words[2].compareToIgnoreCase(oppName) == 0)
				actor = 2;
			else
				actor = 1;
			break;
		case "DISCARD":
			action = 5;
			value = -1;
			if(words.length == 2){
				if(words[1].compareToIgnoreCase(oppName) == 0)
					actor = 2;
				else
					actor = 1;
			} else if(words.length == 4){
				actor = 1;
				disc = new Card(words[1]).toInt();
				gain = new Card(words[2]).toInt();
			}
			break;
		case "RAISE":
			action = 3;
			value = Integer.parseInt(words[1]);
			if(words.length > 2){
				if(words[2].compareToIgnoreCase(oppName) == 0)
					actor = 2;
				else
					actor = 1;
			}
			break;
		case "REFUND":
			action = 8;
			value = Integer.parseInt(words[1]);
			if(words[2].compareToIgnoreCase(oppName) == 0)
				actor = 2;
			else
				actor = 1;
			break;
		case "SHOW":
			action = 9;
			Card hole1 = new Card(words[1]);
			Card hole2 = new Card(words[2]);
			value = 4*hole1.value + hole1.suit;
			value = 52 * value + 4*hole2.value + hole2.suit;
			if(words[3].compareToIgnoreCase(oppName) == 0)
				actor = 2;
			else
				actor = 1;
			break;
		case "TIE":
			action = 10;
			value = Integer.parseInt(words[1]);
			if(words[2].compareToIgnoreCase(oppName) == 0)
				actor = 2;
			else
				actor = 1;
			break;
		case "WIN":
			action = 11;
			value = Integer.parseInt(words[1]);
			if(words[2].compareToIgnoreCase(oppName) == 0)
				actor = 2;
			else
				actor = 1;
			break;
		}
		
	}
	
	public String toString(){
		
		String response = ACTIONS[action];
		switch(action){
		case 0: // bet
		case 3: // raise
		case 7: // post
		case 8: // refund
		case 10: // tie
		case 11: // win
			response += ":" + value;
			if(actor != -1)
				response += ":" + actor;
			break;
		case 1: // call
		case 2: // check
		case 4: // fold
		case 5: // discard
			if(actor != -1)
				response += ":" + actor;
			if(disc != -1)
				response += ":" + new Card(disc) + ":" + new Card(gain);
			break;
		case 6: // deal
			response += ":" + STREETS[value];
			break;
		case 9: // show
			response += ":" + new Card(value / 52);
			response += ":" + new Card(value % 52);
			if(actor != -1)
				response += ":" + actor;
			break;
		}
		return response;
		
	}
	
}
