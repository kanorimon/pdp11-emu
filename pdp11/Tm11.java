package pdp11;

import java.io.IOException;
import java.io.RandomAccessFile;

public class Tm11 {
	
	static int MTRD;
	static int MTD;
	static int MTCMA;
	static int MTBRC;
	static int MTC;
	static int MTS;

	static final int BOOT_START = 016000; //BOOT_ROM

	static int p;
	static int psub;

	static void reset(){
		MTRD = 0;
		MTD = 0;
		MTCMA = 0;
		MTBRC = 0;
		MTC = 0;
		MTS = 0;

		p = 0;
		psub = 0;
	}

	static void tm11access(){

		/*
		System.out.print("\nTM11-xxx ");
		System.out.printf("MTS=%x ", MTS);
		System.out.printf("MTC=%x ", MTC);
		System.out.printf("MTBRC=%x ", MTBRC);
		System.out.printf("MTCMA=%x ", MTCMA);
		System.out.printf("MTD=%x ", MTD);
		System.out.printf("MTRD=%x \n", MTRD);
		*/

		if(MTC << 28 >>> 29 == 1){

			int datasizeWord = ~((MTBRC & 0xFFFF) - 1 - 65535) + 1;
			//int tmpRKDA = ((((RKDA << 19 >>> 24) << 1) | (RKDA << 27 >>> 31)) * 12) + (RKDA << 28 >>> 28);
			//int tmpRKDA = (((((RKDA & 0x1FE0) >>> 5) << 1) | ((RKDA &0x10) >>> 4)) * 12) + (RKDA & 0xF);

			/*
			System.out.print("\nTM11-Read ");
			System.out.printf("MTS=%x ", MTS);
			System.out.printf("MTC=%x ", MTC);
			System.out.printf("MTBRC=%x ", MTBRC);
			System.out.printf("MTCMA=%x ", MTCMA);
			System.out.printf("MTD=%x ", MTD);
			System.out.printf("cnt=%x ", datasizeWord);
			System.out.printf("MTRD=%x \n", MTRD);
			*/

			try {
				RandomAccessFile tm0 = new RandomAccessFile( System.getProperty("user.dir") + "\\" +  Pdp11.TM0, "r");
				//tm0.seek(MTD * 512 + 4);
				//tm0.seek(MTCMA + 4);

				int phyAddr = ((MTC & 0x30) << 12) + (MTCMA & 0xFFFF);
				//System.out.printf("\nstart_p=%x\n", p);
				for(int i=0;i<datasizeWord; i++){
					if(p == 0 && psub == 0) p = p + 4;
					if(psub == 512){
						p = p + 8;
						psub = 0;
					}

					tm0.seek(p);
					byte tmp = tm0.readByte();
					//System.out.printf("%02x ",tmp);
					Memory.setPhyMemory1(phyAddr + i, tmp);
					p++;
					psub++;
				}
				//System.out.printf("\nend_p=%x\n", p);

				tm0.close();

			} catch (IOException e) {
			}


			MTC = Util.clearBit(MTC, 0);
			MTC = Util.setBit(MTC, 7);
			MTS = Util.setBit(MTS, 0);

		}

		if(MTC << 28 >>> 29 == 7){

			/*
			System.out.print("\nTM11-Rewind ");
			System.out.printf("MTS=%x ", MTS);
			System.out.printf("MTC=%x ", MTC);
			System.out.printf("MTBRC=%x ", MTBRC);
			System.out.printf("MTCMA=%x ", MTCMA);
			System.out.printf("MTD=%x ", MTD);
			System.out.printf("MTRD=%x \n", MTRD);
			*/

			p = 0;
			psub = 0;

			MTC = Util.clearBit(MTC, 0);
			MTC = Util.setBit(MTC, 7);
			MTS = Util.setBit(MTS, 0);

		}



		/*
		if(Util.checkBit(RKCS, 6) == 1){
			BR_PRI = 5;
			BR_VEC = 0220;
		}
		*/

	}

	static int boot_rom[] = {
			//0046524,                        /* boot_start: "TM" */
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

	static int boot2_rom[] = {
			0046524,                        /* boot_start: "TM" */
			0012706, BOOT_START,            /* mov #boot_start, sp */
			0012700, 0000000,               /* mov #unit_num, r0 */
			0012701, 0172526,               /* mov #172526, r1      ; mtcma */
			0005011,                        /* clr (r1) */
			0012741, 0177777,               /* mov #-1, -(r1)       ; mtbrc */
			0010002,                        /* mov r0,r2 */
			0000302,                        /* swab r2 */
			0062702, 0060011,               /* add #60011, r2 */
			0010241,                        /* mov r2, -(r1)        ; space + go */
			0105711,                        /* tstb (r1)            ; mtc */
			0100376,                        /* bpl .-2 */
			0010002,                        /* mov r0,r2 */
			0000302,                        /* swab r2 */
			0062702, 0060003,               /* add #60003, r2 */
			0010211,                        /* mov r2, (r1)         ; read + go */
			0105711,                        /* tstb (r1)            ; mtc */
			0100376,                        /* bpl .-2 */
			0005002,                        /* clr r2 */
			0005003,                        /* clr r3 */
			0012704, BOOT_START+020,        /* mov #boot_start+20, r4 */
			0005005,                        /* clr r5 */
			0005007                         /* clr r7 */
	};

	/*
	static int tape_boot[] = {
		0012700, //mov #MTCMA, r0
		0172526, //
		0010040, //mov r0, -(r0)  #MTCMA->MTBRC
		0012740, //mov #060003, -(r0)  #060003->MTC
		0060003, //
		0000777, //br $-2 �������[�v
	    };
	*/
}
