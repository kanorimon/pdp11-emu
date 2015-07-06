package pdp11;

public class Rk11 extends Thread {

	static int RKDS; //RK11 Drive Status Register
	static int RKER; //RK11 Error Register
	static int RKCS; //RK11 Control Status Register
	static int RKWC; //RK11 Word Count Register
	static int RKBA; //RK11 Bus Address Register
	static int RKDA; //RK11 Disk Address Register
	static int RKMR; //RK11 Maintenance Register
	static int RKDB; //RK11 Data Buffer Register
	
	static int BR_PRI; //割り込み優先度
	static int BR_VEC; //割り込みベクタ
	
	static void reset(){
		RKDS = 0;
		RKER = 0;
		RKCS = 128;
		RKWC = 0;
		RKBA = 0;
		RKDA = 0;
		RKMR = 0;
		RKDB = 0;

		BR_PRI = 0;
		BR_VEC = 0;
	}
	
	public void run(){
		System.out.println("RK11 run");
		//RKCS = 128;
		for(;;){
			try{
				  Thread.sleep(1000);
			}catch (InterruptedException e){
			}
			
			if((RKCS & 1) != 0){
				RKCS = RKCS - 1;
				System.out.println("RK11OUT");
				if((RKCS & 64) != 0){
					System.out.println("RK11INTER");
					BR_PRI = 5;
					BR_VEC = 0220;
				}
				
			}
		}
		
	}
	
	
	static void setRKCS(int no){
		RKCS = no;
		RKCS = RKCS | 128;
	}
	
	static int boot_rom[] = {
		0012700, //mov #rkda, r0
		0177412, //
		0005040, //clr -(r0)
		0010040, //mov r0, -(r0)
		0012740, //mov #5, -(r0)
		0000005, //
		0105710, //1: tstb (r0)
		0002376, //bge 1b
		0005007  //clr pc
	    };


}
