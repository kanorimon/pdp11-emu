package pdp11;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

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
	
	static final int BOOT_START = 1024; //BOOT_ROMの読込先アドレス
	
	static void reset(){
		RKDS = 0;
		RKER = 0;
		RKCS = 0;
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
		for(;;){
			try{
				  Thread.sleep(1);
			}catch (InterruptedException e){
			}
			
			//if(Util.checkBit(RKCS, 0) == 1){

			//}
			
			//if(RKER != 0){

			//}
			
		}
	}
	
	static void rk11out(){
		RKCS = Util.clearBit(RKCS, 0);
		//System.out.println("RK11OUT");
		
		if(RKCS << 28 >>> 29 == 2){
			//バイナリ取得
			String dir = System.getProperty("user.dir");
			File file = new File(dir + "\\v6root");
			Path fileName = file.toPath();
			byte[] bf = null;
			try {
		        bf = java.nio.file.Files.readAllBytes(fileName);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			System.out.printf("RKCS=%x ", RKCS);
			System.out.printf("RKWC=%x ", RKWC);
			int datasizeWord = ~(RKWC - 1 - 65535);
			//System.out.printf("cnt=%x ", datasizeWord);
			System.out.printf("RKBA=%x ", RKBA);
			System.out.printf("RKDA=%x ", RKDA);
			int tmpRKDA = ((((RKDA << 19 >>> 24) << 1) | (RKDA << 27 >>> 31)) * 12) + (RKDA << 28 >>> 28);
			//System.out.printf("tmpRKDA=%x ", tmpRKDA);
			System.out.printf("blockNo=%x\n", tmpRKDA*512);
			for(int i=0;i<(datasizeWord+1)*2;i++){
				Memory.setMemory1(Mmu.analyzeMemory(RKBA + i,Register.getNowMode()), bf[tmpRKDA*512 + i]);
			}
			
			/*
			for(int i=0;i<256;i++){
				if(i%16 == 0) System.out.print("\n");
				System.out.printf("%02x ",Memory.mem[i]);
			}
			*/
			RKCS = Util.setBit(RKCS, 7);
			if(Util.checkBit(RKCS, 6) == 1){
				System.out.println("RK11INTER");
				//System.out.printf("NowMode=%d\n",Register.getNowMode());
				BR_PRI = 5;
				BR_VEC = 0220;
			}					
		}
	}
	
	static void rk11error(){
		RKCS = Util.setBit(RKCS, 15);
		if((RKER << 16 >>> 21) != 0){
			RKCS = Util.setBit(RKCS, 14);
		}
	}

	static int boot_rom[] = {
	    0042113,                        /* "KD" */
	    0012706, BOOT_START,            /* MOV #boot_start, SP */
	    0012700, 0000000,               /* MOV #unit, R0        ; unit number */
	    0010003,                        /* MOV R0, R3 */
	    0000303,                        /* SWAB R3 */
	    0006303,                        /* ASL R3 */
	    0006303,                        /* ASL R3 */
	    0006303,                        /* ASL R3 */
	    0006303,                        /* ASL R3 */
	    0006303,                        /* ASL R3 */
	    0012701, 0177412,               /* MOV #RKDA, R1        ; csr */
	    0010311,                        /* MOV R3, (R1)         ; load da */
	    0005041,                        /* CLR -(R1)            ; clear ba */
	    0012741, 0177000,               /* MOV #-256.*2, -(R1)  ; load wc */
	    0012741, 0000005,               /* MOV #READ+GO, -(R1)  ; read & go */
	    0005002,                        /* CLR R2 */
	    0005003,                        /* CLR R3 */
	    0012704, BOOT_START+020,        /* MOV #START+20, R4 */
	    0005005,                        /* CLR R5 */
	    0105711,                        /* TSTB (R1) */
	    0100376,                        /* BPL .-2 */
	    0105011,                        /* CLRB (R1) */
	    0005007                         /* CLR PC */
	};

	/*
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
    */


}
