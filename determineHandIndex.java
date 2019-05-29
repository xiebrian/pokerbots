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
		if (s1 == s2) return 13 * (12 - larger) + (12 - smaller); 
		else return 13 * (12 - smaller) + (12 - larger); 
	}
}