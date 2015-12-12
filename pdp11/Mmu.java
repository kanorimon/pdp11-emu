package pdp11;

public class Mmu {
  /*
   * 制御レジスタ
   */ 
  static int SR0;
  static int SR2;

  /*
   * 制御レジスタビット
   */
  static final int SR0_PAGEERROR = 15;
  static final int SR0_PDRERROR = 14;
  static final int SR0_READONLY = 13;
  
  static void reset(){
    SR0 = 0;
    SR2 = 0;
  }
  
  static int analyzeMemory(int addr, int mode) throws MemoryRangeException{
    if(mode == 0){
      addr = analyzeMemoryKernel(addr);
    }else{
      addr = analyzeMemoryUser(addr);
    }
    return addr;
  }
  
  static int analyzeMemoryKernel(int addr) throws MemoryRangeException{
    //MMUがONになっている場合
    if((SR0 & 1) == 1){
      int par = getPAR(addr);
      int blockno = addr << 19 >>> 25 << 6;
      int offset = addr << 26 >>> 26;
    
      int baseblockno = Register.getKernelBaseBlockNo(par)  << 6;
      int blockcnt = (Register.getKernelBlockCnt(par) + 1) << 6 ;

      //アクセスコントロールが00（割り当てなし）の場合エラー
      if(Util.checkBit(Register.kernelPDR[par],1) == 0 && Util.checkBit(Register.kernelPDR[par],2) == 0) {
        Util.setBit(SR0, SR0_PAGEERROR);
        SR2 = Cpu.instAddr;
        if(Pdp11.flgExeMode || Pdp11.flgTapeMode) throw new MemoryRangeException();
      }
      
      //アクセスコントロールが00（割り当てなし）以外の場合
      //アドレスを上位から下位方向に割り当てる
      if(Util.checkBit(Register.kernelPDR[par],3) == 1){
        //割り当て外にアクセスしようとした場合エラー
        if(blockcnt > blockno+offset){
          Util.setBit(SR0, SR0_PDRERROR);
          SR2 = Cpu.instAddr;
          if(Pdp11.flgExeMode || Pdp11.flgTapeMode) throw new MemoryRangeException();
        }
      //アドレスを下位から上位方向に割り当てる
      }else{
        //割り当て外にアクセスしようとした場合エラー
        if(blockcnt <= blockno+offset){
          Util.setBit(SR0, SR0_PDRERROR);
          SR2 = Cpu.instAddr;
          if(Pdp11.flgExeMode || Pdp11.flgTapeMode) throw new MemoryRangeException();
        }
      }

      return baseblockno + blockno + offset;

    //MMUがOFFになっている場合（0170000以降のアドレスはメモリマップドIOの対象になる）
    }else if(addr >= Memory.IOADDRV){
      return addr - Memory.IOADDRV + Memory.IOADDRP;
    }else{
      return addr;
    }
  }
  
  static int analyzeMemoryUser(int addr) throws MemoryRangeException{
    //MMUがONになっている場合
    if((SR0 & 1) == 1){
      int par = getPAR(addr);
      int blockno = addr << 19 >>> 25 << 6;
      int offset = addr << 26 >>> 26;
      
      int baseblockno = Register.getUserBaseBlockNo(par)  << 6;
      int blockcnt = (Register.getUserBlockCnt(par) + 1) << 6 ;

      //アクセスコントロールが00（割り当てなし）の場合エラー
      if(Util.checkBit(Register.userPDR[par],1) == 0 && Util.checkBit(Register.userPDR[par],2) == 0) {
        Util.setBit(SR0, SR0_PAGEERROR);
        SR2 = Cpu.instAddr;
        if(Pdp11.flgExeMode || Pdp11.flgTapeMode) throw new MemoryRangeException();
      }
      
      //アクセスコントロールが00（割り当てなし）以外の場合
      //アドレスを上位から下位方向に割り当てる
      if(Util.checkBit(Register.userPDR[par],3) == 1){
        //割り当て外にアクセスしようとした場合エラー
        if(blockcnt > blockno+offset){
          Util.setBit(SR0, SR0_PDRERROR);
          SR2 = Cpu.instAddr;
          if(Pdp11.flgExeMode || Pdp11.flgTapeMode) throw new MemoryRangeException();
        }
      //アドレスを下位から上位方向に割り当てる
      }else{
        //割り当て外にアクセスしようとした場合エラー
        if(blockcnt <= blockno+offset){
          Util.setBit(SR0, SR0_PDRERROR);
          SR2 = Cpu.instAddr;
          if(Pdp11.flgExeMode || Pdp11.flgTapeMode) throw new MemoryRangeException();
        }
      }

      return baseblockno + blockno + offset;
    }else{
      return addr;
    }
  }
  
  static int getPAR(int addr){
    return addr << 16 >>> 29;
  }
}
