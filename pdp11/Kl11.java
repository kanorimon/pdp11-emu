package pdp11;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Kl11{
  /*
   * 制御レジスタ
   */
  static int consoleSwitchRegister; //コンソールスイッチレジスタ
  static int XBUF; //出力バッファ
  static int XCSR; //出力レジスタ
  static int RBUF; //入力バッファ
  static int RCSR; //入力レジスタ

  /*
   * 制御レジスタビット
   */
  static final int XCSR_READY = 7;
  static final int XCSR_ID = 6;
  static final int RCSR_BUSY = 11;
  static final int RCSR_DONE = 7;
  static final int RCSR_ID = 6;
  static final int RCSR_ENB = 0;

  /*
   * 割り込み設定
   */
  static int BR_PRI; //割り込み優先度
  static int BR_VEC; //割り込みベクタ

  static BufferedReader reader;

  static void reset(){
    consoleSwitchRegister = 1;
    XBUF = 0;
    XCSR = 128;
    RBUF = 0;
    RCSR = 0;

    BR_PRI = 0;
    BR_VEC = 0;

    reader = new BufferedReader(new InputStreamReader(System.in));
  }

  static void setRCSR(int rcsr){
    RCSR = rcsr;
    if (Util.checkBit(RCSR, RCSR_ENB) == 1)
      RCSR = Util.clearBit(RCSR, RCSR_DONE);

    if (Util.checkBit(RCSR, RCSR_ID) == 1 && Util.checkBit(RCSR, RCSR_DONE) == 1) {
      BR_PRI = 4;
      BR_VEC = 060;
    }
  }

  static int getRBUF(){
    if(RBUF == 013) RBUF = 04;
    RCSR = Util.clearBit(RCSR,RCSR_DONE);
    return RBUF;
  }

  static void setXCSR(int xcsr){
    XCSR = xcsr;
  }

  static void setXBUF(int xbuf){
    XBUF = xbuf & 0x7F;
    switch(XBUF){
    case 004:
      System.out.print("");
      break;
    case 010:
      System.out.print("");
      break;
    case 011:
      System.out.print("\t");
      break;
    case 012:
      System.out.print("\n");
      break;
    case 014:
      System.out.print("\n");
      break;
    case 015:
      System.out.print("\r");
      break;
    case 034:
      System.out.print("");
      break;
    case 040:
      System.out.print(" ");
      break;
    case 0177:
      System.out.print("");
      break;
    default:
      System.out.printf("%c",XBUF);
    }
    XBUF = 0;
    XCSR = Util.setBit(XCSR, XCSR_READY);

    if (Util.checkBit(XCSR, XCSR_ID) == 1 && Util.checkBit(XCSR, XCSR_READY) == 1) {
      BR_PRI = 4;
      BR_VEC = 064;
    }
  }

  static void kl11access(){
    RCSR = Util.setBit(RCSR, RCSR_BUSY);

    try {
      if(reader.ready()){
        RBUF = reader.read();

        //CTRL+Eでエミュレータを終了
        if(RBUF == 5){
        	System.exit(0);
        }
        
        if(RBUF == 0xa){
          RBUF = 0;
          RCSR = Util.clearBit(RCSR, RCSR_BUSY);
        }else {
          RCSR = Util.setBit(RCSR, RCSR_DONE);
          RCSR = Util.clearBit(RCSR, RCSR_BUSY);
        }

        if (Util.checkBit(RCSR, RCSR_ID) == 1 && Util.checkBit(RCSR, RCSR_DONE) == 1) {
          BR_PRI = 4;
          BR_VEC = 060;
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
