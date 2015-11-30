package pdp11;

public class Tm11 {
	
	static int LRC;
	static int DBR;
	static int BUS_ADDRESS;
	static int BYTE_COUNT;
	static int CONTROL;
	static int STATUS;
	
	static void reset(){
		LRC = 0;
		DBR = 0;
		BUS_ADDRESS = 0;
		BYTE_COUNT = 0;
		CONTROL = 0;
		STATUS = 0;
	}
	
	static int tape_boot[] = {
		0012700, //mov #bus_address, r0
		0172526, //
		0010040, //mov r0, -(r0) ->byte_count
		0012740, //mov #060003, -(r0) ->control
		0060003, //
		0000777, //
	    };
}
