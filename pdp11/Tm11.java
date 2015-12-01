package pdp11;

public class Tm11 {
	
	static int MTRD;
	static int MTD;
	static int MTCMA;
	static int MTBRC;
	static int MTC;
	static int MTS;

	static final int BOOT_START = 016000; //BOOT_ROMの読込先アドレス

	static void reset(){
		MTRD = 0;
		MTD = 0;
		MTCMA = 0;
		MTBRC = 0172526;
		MTC = 060003;
		MTS = 0;
	}

	static int boot_rom[] = {
			0046524,                        /* boot_start: "TM" */
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
	static int tape_boot[] = {
		0012700, //mov #MTCMA, r0
		0172526, //
		0010040, //mov r0, -(r0)  #MTCMA->MTBRC
		0012740, //mov #060003, -(r0)  #060003->MTC
		0060003, //
		0000777, //br $-2 無限ループ
	    };
	*/
}
