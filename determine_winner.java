private static int determine_winner(ArrayList<Integer> bhandbutton, ArrayList<Integer> bhandbigblind) {
	if (bhandbutton.get(0) > bhandbigblind.get(0)) return 0;
	else if (bhandbutton.get(0) < bhandbigblind.get(0)) return 1;
	else {
		for (int j = 1; j < bhandbutton.size(); j++) {
			if (bhandbutton.get(j) > bhandbigblind.get(j)) { return 0; }
			else if (bhandbutton.get(j) < bhandbigblind.get(j)) { return 1; }
		}
	}
	return -1	
}
