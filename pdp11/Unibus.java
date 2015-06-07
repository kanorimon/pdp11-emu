package pdp11;

import java.util.ArrayList;

public class Unibus {
	static ArrayList<Integer> br;
	static ArrayList<Integer> bg;
		
	static void reset(){
		br = new ArrayList();
		bg = new ArrayList();
	}
}

class PriorityDto {
	int priority;
	int vectorAddress;
}
