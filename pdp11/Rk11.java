package pdp11;

import java.io.IOException;
import java.io.RandomAccessFile;

public class Rk11 {

  /*
   * 制御レジスタ
   */
  static int RKDS; //RK11 Drive Status Register
  static int RKER; //RK11 Error Register
  static int RKCS; //RK11 Control Status Register
  static int RKWC; //RK11 Word Count Register
  static int RKBA; //RK11 Bus Address Register
  static int RKDA; //RK11 Disk Address Register
  static int RKMR; //RK11 Maintenance Register
  static int RKDB; //RK11 Data Buffer Register

  /*
   * 制御レジスタビット
   */
  static final int RKCS_ERROR = 15;
  static final int RKCS_HERROR = 14;
  static final int RKCS_READY = 7;
  static final int RKCS_INTERRUPT = 6;
  static final int RKCS_GO = 0;
  
  /*
   * 割り込み設定
   */
  static int BR_PRI; //割り込み優先度
  static int BR_VEC; //割り込みベクタ
  
  /*
   * BOOTROM
   */
  static final int BOOT_START = 02000; //BOOT_ROMの読込先アドレス
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
    RKCS = Util.clearBit(RKCS, RKCS_GO);

    //READ
    if(RKCS << 28 >>> 29 == 1){

      int datasizeWord = ~((RKWC & 0xFFFF) - 1 - 65535) + 1;
      int tmpRKDA = (((((RKDA & 0x1FE0) >>> 5) << 1) | ((RKDA &0x10) >>> 4)) * 12) + (RKDA & 0xF);

      try {
        String driveName = "";
        
        switch((RKDA & 0xE000) >>> 13){
        case 0:
          driveName = Pdp11.RK0;
          break;
        case 1:
          driveName = Pdp11.RK1;
          break;
        case 2:
          driveName = Pdp11.RK2;
          break;
        }
        
        RandomAccessFile v6root = new RandomAccessFile( System.getProperty("user.dir") + "\\" + driveName, "rw");
        v6root.seek(tmpRKDA * 512);

        int phyAddr = ((RKCS & 0x30) << 12) + (RKBA & 0xFFFF);
        for(int i=0;i<datasizeWord * 2; i++){
          try {
            v6root.write(Memory.getPhyMemory1(phyAddr + i));
          } catch (MemoryUndefinedException e) {
            e.printStackTrace();
          }
        }
        
        v6root.close();
        
      } catch (IOException e) {
      }

      RKCS = Util.setBit(RKCS, RKCS_READY);
    }

    //WRITE
    if(RKCS << 28 >>> 29 == 2){

      int datasizeWord = ~((RKWC & 0xFFFF) - 1 - 65535) + 1;
      int tmpRKDA = (((((RKDA & 0x1FE0) >>> 5) << 1) | ((RKDA &0x10) >>> 4)) * 12) + (RKDA & 0xF);

      try {
        String driveName = "";
        
        switch((RKDA & 0xE000) >>> 13){
        case 0:
          driveName = Pdp11.RK0;
          break;
        case 1:
          driveName = Pdp11.RK1;
          break;
        case 2:
          driveName = Pdp11.RK2;
          break;
        }

        RandomAccessFile v6root = new RandomAccessFile( System.getProperty("user.dir") + "\\" +  driveName, "r");
        v6root.seek(tmpRKDA * 512);

        int phyAddr = ((RKCS & 0x30) << 12) + (RKBA & 0xFFFF);
        for(int i=0;i<datasizeWord * 2; i++){
          byte tmp = v6root.readByte();
          try {
            Memory.setPhyMemory1(phyAddr + i, tmp);
          } catch (MemoryUndefinedException e) {
            e.printStackTrace();
          }
        }

        v6root.close();
        
      } catch (IOException e) {
      }

      RKCS = Util.setBit(RKCS, RKCS_READY);
    }

    if(Util.checkBit(RKCS, RKCS_INTERRUPT) == 1){
      BR_PRI = 5;
      BR_VEC = 0220;
    }
  }
  
  static void rk11error(){
    RKCS = Util.setBit(RKCS, RKCS_ERROR);
    if((RKER << 16 >>> 21) != 0){
      RKCS = Util.setBit(RKCS, RKCS_HERROR);
    }
    
    if(Util.checkBit(RKCS, RKCS_INTERRUPT) == 1){
      BR_PRI = 5;
      BR_VEC = 0220;
    }
  }
}
