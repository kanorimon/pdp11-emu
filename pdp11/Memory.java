package pdp11;

import java.util.Arrays;

public class Memory {

  /*
   * 物理メモリ
   */
  static byte[] mem;

  final static int MEMORY_SIZE = 0760000;
  final static int HEADER_SIZE = 16;

  /*
   * メモリマップドIO
   */
  final static int IOADDRV = 0160000;
  final static int IOADDRP = 0760000;
  
  final static int PSW = 0777776;
  final static int STACK_LIMIT = 0777774;
  final static int PIRQ = 0777772;
  final static int PB = 0777770;

  final static int CPUERR = 0777766;

  final static int UISA7 = 0777656;
  final static int UISA6 = 0777654;
  final static int UISA5 = 0777652;
  final static int UISA4 = 0777650;
  final static int UISA3 = 0777646;
  final static int UISA2 = 0777644;
  final static int UISA1 = 0777642;
  final static int UISA0 = 0777640;
  final static int UISD7 = 0777616;
  final static int UISD6 = 0777614;
  final static int UISD5 = 0777612;
  final static int UISD4 = 0777610;
  final static int UISD3 = 0777606;
  final static int UISD2 = 0777604;
  final static int UISD1 = 0777602;
  final static int UISD0 = 0777600;
  
  final static int SSR2 = 0777576;
  final static int SSR0 = 0777572;
  
  final static int DISPLAY_REG = 0777570;
  final static int XBUF = 0777566;
  final static int XCSR = 0777564;
  final static int RBUF = 0777562;
  final static int RCSR = 0777560;
  
  final static int CLOCK1 = 0777546;
  
  final static int RKDB = 0777416;
  final static int RKMR = 0777414;
  final static int RKDA = 0777412;
  final static int RKBA = 0777410;
  final static int RKWC = 0777406;
  final static int RKCS = 0777404;
  final static int RKER = 0777402;
  final static int RKDS = 0777400;
  
  final static int MTRD = 0772532;
  final static int MTD = 0772530;
  final static int MTCMA = 0772526;
  final static int MTBRC = 0772524;
  final static int MTC = 0772522;
  final static int MTS = 0772520;
  
  final static int KISA7 = 0772356;
  final static int KISA6 = 0772354;
  final static int KISA5 = 0772352;
  final static int KISA4 = 0772350;
  final static int KISA3 = 0772346;
  final static int KISA2 = 0772344;
  final static int KISA1 = 0772342;
  final static int KISA0 = 0772340;
  final static int KISD7 = 0772316;
  final static int KISD6 = 0772314;
  final static int KISD5 = 0772312;
  final static int KISD4 = 0772310;
  final static int KISD3 = 0772306;
  final static int KISD2 = 0772304;
  final static int KISD1 = 0772302;
  final static int KISD0 = 0772300;

  static void reset(){
    mem = new byte[MEMORY_SIZE];
    Arrays.fill(mem, (byte)0);
  }

  /*
   * メモリにROMをロード
   */
  static void load(int[] rom,int startNo){
    for(int i=0;i<rom.length;i++){
      try {
        setPhyMemory2(startNo + i * 2, rom[i]);
      } catch (MemoryUndefinedException e) {
        e.printStackTrace();
      }
    }
  }
  static void load(byte[] rom,int startNo){
    for(int i=0;i<rom.length;i++){
      try {
        setPhyMemory1(startNo + i, rom[i]);
      } catch (MemoryUndefinedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }
  
  /*
   * メモリにバイナリファイルをロード
   */
  static void fileload(byte[] bf){
    //マジックナンバーを取得
    int magicNo = ((int)bf[0] & 0xFF)|(((int)bf[1] & 0xFF) << 8);

    //サイズを取得
    int textSize = ((int)bf[2] & 0xFF)|(((int)bf[3] & 0xFF) << 8);
    int dataSize = ((int)bf[4] & 0xFF)|(((int)bf[5] & 0xFF) << 8);
    
    //メモリ初期化
    int i;
    int cnt = 0;
    
    //テキスト領域読み込み
    for(i=HEADER_SIZE;i<HEADER_SIZE+textSize;i++){
      mem[cnt] = bf[i];
      cnt++;
    }
    
    //マジックナンバー410対応
    if(magicNo==0x108){
      while(true){
        if(cnt%0x2000==0) break;
        mem[cnt] = 0;
        cnt++;
      }
    }

    //データ領域読み込み
    for(;i<HEADER_SIZE+textSize+dataSize;i++){
      mem[cnt] = bf[i];
      cnt++;
    }
  }
  
  
  /*
   * 2バイト単位でリトルエンディアンを反転して指定箇所のメモリを取得
   */
  static int getPhyMemory2(int addr) throws MemoryUndefinedException{
    
    if(addr >= IOADDRP){
      switch(addr){
      case KISD0:
        return Register.kernelPDR[0];
      case KISD1:
        return Register.kernelPDR[1];
      case KISD2:
        return Register.kernelPDR[2];
      case KISD3:
        return Register.kernelPDR[3];
      case KISD4:
        return Register.kernelPDR[4];
      case KISD5:
        return Register.kernelPDR[5];
      case KISD6:
        return Register.kernelPDR[6];
      case KISD7:
        return Register.kernelPDR[7];
      case KISA0:
        return Register.kernelPAR[0];
      case KISA1:
        return Register.kernelPAR[1];
      case KISA2:
        return Register.kernelPAR[2];
      case KISA3:
        return Register.kernelPAR[3];
      case KISA4:
        return Register.kernelPAR[4];
      case KISA5:
        return Register.kernelPAR[5];
      case KISA6:
        return Register.kernelPAR[6];
      case KISA7:
        return Register.kernelPAR[7];
      case SSR2:
        return Mmu.SR2;
      case SSR0:
        return Mmu.SR0;
      case UISD0:
        return Register.userPDR[0];
      case UISD1:
        return Register.userPDR[1];
      case UISD2:
        return Register.userPDR[2];
      case UISD3:
        return Register.userPDR[3];
      case UISD4:
        return Register.userPDR[4];
      case UISD5:
        return Register.userPDR[5];
      case UISD6:
        return Register.userPDR[6];
      case UISD7:
        return Register.userPDR[7];
      case UISA0:
        return Register.userPAR[0];
      case UISA1:
        return Register.userPAR[1];
      case UISA2:
        return Register.userPAR[2];
      case UISA3:
        return Register.userPAR[3];
      case UISA4:
        return Register.userPAR[4];
      case UISA5:
        return Register.userPAR[5];
      case UISA6:
        return Register.userPAR[6];
      case UISA7:
        return Register.userPAR[7];
      case RCSR:
        return Kl11.RCSR;
      case RBUF:
        return Kl11.getRBUF();
      case XCSR:
        return Kl11.XCSR;
      case XBUF:
        return Kl11.XBUF;
      case DISPLAY_REG:
        return Kl11.consoleSwitchRegister;
      case CPUERR:
        return Register.CPUERR;
      case PB:
        return Register.PB;
      case PIRQ:
        return Register.PIRQ;
      case STACK_LIMIT:
        return Register.STACK_LIMIT;
      case PSW:
        return Register.PSW;
      case CLOCK1:
        return Register.CLOCK1;
      case MTRD:
        return Tm11.MTRD;
      case MTD:
        return Tm11.MTD;
      case MTCMA:
        return Tm11.MTCMA;
      case MTBRC:
        return Tm11.MTBRC;
      case MTC:
        return Tm11.MTC;
      case MTS:
        return Tm11.MTS;
      case RKDS:
        return Rk11.RKDS;
      case RKER:
        return Rk11.RKER;
      case RKCS:
        return Rk11.RKCS;
      case RKWC:
        return Rk11.RKWC;
      case RKBA:
        return Rk11.RKBA;
      case RKDA:
        return Rk11.RKDA;
      case RKMR:
        return Rk11.RKMR;
      case RKDB:
        return Rk11.RKDB;
      default:
        throw new MemoryUndefinedException();
      }
    }else{
      return  ((int)(mem[addr]) & 0xFF) |  ((mem[addr+1] & 0xFF) << 8);
    }
  }

  /*
   * 1バイト単位で指定箇所のメモリを取得
   */
  static int getPhyMemory1(int addr) throws MemoryUndefinedException{
    if(addr >= IOADDRP){
      return getPhyMemory2(addr) & 0xff;
    }else{
      return mem[addr];
    }
  }
  
  /*
   * 2バイト単位でリトルエンディアンに変換して指定箇所のメモリを更新
   */
  static void setPhyMemory2(int addr, int src) throws MemoryUndefinedException{

    if(addr >= IOADDRP){
      switch(addr){
      case KISD0:
        Register.kernelPDR[0] = src;
        break;
      case KISD1:
        Register.kernelPDR[1] = src;
        break;
      case KISD2:
        Register.kernelPDR[2] = src;
        break;
      case KISD3:
        Register.kernelPDR[3] = src;
        break;
      case KISD4:
        Register.kernelPDR[4] = src;
        break;
      case KISD5:
        Register.kernelPDR[5] = src;
        break;
      case KISD6:
        Register.kernelPDR[6] = src;
        break;
      case KISD7:
        Register.kernelPDR[7] = src;
        break;
      case KISA0:
        Register.kernelPAR[0] = src;
        break;
      case KISA1:
        Register.kernelPAR[1] = src;
        break;
      case KISA2:
        Register.kernelPAR[2] = src;
        break;
      case KISA3:
        Register.kernelPAR[3] = src;
        break;
      case KISA4:
        Register.kernelPAR[4] = src;
        break;
      case KISA5:
        Register.kernelPAR[5] = src;
        break;
      case KISA6:
        Register.kernelPAR[6] = src;
        break;
      case KISA7:
        Register.kernelPAR[7] = src;
        break;
      case SSR2:
        Mmu.SR2 = src;
        break;
      case SSR0:
        Mmu.SR0 = src;
        break;
      case UISD0:
        Register.userPDR[0] = src;
        break;
      case UISD1:
        Register.userPDR[1] = src;
        break;
      case UISD2:
        Register.userPDR[2] = src;
        break;
      case UISD3:
        Register.userPDR[3] = src;
        break;
      case UISD4:
        Register.userPDR[4] = src;
        break;
      case UISD5:
        Register.userPDR[5] = src;
        break;
      case UISD6:
        Register.userPDR[6] = src;
        break;
      case UISD7:
        Register.userPDR[7] = src;
        break;
      case UISA0:
        Register.userPAR[0] = src;
        break;
      case UISA1:
        Register.userPAR[1] = src;
        break;
      case UISA2:
        Register.userPAR[2] = src;
        break;
      case UISA3:
        Register.userPAR[3] = src;
        break;
      case UISA4:
        Register.userPAR[4] = src;
        break;
      case UISA5:
        Register.userPAR[5] = src;
        break;
      case UISA6:
        Register.userPAR[6] = src;
        break;
      case UISA7:
        Register.userPAR[7] = src;
        break;
      case RCSR:
        Kl11.setRCSR(src);
        break;
      case RBUF:
        Kl11.RBUF = src;
        break;
      case XCSR:
        Kl11.setXCSR(src);
        break;
      case XBUF:
        Kl11.setXBUF(src);
        break;
      case DISPLAY_REG:
        Kl11.consoleSwitchRegister = src;
        break;
      case CPUERR:
        Register.CPUERR = 0;
        break;
      case PB:
        Register.PB = src;
        break;
      case PIRQ:
        Register.PIRQ = src;
        break;
      case STACK_LIMIT:
        Register.STACK_LIMIT = src;
        break;
      case PSW:
        Register.PSW = src;
        break;
      case CLOCK1:
        Register.CLOCK1 = src;
        break;
      case MTRD:
        Tm11.MTRD = src;
        break;
      case MTD:
        Tm11.MTD = src;
        break;
      case MTCMA:
        Tm11.MTCMA = src;
        break;
      case MTBRC:
        Tm11.MTBRC = src;
        break;
      case MTC:
        Tm11.MTC = src;
        break;
      case MTS:
        Tm11.MTS = src;
        break;
      case RKDS:
        Rk11.RKDS = src;
        break;
      case RKER:
        Rk11.RKER = src;
        break;
      case RKCS:
        Rk11.RKCS = src;
        break;
      case RKWC:
        Rk11.RKWC = src;
        break;
      case RKBA:
        Rk11.RKBA = src;
        break;
      case RKDA:
        Rk11.RKDA = src;
        break;
      case RKMR:
        Rk11.RKMR = src;
        break;
      case RKDB:
        Rk11.RKDB = src;
        break;
      default:
        throw new MemoryUndefinedException();
      }
    }else{
      mem[addr] = (byte)src;
      mem[addr+1] = (byte)(src >> 8);
    }
  }

  /*
   * 1バイト単位で指定箇所のメモリを更新
   */
  static void setPhyMemory1(int addr, int src) throws MemoryUndefinedException{
    if(addr >= IOADDRP){
      setPhyMemory2(addr, (src & 0xFF) |  (getPhyMemory2(addr) & 0xFF00));
    }else{
      mem[addr] = (byte)src;
    }
  }
}

class MemoryException extends Exception{
  public MemoryException(String string) {
    super(string);
  }
}

class MemoryUndefinedException extends MemoryException{
  public MemoryUndefinedException() {
    super("未定義領域にアクセスしました");
  }
}

class MemoryRangeException extends MemoryException{
  public MemoryRangeException() {
    super("アクセス不可領域にアクセスしました");
  }
}
