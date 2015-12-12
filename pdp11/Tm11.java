package pdp11;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Tm11 {
	/*
	 * 制御レジスタ
	 */
	static int MTRD;	//TM11 TU10 Read Lines
	static int MTD; 	//TM11 Data Buffer
	static int MTCMA; 	//TM11 Current Memory Address Register
	static int MTBRC; 	//TM11 Byte Record Counter
	static int MTC; 	//TM11 Command Register
	static int MTS; 	//TM11 Status Register

	/*
	 * 制御レジスタビット
	 */
	static final int MTS_EOF = 14;
	static final int MTS_READY = 0;
	static final int MTC_READY = 7;
	static final int MTC_INTERRUPT = 6;
	static final int MTC_GO = 0;
	
	/*
	 * 割り込み設定
	 */
	static int BR_PRI; //割り込み優先度
	static int BR_VEC; //割り込みベクタ
	
	/*
	 * BOOTROM
	 */
	static final int BOOT_START = 016000; //BOOT_ROMの読込先アドレス
	static int boot_rom[] = {
		//0046524,                      /* boot_start: "TM" */
		0012706, BOOT_START,            /* mov #boot_start, sp */
		0012700, 0000000,               /* mov #unit_num, r0 */
		0012701, 0172526,               /* mov #172526, r1      ; mtcma */
		0005011,                        /* clr (r1) */
		0010141,                        /* mov r1, -(r1)        ; mtbrc */
		0010002,                        /* mov r0,r2 */
		0000302,                        /* swab r2 */
		0062702, 0060003,               /* add #60003, r2 */
		0010241,                        /* mov r2, -(r1)        ; read + go */
		0105711,                        /* tstb (r1)            ; mtc */
		0100376,                        /* bpl .-2 */
		0005002,                        /* clr r2 */
		0005003,                        /* clr r3 */
		0012704, BOOT_START+020,        /* mov #boot_start+20, r4 */
		0005005,                        /* clr r5 */
		0005007                         /* clr r7 */
	};

	/*
	 * テープの位置情報
	 */
	static int p;
	static int psub;

	static void reset(){
		MTRD = 0;
		MTD = 0;
		MTCMA = 0;
		MTBRC = 0;
		MTC = 0200;
		MTS = 0;

		BR_PRI = 0;
		BR_VEC = 0;

		p = 0;
		psub = 0;
	}

	static void tm11access(){

		//READ
		if(MTC << 28 >>> 29 == 1){

			int datasizeWord = ~((MTBRC & 0xFFFF) - 1 - 65535) + 1;

			try {
				RandomAccessFile tm0 = new RandomAccessFile( System.getProperty("user.dir") + "\\" +  Pdp11.TM0, "r");

				int phyAddr = ((MTC & 0x30) << 12) + (MTCMA & 0xFFFF);
				for(int i=0;i<datasizeWord; i++){
					if(p == 0 && psub == 0) p = p + 4;
					if(psub == 512){
						p = p + 8;
						psub = 0;
					}

					tm0.seek(p);
					byte tmp = tm0.readByte();
					try {
						Memory.setPhyMemory1(phyAddr + i, tmp);
					} catch (MemoryUndefinedException e) {
						e.printStackTrace();
					}
					p++;
					psub++;
				}

				tm0.close();

			} catch (EOFException e) {
				MTS = Util.setBit(MTS, MTS_EOF);
			} catch (IOException e) {
				e.printStackTrace();
			}


			MTC = Util.clearBit(MTC, MTC_GO);
			MTC = Util.setBit(MTC, MTC_READY);
			MTS = Util.setBit(MTS, MTS_READY);
		}

		//REWIND
		if(MTC << 28 >>> 29 == 7){

			p = 0;
			psub = 0;

			MTC = Util.clearBit(MTC, MTC_GO);
			MTC = Util.setBit(MTC, MTC_READY);
			MTS = Util.setBit(MTS, MTS_READY);
		}

		//割込み制御ビットが1の場合は割込みを設定する
		if(Util.checkBit(MTC, MTC_INTERRUPT) == 1){
			BR_PRI = 5;
			BR_VEC = 0224;
		}

	}
}
