package pdp11;

import java.io.IOException;
import java.io.RandomAccessFile;

public class Rk11 {

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
	
	static void rk11access(){
		RKCS = Util.clearBit(RKCS, 0);

		if(RKCS << 28 >>> 29 == 1){

			int datasizeWord = ~(RKWC - 1 - 65535) + 1;
			int tmpRKDA = (((((RKDA & 0x1FE0) >>> 5) << 1) | ((RKDA &0x10) >>> 4)) * 12) + (RKDA & 0xF);

			/*
			System.out.print("\nRK11-Write ");
			System.out.printf("RKCS=%x ", RKCS);
			System.out.printf("RKER=%x ", RKER);
			System.out.printf("RKDS=%x ", RKDS);
			System.out.printf("RKDB=%x ", RKDB);
			System.out.printf("RKWC=%x ", RKWC);
			System.out.printf("cnt=%x ", datasizeWord);
			System.out.printf("RKBA=%x ", RKBA);
			System.out.printf("RKDA=%x ", RKDA);
			System.out.printf("blockNo=%x\n", tmpRKDA*512);
			*/

			try {
				RandomAccessFile v6root = new RandomAccessFile( System.getProperty("user.dir") + "\\v6root", "rw");
				v6root.seek(tmpRKDA * 512);

				int phyAddr = ((RKCS & 0x30) << 12) + RKBA;
				for(int i=0;i<datasizeWord * 2; i++){
					v6root.write(Memory.getPhyMemory1(phyAddr + i));
				}
				
				v6root.close();
				
			} catch (IOException e) {
			}

			RKCS = Util.setBit(RKCS, 7);
		}


		if(RKCS << 28 >>> 29 == 2){

			int datasizeWord = ~(RKWC - 1 - 65535) + 1;
			//int tmpRKDA = ((((RKDA << 19 >>> 24) << 1) | (RKDA << 27 >>> 31)) * 12) + (RKDA << 28 >>> 28);
			int tmpRKDA = (((((RKDA & 0x1FE0) >>> 5) << 1) | ((RKDA &0x10) >>> 4)) * 12) + (RKDA & 0xF);
			
			/*
			System.out.print("\nRK11-Read ");
			System.out.printf("RKCS=%x ", RKCS);
			System.out.printf("RKER=%x ", RKER);
			System.out.printf("RKDS=%x ", RKDS);
			System.out.printf("RKDB=%x ", RKDB);
			System.out.printf("RKWC=%x ", RKWC);
			System.out.printf("cnt=%x ", datasizeWord);
			System.out.printf("RKBA=%x ", RKBA);
			System.out.printf("RKDA=%x ", RKDA);
			System.out.printf("blockNo=%x\n", tmpRKDA*512);
			*/

			try {
				RandomAccessFile v6root = new RandomAccessFile( System.getProperty("user.dir") + "\\v6root", "r");
				v6root.seek(tmpRKDA * 512);

				int phyAddr = ((RKCS & 0x30) << 12) + RKBA;
				for(int i=0;i<datasizeWord * 2; i++){
					byte tmp = v6root.readByte();
					Memory.setPhyMemory1(phyAddr + i, tmp);
				}

				v6root.close();
				
			} catch (IOException e) {
			}

			RKCS = Util.setBit(RKCS, 7);

		}

		if(Util.checkBit(RKCS, 6) == 1){
			BR_PRI = 5;
			BR_VEC = 0220;
		}

	}
	
	static void rk11error(){
		RKCS = Util.setBit(RKCS, 15);
		if((RKER << 16 >>> 21) != 0){
			RKCS = Util.setBit(RKCS, 14);
		}
		
		if(Util.checkBit(RKCS, 6) == 1){
			BR_PRI = 5;
			BR_VEC = 0220;
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
