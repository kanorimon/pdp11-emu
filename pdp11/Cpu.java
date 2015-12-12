package pdp11;

import java.util.ArrayList;

public class Cpu {

  /*
   * 変数定義（実行用）
   */
  int exeCnt;                 //実行回数
  static int instAddr;        //実行中の命令のアドレス
  boolean waitFlg;            //WAIT false:WAITしていない true:WAITしている

  /*
   * 変数定義（デバッグ用）
   */
  ArrayList<Integer> dbgList; //シンボル出力（呼び先）
  ArrayList<Integer> rtnList; //シンボル出力（戻り先）
  int printCnt;               //0000番地の命令をSTART_CNT以上実行した時にダンプを出力する
  int START_CNT;              //0000番地の命令をSTART_CNT以上実行した時にダンプを出力する

  /*
   * 変数定義（逆アセンブラ用）
   */
  int strnum;                 //出力時の空白制御

  Cpu(){
    /*
     * 変数初期化
     */
    exeCnt = 0;
    instAddr = 0;
    waitFlg = false;

    dbgList = new ArrayList<>();
    rtnList = new ArrayList<>();
    printCnt = 0;
    START_CNT = 0;
  }

  /*
   * CPUエミュレータ実行
   */
  public void execute(){
    /*
     * 変数定義
     */
    int tmp = 0;                          //一時保存領域
    int fetchedMem;                       //フェッチしたメモリの内容
    Opcode opcode;                        //オペコード
    Operand srcOperand = new Operand();   //srcオペランド
    Operand dstOperand = new Operand();   //dstオペランド
    int srcValue;                         //srcの値
    int dstValue;                         //dstの値

    for(;;){
      //実行回数をインクリメント
      exeCnt++;

      /*
      * KL11
       */
      if (exeCnt % 1000 == 500 && Util.checkBit(Kl11.RCSR,Kl11.RCSR_DONE) == 0) {
        Kl11.kl11access();
      }

      /*
      * RK11
      */
      //ディスクアクセス
      if (Util.checkBit(Rk11.RKCS, Rk11.RKCS_GO) == 1) {
        Rk11.rk11access();
      }
      //エラー発生時
      if (Rk11.RKER != 0) Rk11.rk11error();

      /*
      * TM11
      */
      //テープアクセス
      if (Util.checkBit(Tm11.MTC, Tm11.MTC_GO) == 1) {
        Tm11.tm11access();
      }

      /*
       * CLOCK
       */
      if (exeCnt % 1000 == 0) {
        Register.CLOCK1 = Util.setBit(Register.CLOCK1, 7);
      } else {
        Register.CLOCK1 = Util.clearBit(Register.CLOCK1, 7);
      }

      /*
       * 割り込み
       */
      //クロック割り込み
      if (Util.checkBit(Register.CLOCK1, 7) == 1 && Util.checkBit(Register.CLOCK1, 6) == 1) {
        if (7 > Register.getPriority()) {
          trap(0100, 0102);
          waitFlg = false;
        }
      //RK11割り込み
      } else if (Rk11.BR_PRI > Register.getPriority()) {
        trap(Rk11.BR_VEC, Rk11.BR_VEC + 2);
        Rk11.BR_PRI = 0;
        waitFlg = false;
      //TM11割り込み
      } else if (Tm11.BR_PRI > Register.getPriority()) {
        trap(Tm11.BR_VEC, Tm11.BR_VEC + 2);
        Tm11.BR_PRI = 0;
        waitFlg = false;
      //KL11割り込み
      } else if (Kl11.BR_PRI > Register.getPriority()) {
        trap(Kl11.BR_VEC, Kl11.BR_VEC + 2);
        Kl11.BR_PRI = 0;
        waitFlg = false;
      }

      /*
       * デバッグ用出力
       */
      if(Register.get(7) == 0) printCnt++;
      //シンボルをPOP
      if(Pdp11.flgDebugMode>=1) popCall(Register.get(7));
      //レジスタ・フラグを出力
      if(Pdp11.flgDebugMode>1) printDebug();

      /*
       * 命令解釈・実行
       */
      try{
        //WAIT中は次の命令の取得はせず、WAIT命令を繰り返す
        if(!waitFlg){
          //Fetch
          fetchedMem = fetchMemory();
          
          //Decode
          opcode = getOpcode(fetchedMem);
  
          //Execute
          switch(opcode){
          case ADC:
            //add carry
            dstOperand = getOperand(dstOperand,(fetchedMem >> 3) & 7,fetchedMem & 7);
            dstValue = dstOperand.getValue();

            int adctmp = 0;
            if(Register.getC()) adctmp = 1;

            tmp = dstValue + adctmp;

            Register.setCC( (tmp << 16 >> 16) < 0, 
                            (tmp << 16 >> 16) == 0, 
                            ((dstValue << 16 >>> 16 == 077777) && adctmp == 1), 
                            ((dstValue << 16 >>> 16 == 0177777) && adctmp == 1));

            if(dstOperand.flgRegister){
              Register.set(dstOperand.register, tmp);
            }else if(dstOperand.flgAddress){
              setMemory2(dstOperand.address, tmp);
            }else{
              setMemory2(dstOperand.immediate, tmp);
            }

            break;
          case ADD:
            //add src to dst
            srcOperand = getOperand(srcOperand,(fetchedMem >> 9) & 7,(fetchedMem >> 6) & 7);
            dstOperand = getOperand(dstOperand,(fetchedMem >> 3) & 7,fetchedMem  & 7);
            srcValue = srcOperand.getValue();
            dstValue = dstOperand.getValue();

            tmp = srcValue + dstValue;

            Register.setCC( (tmp << 16 >> 16) < 0, 
                            (tmp << 16 >> 16) == 0,
                            getOverflow(srcValue, dstValue, tmp, 16),
                            getCarry(srcValue, dstValue, tmp, 16));

            if(dstOperand.flgRegister){
              Register.set(dstOperand.register, tmp);
            }else{
              setMemory2(dstOperand.address, tmp);
            }

            break;
          case ASH:
            //shift arithmetically
            int ashReg = Register.get((fetchedMem >> 6) & 7);
            srcOperand = getOperand(srcOperand,(fetchedMem >> 3) & 7,fetchedMem  & 7);
            srcValue = srcOperand.getValue();

            int ashInt = srcValue << 26 >> 26;

            if(ashInt < 0){
              tmp = ashReg >> Math.abs(ashInt);
            }else{
              tmp = ashReg << ashInt;
              if(ashInt == 0){
                Register.setC(false);
              }else{
                Register.setC(((ashReg << (ashInt - 1)) & 0x8000) != 0);
              }
            }

            Register.setCC( (tmp << 16 >> 16) < 0,
                            (tmp << 16 >> 16) == 0, 
                            (ashReg << 16 >>> 31) != (tmp << 16 >>> 31),
                            Register.getC());

            Register.set((fetchedMem >> 6) & 7, tmp);

            break;
          case ASHC:
            //arithmetic shift combined
            int ashcReg1 = Register.get((fetchedMem >> 6) & 7);
            int ashcReg2 = Register.get(((fetchedMem >> 6) & 7) + 1);
            int ashcTmp = (ashcReg1 << 16) + (ashcReg2 << 16 >>> 16);

            srcOperand = getOperand(srcOperand,(fetchedMem >> 3) & 7,fetchedMem  & 7);
            srcValue = srcOperand.getValue();

            int ashcInt = srcValue << 26 >> 26;

            if(ashcInt < 0){
              tmp = ashcTmp >> Math.abs(ashcInt);
              Register.setC(((ashcTmp >> (Math.abs(ashcInt) - 1)) & 1) == 1);
            }else{
              tmp = ashcTmp << ashcInt;
              if(ashcInt == 0){
                Register.setC(false);
              }else{
                Register.setC(((ashcTmp << (ashcInt - 1)) & 0x80000000) != 0);
              }
            }

            Register.setCC( tmp < 0,
                            tmp == 0, 
                            (ashcTmp >>> 31) != (tmp  >>> 31),
                            Register.getC());

            Register.set((fetchedMem >> 6) & 7, tmp >>> 16);
            Register.set(((fetchedMem >> 6) & 7)+1, tmp << 16 >>> 16);

            break;
          case ASL:
            //arithmetic shift left
            dstOperand = getOperand(dstOperand,(fetchedMem >> 3) & 7,fetchedMem  & 7);
            dstValue= dstOperand.getValue();

            tmp = dstValue << 1;

            Register.setCC( (tmp << 16 >> 16) < 0,
                            (tmp << 16 >> 16) == 0, 
                            Register.getV(), 
                            (dstValue << 16 >>> 16) < 0);
            Register.setN((Register.getN() || Register.getC()) && (!Register.getN() || !Register.getC()));

            if(dstOperand.flgRegister){
              Register.set(dstOperand.register, tmp);
            }else{
              setMemory2(dstOperand.address, tmp);
            }

            break;
          case ASR:
            //arithmetic shift right
            dstOperand = getOperand(dstOperand,(fetchedMem >> 3) & 7,fetchedMem  & 7);
            dstValue= dstOperand.getValue();

            tmp = (dstValue << 16 >> 16) >> 1;

            Register.setCC( (tmp << 16 >> 16) < 0,
                            (tmp << 16 >> 16) == 0, 
                            Register.getV(), 
                          (dstValue & 1) == 1);
            Register.setN((Register.getN() || Register.getC()) && (!Register.getN() || !Register.getC()));

            if(dstOperand.flgRegister){
              Register.set(dstOperand.register, tmp);
            }else{
              setMemory2(dstOperand.address, tmp);
            }

            break;
          case BCC:
            //branch if carry clear
            if(!Register.getC()) Register.set(7,getOffset(dstOperand,fetchedMem).address);
            break;
          case BCS:
            //branch if carry set
            if(Register.getC()) Register.set(7,getOffset(dstOperand,fetchedMem).address);
            break;
          case BEQ:
            //branch if equal
            if(Register.getZ()) Register.set(7,getOffset(dstOperand,fetchedMem).address);
            break;
          case BGE:
            //branch if greater than or equal
            if(Register.getN() == Register.getV()) 
              Register.set(7,getOffset(dstOperand,fetchedMem).address);
            break;
          case BGT:
            //branch if greater than
            if(!Register.getZ() && Register.getN() == Register.getV()) 
              Register.set(7,getOffset(dstOperand,fetchedMem).address);
            break;
          case BHI:
            //branch if higher
            if(!Register.getC() && !Register.getZ()) 
              Register.set(7,getOffset(dstOperand,fetchedMem).address);
            break;
          case BIC:
            //bit clear
            srcOperand = getOperand(srcOperand,(fetchedMem >> 9) & 7,(fetchedMem >> 6) & 7);
            dstOperand = getOperand(dstOperand,(fetchedMem >> 3) & 7,fetchedMem  & 7);
            srcValue = srcOperand.getValue();
            dstValue = dstOperand.getValue();

            tmp = ~srcValue & dstValue;

            Register.setCC( (tmp << 16 >> 16) < 0,
                            (tmp << 16 >> 16) == 0, 
                            false, 
                            Register.getC());

            if(dstOperand.flgRegister){
              Register.set(dstOperand.register, tmp);
            }else{
              setMemory2(dstOperand.address, tmp);
            }

            break;
          case BICB:
            //bit clear
            srcOperand = getOperand(srcOperand,(fetchedMem >> 9) & 7,(fetchedMem >> 6) & 7,true);
            dstOperand = getOperand(dstOperand,(fetchedMem >> 3) & 7,fetchedMem  & 7,true);
            srcValue = srcOperand.getValue(true);
            dstValue = dstOperand.getValue(true);         

            tmp = (~srcValue & dstValue) << 24 >>> 24;

            Register.setCC( (tmp << 24 >> 24) < 0,
                            (tmp << 24 >> 24) == 0, 
                            false, 
                            Register.getC());

            if(dstOperand.flgRegister){
              Register.set(dstOperand.register, (dstValue & 0xFF00) + tmp);
            }else{
              setMemory1(dstOperand.address, tmp);
            }

            break;
          case BIS:
            //bit set
            srcOperand = getOperand(srcOperand,(fetchedMem >> 9) & 7,(fetchedMem >> 6) & 7);
            dstOperand = getOperand(dstOperand,(fetchedMem >> 3) & 7,fetchedMem  & 7);
            srcValue = srcOperand.getValue();
            dstValue = dstOperand.getValue();

            tmp = srcValue | dstValue;

            Register.setCC( (tmp << 16 >> 16) < 0,
                            (tmp << 16 >> 16) == 0, 
                            false, 
                            Register.getC());

            if(dstOperand.flgRegister){
              Register.set(dstOperand.register, tmp);
            }else if(dstOperand.flgAddress){
              setMemory2(dstOperand.address, tmp);
            }

            break;
          case BISB:
            //bit set
            srcOperand = getOperand(srcOperand,(fetchedMem >> 9) & 7,(fetchedMem >> 6) & 7,true);
            dstOperand = getOperand(dstOperand, (fetchedMem >> 3) & 7, fetchedMem & 7, true);
            srcValue = srcOperand.getValue(true);
            dstValue = dstOperand.getValue(true);

            tmp = (srcValue | dstValue) << 24 >>> 24;

            Register.setCC( (tmp << 24 >> 24) < 0,
                            (tmp << 24 >> 24) == 0, 
                            false, 
                            Register.getC());

            if(dstOperand.flgRegister){
              Register.set(dstOperand.register, (dstValue & 0xFF00) + tmp);
            }else if(dstOperand.flgAddress){
              setMemory1(dstOperand.address, tmp);
            }

            break;
          case BIT:
            //bit test
            srcOperand = getOperand(srcOperand,(fetchedMem >> 9) & 7,(fetchedMem >> 6) & 7);
            dstOperand = getOperand(dstOperand,(fetchedMem >> 3) & 7,fetchedMem  & 7);
            srcValue = srcOperand.getValue();
            dstValue = dstOperand.getValue();

            tmp = srcValue & dstValue;

            Register.setCC( (tmp << 16 >> 16) < 0, 
                            (tmp << 16 >> 16) == 0, 
                            false, 
                            Register.getC());

            break;
          case BITB:
            //bit test
            srcOperand = getOperand(srcOperand,(fetchedMem >> 9) & 7,(fetchedMem >> 6) & 7,true);
            dstOperand = getOperand(dstOperand,(fetchedMem >> 3) & 7,fetchedMem  & 7,true);
            srcValue = srcOperand.getValue(true);
            dstValue = dstOperand.getValue(true);

            tmp = (srcValue & dstValue) << 24 >>> 24;

            Register.setCC( (tmp << 24 >> 24) < 0, 
                            (tmp << 24 >> 24) == 0, 
                            false, 
                            Register.getC());

            break;
          case BLE:
            //branch if less than or equal to
            if(Register.getZ() || Register.getN() != Register.getV()) 
              Register.set(7,getOffset(dstOperand,fetchedMem).address);
            break;
          case BLOS:
            //branch if lower or same
            if(Register.getC() || Register.getZ()) 
              Register.set(7,getOffset(dstOperand,fetchedMem).address);
            break;
          case BLT:
            //branch if less than
            if((Register.getN() || Register.getV()) && (!Register.getN() || !Register.getV())) 
              Register.set(7,getOffset(dstOperand,fetchedMem).address);
            break;
          case BMI:
            //branch in minus
            if(Register.getN()) Register.set(7,getOffset(dstOperand,fetchedMem).address);
            break;
          case BNE:
            //branch if not equal
            if(!Register.getZ()) Register.set(7,getOffset(dstOperand,fetchedMem).address);
            break;
          case BPL:
            //branch if plus
            if(!Register.getN()) Register.set(7,getOffset(dstOperand,fetchedMem).address);
            break;
          case BR:
            //branch
            Register.set(7,getOffset(dstOperand,fetchedMem).address);
            break;
          case BVS:
            //branch if v bit clear
            if(Register.getV()) Register.set(7,getOffset(dstOperand,fetchedMem).address);
            break;
          case CLC:
            //clear c
            Register.setC(false);
            break;
          case CLR:
            //clear
            dstOperand = getOperand(dstOperand,(fetchedMem >> 3) & 7,fetchedMem  & 7);

            Register.setCC(false, true, false, false);

            if(dstOperand.flgRegister){
              Register.set(dstOperand.register, 0);
            }else{
              setMemory2(dstOperand.address,0);
            }

            break;
          case CLRB:
            //clear
            dstOperand = getOperand(dstOperand,(fetchedMem >> 3) & 7,fetchedMem  & 7, true);

            Register.setCC(false, true, false, false);

            if(dstOperand.flgRegister){
              Register.set(dstOperand.register, dstOperand.getValue() & 0xFF00);
            }else{
              setMemory1(dstOperand.address,0);
            }

            break;
          case CMP:
            //compare
            srcOperand = getOperand(srcOperand, (fetchedMem >> 9) & 7, (fetchedMem >> 6) & 7);
            dstOperand = getOperand(dstOperand, (fetchedMem >> 3) & 7, fetchedMem & 7);
            srcValue = srcOperand.getValue();
            dstValue =(~(dstOperand.getValue()) + 1) << 16 >> 16;

            tmp = srcValue + dstValue;

            Register.setCC( (tmp << 16 >> 16) < 0, 
                            (tmp << 16 >> 16) == 0,
                            getOverflow(srcValue, dstValue, tmp, 16),
                            dstValue != 0 && !getCarry(srcValue, dstValue, tmp, 16));

            break;
          case CMPB:
            //compare
            srcOperand = getOperand(srcOperand, (fetchedMem >> 9) & 7, (fetchedMem >> 6) & 7, true);
            dstOperand = getOperand(dstOperand, (fetchedMem >> 3) & 7, fetchedMem & 7, true);
            srcValue = srcOperand.getValue(true) ;
            dstValue = (~(dstOperand.getValue(true)) + 1) << 24 >> 24;

            tmp = srcValue + dstValue;

            Register.setCC( (tmp << 24 >> 24) < 0, 
                            (tmp << 24 >> 24) == 0,
                            getOverflow(srcValue, dstValue, tmp, 24),
                            dstValue != 0 && !getCarry(srcValue, dstValue, tmp, 24));

            break;
          case COM:
            //complement
            dstOperand = getOperand(dstOperand,(fetchedMem >> 3) & 7,fetchedMem  & 7);
            dstValue = dstOperand.getValue();

            Register.setCC( ((~dstValue) << 16 >> 16) < 0,
                            ((~dstValue) << 16 >> 16) == 0, 
                            false, 
                            true);

            if(dstOperand.flgRegister){
              Register.set(dstOperand.register, ~dstValue);
            }else{
              setMemory2(dstOperand.address, ~dstValue);
            }

            break;
          case DEC:
            //decrement
            dstOperand = getOperand(dstOperand, (fetchedMem >> 3) & 7, fetchedMem & 7);
            dstValue = dstOperand.getValue();

            tmp = dstValue - 1;

            Register.setCC( (tmp << 16 >> 16) < 0, 
                            (tmp << 16 >> 16) == 0, 
                            (dstValue & 0xFFFF)  == 0100000,
                            Register.getC());

            if(dstOperand.flgRegister){
              Register.set(dstOperand.register, tmp);
            }else if(dstOperand.flgAddress){
              setMemory2(dstOperand.address, tmp);
            }else{
              setMemory2(dstOperand.immediate, tmp);
            }

            break;
          case DECB:
            //decrement
            dstOperand = getOperand(dstOperand,(fetchedMem >> 3) & 7,fetchedMem  & 7, true);
            dstValue = dstOperand.getValue(true);

            tmp = (dstValue - 1) << 24 >>> 24;

            Register.setCC( (tmp << 24 >> 24) < 0, 
                            (tmp << 24 >> 24) == 0, 
                            (dstValue << 24 >> 24) < 0, 
                            Register.getC());

            if(dstOperand.flgRegister){
              Register.set(dstOperand.register, (dstValue & 0xFF00) + tmp);
            }else if(dstOperand.flgAddress){
              setMemory1(dstOperand.address, tmp);
            }else{
              setMemory1(dstOperand.immediate, tmp);
            }

            break;
          case DIV: 
            //divide
            int divR1 = Register.get((fetchedMem >> 6) & 7) << 16;
            int divR2 = Register.get(((fetchedMem >> 6) & 7)+1);

            int divValue = divR1 + divR2;

            srcOperand = getOperand(srcOperand,(fetchedMem >> 3) & 7,fetchedMem & 7);
            srcValue = srcOperand.getValue();

            if( srcValue == 0 ||
                ((divValue / srcValue) > 0x7fff ||
                (divValue / srcValue) < -0x8000)){
              Register.setCC(false, false, true, true);
              break;
            }

            tmp = divValue / srcValue;

            Register.setCC( (tmp << 16 >> 16) < 0, 
                            (tmp << 16 >> 16) == 0, 
                            false,
                            false);

            Register.set((fetchedMem >> 6) & 7, tmp);
            Register.set(((fetchedMem >> 6) & 7)+1, divValue % srcValue);

            break;
          case HALT:
            //halt
            break;
          case INC:
            //increment
            dstOperand = getOperand(dstOperand,(fetchedMem >> 3) & 7,fetchedMem  & 7);
            dstValue = dstOperand.getValue();

            tmp = dstValue + 1;

            Register.setCC( (tmp << 16 >> 16) < 0, 
                            (tmp << 16 >> 16) == 0, 
                            dstValue == 077777, 
                            Register.getC());

            if(dstOperand.flgRegister){
              Register.set(dstOperand.register, tmp);
            }else if(dstOperand.flgAddress){
              setMemory2(dstOperand.address, tmp);
            }else{
              setMemory2(dstOperand.immediate, tmp);
            }

            break;
          case INCB:
            //increment
            dstOperand = getOperand(dstOperand,(fetchedMem >> 3) & 7,fetchedMem  & 7,true);
            dstValue = dstOperand.getValue(true);

            tmp = (dstValue + 1) << 24 >>> 24;

            Register.setCC( (tmp << 16 >> 16) < 0, 
                            (tmp << 16 >> 16) == 0, 
                            dstValue == 077777, 
                            Register.getC());

            if(dstOperand.flgRegister){
              Register.set(dstOperand.register, (dstValue & 0xFF00) + tmp);
            }else if(dstOperand.flgAddress){
              setMemory1(dstOperand.address, tmp);
            }else{
              setMemory1(dstOperand.immediate, tmp);
            }

            break;
          case JMP:
            //jump
            if(((fetchedMem >> 3) & 7) == 0){
              trap04();
              break;
            }

            dstOperand = getOperand(dstOperand,(fetchedMem >> 3) & 7,fetchedMem  & 7);

            tmp = Register.get(7);

            if(dstOperand.flgRegister){
              Register.set(7,Register.get(dstOperand.register));
            }else if(dstOperand.flgAddress){
              Register.set(7,dstOperand.address);
            }else{
              Register.set(7,dstOperand.immediate);
            }

            if(Pdp11.flgDebugMode>1) {
              pushCall(Register.get(7),tmp);
              printCall();
            }

            break;
          case JSR:
            //jump to subroutine
            if(((fetchedMem >> 3) & 7) == 0){
              trap04();
              break;
            }

            dstOperand = getOperand(dstOperand, (fetchedMem >> 3) & 7, fetchedMem & 7);

            tmp = Register.get(7);

            pushStack(Register.get((fetchedMem >> 6) & 7));
            Register.set((fetchedMem >> 6) & 7,Register.get(7));

            if(dstOperand.flgRegister){
              Register.set(7,Register.get(dstOperand.register));
            }else if(dstOperand.flgAddress){
              Register.set(7,dstOperand.address);
            }else{
              Register.set(7,dstOperand.immediate);
            }

            if(Pdp11.flgDebugMode>1) {
              pushCall(Register.get(7),tmp);
              printCall();
            }

            break;
          case MFPI:
            //move from previous instruction space
            try{
              srcOperand = getOperand(srcOperand,(fetchedMem >> 3) & 7,fetchedMem  & 7, false, Register.getNowMode());
              srcValue = srcOperand.getValue(Register.getPreMode());
            }catch(MemoryUndefinedException e){
              trap04();
              break;
            }

            try{
              pushStack(srcValue);
            }catch(ArrayIndexOutOfBoundsException e){
              trap04();
              break;
            }

            Register.setCC( (srcValue << 16 >> 16) < 0, 
                            (srcValue << 16 >> 16) == 0, 
                            false, 
                            Register.getC());

            break;
          case MOV:
            //move
            srcOperand = getOperand(srcOperand,(fetchedMem >> 9) & 7,(fetchedMem >> 6) & 7);
            dstOperand = getOperand(dstOperand,(fetchedMem >> 3) & 7,fetchedMem  & 7);
            srcValue = srcOperand.getValue();

            Register.setCC( (srcValue << 16 >> 16) < 0, 
                            (srcValue << 16 >> 16) == 0, 
                            false, 
                            Register.getC());

            if(dstOperand.flgRegister){
              Register.set(dstOperand.register, srcValue);
            }else if(dstOperand.flgAddress){
              setMemory2(dstOperand.address, srcValue);
            }

            break;
          case MOVB:
            //move
            srcOperand = getOperand(srcOperand,(fetchedMem >> 9) & 7,(fetchedMem >> 6) & 7, true);
            dstOperand = getOperand(dstOperand,(fetchedMem >> 3) & 7,fetchedMem  & 7, true);
            srcValue = srcOperand.getValue(true);

            Register.setCC( (srcValue << 24 >> 24) < 0, 
                            (srcValue << 24 >> 24) == 0, 
                            false, 
                            Register.getC());

            if(dstOperand.flgRegister){
              //モード0の場合、符号拡張を行う
              Register.set(dstOperand.register, srcValue << 24 >> 24);
            }else if(dstOperand.flgAddress){
              setMemory1(dstOperand.address, srcValue);
            }

            break;
          case MTPI:
            //move　to previous instruction space
            try{
              dstOperand = getOperand(dstOperand, (fetchedMem >> 3) & 7, fetchedMem & 7, false, Register.getPreMode());
              tmp = popStack();

              Register.setCC( (tmp << 16 >> 16) < 0,
                              (tmp << 16 >> 16) == 0,
                              false,
                              Register.getC());

              if(dstOperand.flgRegister){
                Register.set(dstOperand.register, tmp, Register.getPreMode());
              }else if(dstOperand.flgAddress){
                setMemory2(dstOperand.address, tmp, Register.getPreMode());
              }
            }catch(MemoryUndefinedException e){
              pushStack(tmp);
              trap04();
              break;
            }catch(ArrayIndexOutOfBoundsException e){
              e.printStackTrace();
            }

            break;
          case MUL:
            //multiply
            int mulR = Register.get((fetchedMem >> 6) & 7);
            srcOperand = getOperand(srcOperand,(fetchedMem >> 3) & 7,fetchedMem  & 7);
            srcValue = srcOperand.getValue();

            tmp = mulR * (srcValue << 16 >> 16);

            Register.setCC( tmp < 0, 
                            tmp == 0, 
                            false,
                            tmp < (-1 * Math.pow(2,15))|| tmp >= (Math.pow(2,15)-1));

            if(((fetchedMem >> 6) & 7) %2 == 0){
              Register.set((fetchedMem >> 6) & 7, tmp >>> 16);
              Register.set(((fetchedMem >> 6) & 7) + 1, tmp << 16 >>> 16);
            }else{
              Register.set((fetchedMem >> 6) & 7, tmp << 16 >>> 16);
            }

            break;
          case NEG:
            //negate
            dstOperand = getOperand(dstOperand, (fetchedMem >> 3) & 7, fetchedMem & 7);
            dstValue = dstOperand.getValue();

            tmp = ~dstValue + 1;

            Register.setCC( (tmp << 16 >> 16) < 0, 
                            (tmp << 16 >> 16) == 0,
                            (dstValue & 0xFFFF)  == 0100000,
                            !((tmp << 16 >> 16) == 0));

            if(dstOperand.flgRegister){
              Register.set(dstOperand.register, tmp);
            }else{
              setMemory2(dstOperand.address, tmp);
            }

            break;
          case RESET:
            //reset external bus
            Kl11.reset();
            Rk11.reset();
            Register.PSW = 0;
            break;
          case ROL:
            //rotate left
            dstOperand = getOperand(dstOperand,(fetchedMem >> 3) & 7,fetchedMem  & 7);
            dstValue = dstOperand.getValue();

            int roltmp = 0;
            if(Register.getC()) roltmp = 1;

            tmp = (dstValue << 1) + roltmp;

            if(dstValue << 16 >>> 31 == 1) Register.setC(true);
            if(dstValue << 16 >>> 31 == 0) Register.setC(false);

            Register.setCC( (tmp << 16 >> 16) < 0, 
                            (tmp << 16 >> 16) == 0, 
                            (Register.getN() || Register.getC()) && (!Register.getN() || !Register.getC()), 
                            Register.getC());

            if(dstOperand.flgRegister){
              Register.set(dstOperand.register, tmp);
            }else if(dstOperand.flgAddress){
              setMemory2(dstOperand.address, tmp);
            }else{
              setMemory2(dstOperand.immediate, tmp);
            }

            break;
          case ROR:
            //rotate right
            dstOperand = getOperand(dstOperand,(fetchedMem >> 3) & 7,fetchedMem  & 7);
            dstValue = dstOperand.getValue();

            int rortmp = 0;
            if(Register.getC()) rortmp = 1;

            tmp = (dstValue << 16 >>> 16 >> 1) | (rortmp << 15);

            if(dstValue << 31 >>> 31 == 1) Register.setC(true);
            if(dstValue << 31 >>> 31 == 0) Register.setC(false);

            Register.setCC( (tmp << 16 >> 16) < 0, 
                            (tmp << 16 >> 16) == 0, 
                            (Register.getN() || Register.getC()) && (!Register.getN() || !Register.getC()), 
                            Register.getC());

            if(dstOperand.flgRegister){
              Register.set(dstOperand.register, tmp);
            }else if(dstOperand.flgAddress){
              setMemory2(dstOperand.address, tmp);
            }else{
              setMemory2(dstOperand.immediate, tmp);
            }

            break;
          case RTS:
            //return from subroutine
            Register.set(7,Register.get(fetchedMem & 7));
            Register.set(fetchedMem & 7,popStack());

            break;
          case RTI:
          case RTT:
            //return from interrupt
            Register.set(7, popStack());
            Register.PSW = popStack();

            break;
          case SBC:
            //subtract carry
            dstOperand = getOperand(dstOperand,(fetchedMem >> 3) & 7,fetchedMem  & 7);
            dstValue = dstOperand.getValue();

            tmp = dstValue;
            if(Register.getC()) tmp = tmp - 1;

            Register.setCC( (tmp << 16 >> 16) < 0, 
                            (tmp << 16 >> 16) == 0,
                            (dstValue & 0xFFFF)  == 0100000,
                            !(dstValue==0 && Register.getC()));

            if(dstOperand.flgRegister){
              Register.set(dstOperand.register, tmp);
            }else{
              setMemory2(dstOperand.address, tmp);
            }

            break;
          case SCC:
            //set all cc's
            Register.setCC(true,true,true,true);
            break;
          case SETD:
            trap(010, 012);
            break;
          case SEN:
            //set n
            Register.setN(true);
            break;
          case SENZ:
            //set n z
            Register.setN(true);
            Register.setZ(true);
            break;
          case SEV:
            //set v
            Register.setV(true);
            break;
          case SOB:
            //subtract one and branch if not equal to 0
            tmp = Register.get((fetchedMem >> 6) & 7) - 1;
            Register.set((fetchedMem >> 6) & 7,tmp);
            if(tmp != 0) Register.set(7,getOffset6(dstOperand,(fetchedMem >> 3) & 7,fetchedMem  & 7).address);
            break;
          case SUB:
            //subtract
            srcOperand = getOperand(srcOperand,(fetchedMem >> 9) & 7,(fetchedMem >> 6) & 7);
            dstOperand = getOperand(dstOperand, (fetchedMem >> 3) & 7, fetchedMem & 7);
            srcValue = (~(srcOperand.getValue()) + 1) << 16 >> 16;
            dstValue = dstOperand.getValue();

            tmp = dstValue + srcValue;

            Register.setCC( (tmp << 16 >> 16) < 0,
                            (tmp << 16 >> 16) == 0,
                            getOverflow(dstValue, srcValue, tmp, 16),
                            srcValue != 0 && !getCarry(dstValue, srcValue, tmp, 16));

            if(dstOperand.flgRegister){
              Register.set(dstOperand.register, tmp);
            }else{
              setMemory2(dstOperand.address, tmp);
            }

            break;
          case SWAB:
            //swap byte
            dstOperand = getOperand(dstOperand,(fetchedMem >> 3) & 7,fetchedMem  & 7);
            dstValue = dstOperand.getValue();

            tmp = (dstValue << 16 >>> 24 ) + (dstValue << 24 >>> 16);

            Register.setCC( (tmp << 24 >> 24) < 0, 
                            (tmp << 24 >> 24) == 0, 
                            false, 
                            false);

            if(dstOperand.flgRegister){
              Register.set(dstOperand.register, tmp);
            }else if(dstOperand.flgAddress){
              setMemory2(dstOperand.address, tmp);
            }

            break;
          case SXT:
            //sign extend
            dstOperand = getOperand(dstOperand,(fetchedMem >> 3) & 7,fetchedMem  & 7);

            tmp = 0;
            if(Register.getN()) tmp = 0xffff;

            Register.setCC( Register.getN(),
                            !Register.getN(), 
                            false, 
                            Register.getC());

            if(dstOperand.flgRegister){
              Register.set(dstOperand.register, tmp);
            }else if(dstOperand.flgAddress){
              setMemory2(dstOperand.address, tmp);
            }

            break;
          case TRAP:
            //trap
            trap(034,036);
            break;
          case TST:
            //test
            dstOperand = getOperand(dstOperand,(fetchedMem >> 3) & 7,fetchedMem  & 7);
            dstValue = dstOperand.getValue();

            Register.setCC( (dstValue << 16 >> 16) < 0, 
                            (dstValue << 16 >> 16) == 0, 
                            false, 
                            false);
            break;
          case TSTB:
            //test
            dstOperand = getOperand(dstOperand,(fetchedMem >> 3) & 7,fetchedMem  & 7, true);
            dstValue = dstOperand.getValue();

            Register.setCC( (dstValue << 24 >> 24) < 0, 
                    (dstValue << 24 >> 24) == 0, 
                    false, 
                    false);
            break;
          case WAIT:
            //wait
            waitFlg = true; 
            break;
          case XOR:
            //exclusive
            int srcreg = Register.get((fetchedMem >> 6) & 7);
            dstOperand = getOperand(dstOperand,(fetchedMem >> 3) & 7,fetchedMem  & 7);
            dstValue = dstOperand.getValue();

            tmp = srcreg^dstValue;

            Register.setCC( (tmp << 16 >> 16) < 0, 
                            (tmp << 16 >> 16) == 0, 
                            false, 
                            Register.getC());

            if(dstOperand.flgRegister){
              Register.set(dstOperand.register, tmp);
            }else if(dstOperand.flgAddress){
              setMemory2(dstOperand.address, tmp);
            }

            break;
          case WORD:
            System.out.print("\n");
            System.out.println("undefined case");
            System.out.println(getMemory2(Register.get(7) - 2));

            System.exit(0);
            break;
          }
        }
      }catch(MemoryUndefinedException e){
      }catch(MemoryException e){
        trap(0250,0252);
      }
    }
  }

  /*
   * TRAP
   */
  //トラップ
  void trap04(){
    trap(04, 06);
  }

  //割り込み・トラップ
  void trap(int newPC,int newPSW){
    int prePSW = Register.PSW;
    int prePC = Register.get(7);

    try {
      Register.set(7, Memory.getPhyMemory2(newPC));
      Register.PSW = (Register.getNowMode() << 12) | Memory.getPhyMemory2(newPSW);

      pushStack(prePSW);
      pushStack(prePC);
    } catch (MemoryException e) {
      e.printStackTrace();
    }
  }

  /*
   * オフセット取得
   */
  //オフセット取得（PC+オフセット*2 8bit（符号付））
  Operand getOffset(Operand operand,int mem){
    return getOffset(operand,(mem >> 6) & 3,(mem >> 3) & 7,mem  & 7);
  }
  Operand getOffset(Operand operand,int first,int second,int third){
    operand.reset();
    operand.setAddress(Register.get(7) + ((((first << 6) + (second << 3) + third)) << 24 >> 24) * 2);
    return operand;
  }

  //オフセット取得（PC-オフセット*2 6bit（符号なし、正の数値））
  Operand getOffset6(Operand operand,int first,int second){
    operand.reset();
    operand.setAddress(Register.get(7) - ((first << 3) + second) * 2);
    return operand;
  }
  
  /*
   * オペランド取得
   */
  Operand getOperand(Operand operand,int mode, int regNo) throws MemoryException{
    return getOperand(operand, mode, regNo, false);
  }
  Operand getOperand(Operand operand,int mode, int regNo,boolean byteFlg) throws MemoryException{
    return getOperand(operand, mode, regNo, byteFlg, Register.getNowMode());
  }
  Operand getOperand(Operand operand,int mode, int regNo, boolean byteFlg,int mmuMode) throws MemoryException{
    operand.reset();

    switch(regNo){
    case 0:
    case 1:
    case 2:
    case 3:
    case 4:
    case 5:
    case 6:
      switch(mode){
      case 0:
        //レジスタ
        //registerにオペランドがある。
        operand.setRegister(regNo);
        break;
      case 1:
        //レジスタ間接
        //registerにオペランドのアドレスがある。
        operand.setAddress(Register.get(regNo));
        break;
      case 2:
        //自動インクリメント
        //registerにオペランドのアドレスがあり、命令実行後にregisterの内容をインクリメントする。
        operand.setAddress(Register.get(regNo));
        if(byteFlg){
          if(regNo == 6){
            Register.add(regNo,2);
          }else{
            Register.add(regNo,1);
          }
        }else{
          Register.add(regNo,2);
        }
        break;
      case 3:
        //自動インクリメント間接
        //registerにオペランドへのポインタのアドレスがあり、命令実行後にregisterの内容を2だけインクリメントする。
        operand.setAddress(getMemory2(Register.get(regNo),mmuMode));
        if(byteFlg){
          if(regNo == 6){
            Register.add(regNo,2);
          }else{
            Register.add(regNo,1);
          }
        }else{
          Register.add(regNo,2);
        }
        break;
      case 4:
        //自動デクリメント
        //命令実行前にregisterをデクリメントし、それをオペランドのアドレスとして使用する。
        if(byteFlg){
          if(regNo == 6){
            Register.add(regNo,-2);
          }else{
            Register.add(regNo,-1);
          }
        }else{
          Register.add(regNo,-2);
        }
        operand.setAddress(Register.get(regNo));
        break;
      case 5:
        //自動デクリメント間接
        //命令実行前にregisterを2だけデクリメントし、それをオペランドへのポインタのアドレスとして使用する。
        Register.add(regNo,-2);
        operand.setAddress(getMemory2(Register.get(regNo),mmuMode));
        break;
      case 6:
        //インデックス
        //register+Xがオペランドのアドレス。Xはこの命令に続くワード。
        operand.setAddress(Register.get(regNo) + readMemory());
        break;
      case 7:
        //インデックス間接
        //register+Xがオペランドへのポインタのアドレス。Xはこの命令に続くワード。
        operand.setAddress(getMemory2(Register.get(regNo) + readMemory(),mmuMode));
        break;
      }
      break;

    case 7:
      switch(mode){
      case 0:
        //レジスタ
        //registerにオペランドがある。
        operand.setRegister(regNo);
        break;
      case 1:
        //レジスタ間接
        //registerにオペランドのアドレスがある。
        operand.setAddress(Register.get(regNo));
        break;      
      case 2:
        //イミディエート
        //オペランドは命令内にある。
        operand.setImmediate(readMemory() << 16 >>> 16);
        break;
      case 3:
        //絶対
        //オペランドの絶対アドレスが命令内にある。
        operand.setAddress(readMemory() << 16 >>> 16);
        break;
      case 4:
        //自動デクリメント
        //命令実行前にregisterをデクリメントし、それをオペランドのアドレスとして使用する。
        Register.add(regNo,-2);
        operand.setAddress(Register.get(regNo));
        break;
      case 5:
        //自動デクリメント間接
        //命令実行前にregisterを2だけデクリメントし、それをオペランドへのポインタのアドレスとして使用する。
        Register.add(regNo,-2);
        operand.setAddress(getMemory2(Register.get(regNo),mmuMode));
        break;
      case 6:
        //相対
        //命令に続くワードの内容 a を PC+2 に加算したものをアドレスとして使用する。
        operand.setAddress((readMemory() + Register.get(7)) << 16 >>> 16);
        break;
      case 7:
        //相対間接
        //命令に続くワードの内容 a を PC+2 に加算したものをアドレスのアドレスとして使用する。
        operand.setAddress(getMemory2((readMemory() + Register.get(7)) << 16 >>> 16,mmuMode));
        break;
      }
      break;
    }
    return operand;
  }

  /*
   * オペコード取得
   */
  //オペコード（ニーモニック）
  enum Opcode {
    ADC, ADD, ASH, ASHC, ASL, ASR,
    BCC, BCS, BEQ, BGE, BGT, BHI, BICB, BIC, BIT, BITB, BIS, BISB, BLE, BLOS, BLT, BMI, BNE, BPL, BR, BVS,
    CLR, CLRB, CMP, CMPB, COM,
    DEC, DECB, DIV,
    INC, INCB,
    JMP, JSR,
      ROR, ROL, RTT, RTS, RTI,
    MOV, MOVB, MUL,
    NEG,
    SBC, SETD,  SOB, SUB, SWAB, SXT, TRAP,
    TST, TSTB,
    XOR,
    RESET,
    MFPI,MTPI,
    WAIT,
    CLC,SEN, SEV, SENZ, SCC,
    WORD,HALT
  }

  //機械語からオペコード（ニーモニック）取得
  Opcode getOpcode(int opnum){
    Opcode opcode = null;
    switch(opnum >> 15){
    case 0:
      switch((opnum >> 12) & 7){
      case 0:
        switch((opnum >> 9) & 7){
        case 0:
          switch((opnum >> 6) & 7){
          case 0:
            switch((opnum >> 3) & 7){
            case 0:
              switch(opnum  & 7){
              case 0:
                opcode = Opcode.HALT;
                break;
              case 1:
                opcode = Opcode.WAIT;
                break;
              case 2:
                opcode = Opcode.RTI;
                break;
              case 5:
                opcode = Opcode.RESET;
                break;
              case 6:
                opcode = Opcode.RTT;
                break;
              }
              break;
            }
            break;
          case 1:
            opcode = Opcode.JMP;
            break;
          case 2:
            switch((opnum >> 3) & 7){
            case 0:
              opcode = Opcode.RTS;
              break;
            case 4:
              switch(opnum  & 7){
                case 1:
                  opcode = Opcode.CLC;
                  break;
              }
              break;
            case 6:
              switch(opnum  & 7){
              case 2:
                opcode = Opcode.SEV;
                break;
              }
              break;
            case 7:
              switch(opnum  & 7){
              case 0:
                opcode = Opcode.SEN;
                break;
              case 4:
                opcode = Opcode.SENZ;
                break;
              case 7:
                opcode = Opcode.SCC;
                break;
              }
              break;
            }
            break;
          case 3:
            opcode = Opcode.SWAB;
            break;
          case 4:
          case 5:
          case 6:
          case 7:
            opcode = Opcode.BR;
            break;
          }
          break;
        case 1:
          switch((opnum >> 6) & 7){
          case 0:
          case 1:
          case 2:
          case 3:
            opcode = Opcode.BNE;
            break;
          case 4:
          case 5:
          case 6:
          case 7:
            opcode = Opcode.BEQ;
            break;
          }
          break;
        case 2:
          switch((opnum >> 6) & 7){
          case 0:
          case 1:
          case 2:
          case 3:
            opcode = Opcode.BGE;
            break;
          case 4:
          case 5:
          case 6:
          case 7:
            opcode = Opcode.BLT;
            break;
          }
          break;
        case 3:
          switch((opnum >> 6) & 7){
          case 0:
          case 1:
          case 2:
          case 3:
            opcode = Opcode.BGT;
            break;
          case 4:
          case 5:
          case 6:
          case 7:
            opcode = Opcode.BLE;
            break;
          }
          break;
        case 4:
          opcode = Opcode.JSR;
          break;
        case 5:
          switch((opnum >> 6) & 7){
          case 0:
            opcode = Opcode.CLR;
            break;
          case 1:
            opcode = Opcode.COM;
            break;
          case 2:
            opcode = Opcode.INC;
            break;
          case 3:
            opcode = Opcode.DEC;
            break;
          case 4:
            opcode = Opcode.NEG;
            break;
          case 5:
            opcode = Opcode.ADC;
            break;
          case 6:
            opcode = Opcode.SBC;
            break;
          case 7:
            opcode = Opcode.TST;
            break;
          }
          break;
        case 6:
          switch((opnum >> 6) & 7){
          case 0:
            opcode = Opcode.ROR;
            break;
          case 1:
            opcode = Opcode.ROL;
            break;
          case 2:
            opcode = Opcode.ASR;
            break;
          case 3:
            opcode = Opcode.ASL;
            break;
          case 5:
            opcode = Opcode.MFPI;
            break;
          case 6:
            opcode = Opcode.MTPI;
            break;
          case 7:
            opcode = Opcode.SXT;
            break;
          }
          break;
        }
        break;
      case 1:
        opcode = Opcode.MOV;
        break;
      case 2:
        opcode = Opcode.CMP;
        break;
      case 3:
        opcode = Opcode.BIT;
        break;
      case 4:
        opcode = Opcode.BIC;
        break;
      case 5:
        opcode = Opcode.BIS;
        break;
      case 6:
        opcode = Opcode.ADD;
        break;
      case 7:
        switch((opnum >> 9) & 7){
        case 0:
          opcode = Opcode.MUL;
          break;
        case 1:
          opcode = Opcode.DIV;
          break;
        case 2:
          opcode = Opcode.ASH;
          break;
        case 3:
          opcode = Opcode.ASHC;
          break;
        case 4:
          opcode = Opcode.XOR;
          break;
        case 7:
          opcode = Opcode.SOB;
          break;
        }
        break;
      }
      break;
    case 1:
      switch((opnum >> 12) & 7){
      case 0:
        switch((opnum >> 9) & 7){
        case 0:
          switch((opnum >> 6) & 7){
          case 0:
          case 1:
          case 2:
          case 3:
            opcode = Opcode.BPL;
            break;
          case 4:
          case 5:
          case 6:
          case 7:
            opcode = Opcode.BMI;
            break;
          }
          break;
        case 1:
          switch((opnum >> 6) & 7){
          case 0:
          case 1:
          case 2:
          case 3:
            opcode = Opcode.BHI;
            break;
          case 4:
          case 5:
          case 6:
          case 7:
            opcode = Opcode.BLOS;
            break;
          }
          break;
        case 2:
          switch((opnum >> 6) & 7){
          case 4:
          case 5:
          case 6:
          case 7:
            opcode = Opcode.BVS;
            break;
          }
          break;
        case 3:
          switch((opnum >> 6) & 7){
          case 0:
          case 1:
          case 2:
          case 3:
            opcode = Opcode.BCC;
            break;
          case 4:
          case 5:
          case 6:
          case 7:
            opcode = Opcode.BCS;
            break;
          }
          break;
        case 4:
          switch((opnum >> 6) & 7){
          case 4: 
            opcode = Opcode.TRAP;
            break;
          }
          break;
        case 5:
          switch((opnum >> 6) & 7){
          case 0:
            opcode = Opcode.CLRB;
            break;
          case 2:
            opcode = Opcode.INCB;
            break;
          case 3:
            opcode = Opcode.DECB;
            break;
          case 7:
            opcode = Opcode.TSTB;
            break;
          }
          break;
        case 6:
          switch((opnum >> 6) & 7){
          case 5:
            opcode = Opcode.MFPI;
            break;
          }
          break;
        }
        break;
      case 1:
        opcode = Opcode.MOVB;
        break;
      case 2:
        opcode = Opcode.CMPB;
        break;
      case 3:
        opcode = Opcode.BITB;
        break;
      case 4:
        opcode = Opcode.BICB;
        break;
      case 5:
        opcode = Opcode.BISB;
        break;
      case 6:
        opcode = Opcode.SUB;
        break;
      case 7:
        opcode = Opcode.SETD;
        break;
      }
      break;
    }
    if(opcode == null) opcode = Opcode.WORD;
    return opcode;
  }

  /*
   * メモリアクセス
   */
  //メモリ上の命令を取得してPC+2する（命令取得の場合に使用する）
  int fetchMemory() throws MemoryException{
    int opcode = getMemory2(Register.get(7)) << 16 >>> 16;

    //逆アセンブル/デバッグモード（すべて）の場合は出力
    if(Pdp11.flgExeMode || Pdp11.flgTapeMode){
      if(Pdp11.flgDebugMode>1) printOpcode(opcode);
    }else{
      printOpcode(opcode);
      strnum++;
    }

    if(Util.checkBit(Mmu.SR0, 13) != 1 &&
      Util.checkBit(Mmu.SR0, 14) != 1 &&
      Util.checkBit(Mmu.SR0, 15) != 1){
      instAddr = Register.get(7);
    }
    Register.add(7, 2);
    
    return opcode;
  }

  //メモリ上のデータを取得してPC+2する（オペランド取得の場合に使用する）
  int readMemory() throws MemoryException{
    int opcode = getMemory2(Register.get(7)) << 16 >>> 16;

    //逆アセンブル/デバッグモード（すべて）の場合は出力
    if(Pdp11.flgExeMode || Pdp11.flgTapeMode){
      if(Pdp11.flgDebugMode>1) printOpcode(opcode);
    }else{
      printOpcode(opcode);
      strnum++;
    }

    Register.add(7, 2);

    return opcode;
  }

  //2バイト単位でリトルエンディアンを反転して10進数で取得
  static int getMemory2(int addr) throws MemoryException{
    return getMemory2(addr, Register.getNowMode());
  }
  static int getMemory2(int addr, int mode) throws MemoryException{
    addr = addr << 16 >>> 16;
    return Memory.getPhyMemory2(Mmu.analyzeMemory(addr, mode));
  } 

  //1バイト単位で指定箇所のメモリを取得
  static int getMemory1(int addr) throws MemoryException{
    return getMemory1(addr, Register.getNowMode());
  }
  static int getMemory1(int addr,int mode) throws MemoryException{
    addr = addr << 16 >>> 16;
    return Memory.getPhyMemory1(Mmu.analyzeMemory(addr, mode));
  }

  //2バイト単位で指定箇所のメモリを更新
  void setMemory2(int addr, int src) throws MemoryException{
    setMemory2(addr, src, Register.getNowMode());
  }
  void setMemory2(int addr,int src,int mode) throws MemoryException{
    addr = addr << 16 >>> 16;
    Memory.setPhyMemory2(Mmu.analyzeMemory(addr, mode), src);
  }

  //1バイト単位で指定箇所のメモリを更新
  void setMemory1(int addr, int src) throws MemoryException{
    setMemory1(addr, src, Register.getNowMode());
  }
  void setMemory1(int addr,int src,int mode) throws MemoryException{
    addr = addr << 16 >>> 16;
    Memory.setPhyMemory1(Mmu.analyzeMemory(addr, mode), src);
  }

  //スタックプッシュ
  void pushStack(int n) throws MemoryException{
    Register.add(6, -2);
    setMemory2(Register.get(6), n);
  }

  //スタックポップ
  int popStack() throws MemoryException{
    int tmp = getMemory2(Register.get(6));
    Register.add(6, 2);
    return tmp;
  }

  /*
   * データ出力
   */
  //機械語を出力
  void printOpcode(int opcode){
    if(printCnt >= START_CNT || Pdp11.flgDismMode){
      if(Pdp11.flgOctMode){
        System.out.print(String.format("%06o", opcode));
      }else{
        System.out.print(String.format("%04x", opcode));
      }
      System.out.print(" ");
    }
  }

  //レジスタ・フラグの出力
  void printDebug(){
    if(printCnt >= START_CNT){
      Register.printDebug();
    }
  }

  /*
   * デバッグ用
   */
  //シンボルリストにPUSH
  void pushCall(int pc,int nextPc){
    dbgList.add(pc);
    rtnList.add(nextPc);
  }

  //シンボルリストをPOP
  void popCall(int pc){
    int lastIndex = rtnList.lastIndexOf(pc);
    if( lastIndex > -1){
      rtnList.remove(lastIndex);
      dbgList.remove(lastIndex);
    }
  }
  
  //シンボルを出力
  void printCall(){
    if(Pdp11.flgDebugMode>=1 && dbgList.size() != 0){
      if(printCnt >= START_CNT){
        System.out.print("\n*** ");
        for (int tmp : dbgList) {
          Util.printSub(tmp);
        }
        System.out.print("\n");
      }
    }
  }

  /*
   * オーバーフロー判定関数
   */

  //オーバーフロー判定
  boolean getOverflow(int front, int back, int result, int shiftCnt){
    if((front << shiftCnt >>> 31) == (back << shiftCnt >>> 31)){
      if((front << shiftCnt >>> 31) != (result << shiftCnt >>> 31)) return true;
    }
    return false;
  }

  //キャリー判定
  boolean getCarry(int front, int back, int result, int shiftCnt){
    if((front << shiftCnt >>> 31) == 1){
      if((back << shiftCnt >>> 31) == 1){
        return true;
      }else{
        if((result << shiftCnt >>> 31) == 0) return true;
      }
    }else{
      if((back << shiftCnt >>> 31) == 1){
        if((result << shiftCnt >>> 31) == 0) return true;
      }
    }
    return false;
  }

  /*
   * 逆アセンブル関数
   */ 
  void dissAssemble() throws MemoryException{
    Pdp11.flgExeMode = false; //実行モードオフ
    
    //逆アセンブル
    for(Register.set(7, 0);Register.get(7)<Memory.MEMORY_SIZE;){

      //プログラムカウンタを出力
      System.out.print(String.format("%4x", Register.get(7)));
      System.out.print(":   ");

      strnum = 0;

      int opnum = readMemory();
      Opcode opcode = getOpcode(opnum);

      switch(opcode){
      case ADC:
        printDisasm("adc", "", getOperandStr((opnum >> 3) & 7,opnum  & 7));
        break;
      case ADD:
        printDisasm("add", getOperandStr((opnum >> 9) & 7,(opnum >> 6) & 7), getOperandStr((opnum >> 3) & 7,opnum  & 7));
        break;
      case ASH:
        printDisasm("ash", getOperandStr((opnum >> 3) & 7,opnum  & 7), getRegisterName((opnum >> 6) & 7));
        break;
      case ASHC:
        printDisasm("ashc", getOperandStr((opnum >> 3) & 7,opnum  & 7), getRegisterName((opnum >> 6) & 7));
        break;
      case ASL:
        printDisasm("asl", "", getOperandStr((opnum >> 3) & 7,opnum  & 7));
        break;
      case ASR:
        printDisasm("asr", "", getOperandStr((opnum >> 3) & 7,opnum  & 7));
        break;
      case BCC:
        printDisasm("bcc", "", getOffsetStr((opnum >> 6) & 7,(opnum >> 3) & 7,opnum  & 7));
        break;
      case BCS:
        printDisasm("bcs", "", getOffsetStr((opnum >> 6) & 7,(opnum >> 3) & 7,opnum  & 7));
        break;
      case BEQ:
        printDisasm("beq", getOffsetStr((opnum >> 6) & 7,(opnum >> 3) & 7,opnum  & 7), "");
        break;
      case BGE:
        printDisasm("bge", "", getOffsetStr((opnum >> 6) & 7,(opnum >> 3) & 7,opnum  & 7));
        break;
      case BGT:
        printDisasm("bgt", "", getOffsetStr((opnum >> 6) & 7,(opnum >> 3) & 7,opnum  & 7));
        break;
      case BHI:
        printDisasm("bhi", "", getOffsetStr((opnum >> 6) & 7,(opnum >> 3) & 7,opnum  & 7));
        break;
      case BIC:
        printDisasm("bic", getOperandStr((opnum >> 9) & 7,(opnum >> 6) & 7), getOperandStr((opnum >> 3) & 7,opnum  & 7));
        break;
      case BICB:
        printDisasm("bicb", getOperandStr((opnum >> 9) & 7,(opnum >> 6) & 7), getOperandStr((opnum >> 3) & 7,opnum  & 7));
        break;
      case BIS:
        printDisasm("bis", getOperandStr((opnum >> 9) & 7,(opnum >> 6) & 7), getOperandStr((opnum >> 3) & 7,opnum  & 7));
        break;
      case BISB:
        printDisasm("bisb", getOperandStr((opnum >> 9) & 7,(opnum >> 6) & 7), getOperandStr((opnum >> 3) & 7,opnum  & 7));
        break;
      case BIT:
        printDisasm("bit", getOperandStr((opnum >> 9) & 7,(opnum >> 6) & 7), getOperandStr((opnum >> 3) & 7,opnum  & 7));
        break;
      case BITB:
        printDisasm("bitb", getOperandStr((opnum >> 9) & 7,(opnum >> 6) & 7), getOperandStr((opnum >> 3) & 7,opnum  & 7));
        break;
      case BLE:
        printDisasm("ble", "", getOffsetStr((opnum >> 6) & 7,(opnum >> 3) & 7,opnum  & 7));
        break;
      case BLOS:
        printDisasm("blos", "", getOffsetStr((opnum >> 6) & 7,(opnum >> 3) & 7,opnum  & 7));
        break;
      case BLT:
        printDisasm("blt", "", getOffsetStr((opnum >> 6) & 7,(opnum >> 3) & 7,opnum  & 7));
        break;
      case BMI:
        printDisasm("bmi", "", getOffsetStr((opnum >> 6) & 7,(opnum >> 3) & 7,opnum  & 7));
        break;
      case BNE:
        printDisasm("bne", "", getOffsetStr((opnum >> 6) & 7,(opnum >> 3) & 7,opnum  & 7));
        break;
      case BPL:
        printDisasm("bpl", "", getOffsetStr((opnum >> 6) & 7,(opnum >> 3) & 7,opnum  & 7));
        break;
      case BR:
        printDisasm("br", "", getOffsetStr((opnum >> 6) & 7,(opnum >> 3) & 7,opnum  & 7));
        break;
      case BVS:
        printDisasm("bvs", "", getOffsetStr((opnum >> 6) & 7,(opnum >> 3) & 7,opnum  & 7));
        break;
      case CLC:
        printDisasm("clc", "", "");
        break;
      case CLR:
        printDisasm("clr", "", getOperandStr((opnum >> 3) & 7,opnum  & 7));
        break;
      case CLRB:
        printDisasm("clrb", "", getOperandStr((opnum >> 3) & 7,opnum  & 7));
        break;
      case CMP:
        printDisasm("cmp", getOperandStr((opnum >> 9) & 7,(opnum >> 6) & 7), getOperandStr((opnum >> 3) & 7,opnum  & 7));
        break;
      case CMPB:
        printDisasm("cmpb", getOperandStr((opnum >> 9) & 7,(opnum >> 6) & 7), getOperandStr((opnum >> 3) & 7,opnum  & 7));
        break;
      case COM:
        printDisasm("com", "", getOperandStr((opnum >> 3) & 7,opnum  & 7));
        break;
      case DEC:
        printDisasm("dec", "", getOperandStr((opnum >> 3) & 7,opnum  & 7));
        break;
      case DECB:
        printDisasm("decb", "", getOperandStr((opnum >> 3) & 7,opnum  & 7));
        break;
      case DIV:
        printDisasm("div", getOperandStr((opnum >> 3) & 7,opnum  & 7), getRegisterName((opnum >> 6) & 7));
        break;
      case HALT:
        printDisasm("halt", "", "");
        break;
      case INC:
        printDisasm("inc", "", getOperandStr((opnum >> 3) & 7,opnum  & 7));
        break;
      case INCB:
        printDisasm("incb", "", getOperandStr((opnum >> 3) & 7,opnum  & 7));
        break;
      case JMP:
        printDisasm("jmp", "", getOperandStr((opnum >> 3) & 7,opnum  & 7));
        break;
      case JSR:
        printDisasm("jsr", getRegisterName((opnum >> 6) & 7), getOperandStr((opnum >> 3) & 7,opnum  & 7));
        break;
      case MFPI:
        printDisasm("mfpi", getOperandStr((opnum >> 3) & 7,opnum & 7), "");
        break;
      case MOV: 
        printDisasm("mov", getOperandStr((opnum >> 9) & 7,(opnum >> 6) & 7), getOperandStr((opnum >> 3) & 7,opnum  & 7));
        break;
      case MOVB:
        printDisasm("movb", getOperandStr((opnum >> 9) & 7,(opnum >> 6) & 7), getOperandStr((opnum >> 3) & 7,opnum  & 7));
        break;
      case MTPI:
        printDisasm("mtpi", "", getOperandStr((opnum >> 3) & 7,opnum & 7));
        break;
      case MUL:
        printDisasm("mul", getOperandStr((opnum >> 3) & 7,opnum  & 7), getRegisterName((opnum >> 6) & 7));
        break;
      case NEG:
        printDisasm("neg", "", getOperandStr((opnum >> 3) & 7,opnum  & 7));
        break;
      case RESET:
        printDisasm("reset", "", "");
        break;
      case ROL:
        printDisasm("rol", "", getOperandStr((opnum >> 3) & 7,opnum  & 7));
        break;
      case ROR:
        printDisasm("ror", "", getOperandStr((opnum >> 3) & 7,opnum  & 7));
        break;
      case RTI:
        printDisasm("rti", "", "");
        break;
      case RTS:
        printDisasm("rts", "", getRegisterName(opnum  & 7));
        break;
      case RTT:
        printDisasm("rtt", "", "");
        break;
      case SBC:
        printDisasm("sbc", "", getOperandStr((opnum >> 3) & 7,opnum  & 7));
        break;
      case SCC:
        printDisasm("scc", "", "");
        break;
      case SETD:
        printDisasm("setd", "", "");
        break;
      case SEN:
        printDisasm("sen", "", "");
        break;
      case SENZ:
        printDisasm("senz", "", "");
        break;
      case SEV:
        printDisasm("sev", "", "");
        break;
      case SOB:
        printDisasm("sob", getRegisterName((opnum >> 6) & 7), getOffset6Str((opnum >> 3) & 7,opnum  & 7));
        break;
      case SUB:
        printDisasm("sub", getOperandStr((opnum >> 9) & 7,(opnum >> 6) & 7), getOperandStr((opnum >> 3) & 7,opnum  & 7));
        break;
      case SWAB:
        printDisasm("swab", "", getOperandStr((opnum >> 3) & 7,opnum  & 7));
        break;
      case SXT:
        printDisasm("sxt", "", getOperandStr((opnum >> 3) & 7,opnum  & 7));
        break;
      case TRAP:
        if(getDex((opnum >> 3) & 7,opnum  & 7) == 0) readMemory();
        printDisasm("sys", "", String.valueOf(opnum  & 7));
        break;
      case TST:
        printDisasm("tst", "", getOperandStr((opnum >> 3) & 7,opnum  & 7));
        break;
      case TSTB:
        printDisasm("tstb", "", getOperandStr((opnum >> 3) & 7,opnum  & 7));
        break;
      case XOR:
        printDisasm("xor", String.valueOf(Register.get((opnum >> 6) & 7)), getOperandStr((opnum >> 3) & 7,opnum  & 7));
        break;
      case WAIT:
        printDisasm("wait", "", "");
        break;
      case WORD:
        printDisasm(".word", "", getNormalStr((opnum >> 6) & 7,(opnum >> 3) & 7,opnum  & 7));
        break;
      default:
        printDisasm("undefined", "", "");
        break;
      }
    }
  }

  /*
   * フィールド関数
   */
  //オペランド取得（PC+オフセット*2 8bit（符号付））
  String getOffsetStr(int first,int second,int third){
    return "0x" + String.format("%x",Register.get(7) + ((byte)((first << 6) + (second << 3) + third)) * 2);
  }

  //オペランド取得（PC-オフセット*2 6bit（符号なし、正の数値））
  String getOffset6Str(int first,int second){
    return "0x" + String.format("%x",Register.get(7) - ((first << 3) + second) * 2);
  }

  //オペランド取得（8進数 6bit）
  String getNormalStr(int first,int second,int third){
    return String.format("%o",(first << 6) + (second << 3) + third);
  }

  //オペランド文字列取得（dst,src）
  String getOperandStr(int mode, int regNo) throws MemoryException{
    switch(regNo){
    case 0:
    case 1:
    case 2:
    case 3:
    case 4:
    case 5:
    case 6:
      switch(mode){
      case 0:
        //レジスタ
        //registerにオペランドがある。
        return getRegisterName(regNo);
      case 1:
        //レジスタ間接
        //registerにオペランドのアドレスがある。
        return "(" + getRegisterName(regNo) + ")";
      case 2:
        //自動インクリメント
        //registerにオペランドのアドレスがあり、命令実行後にregisterの内容をインクリメントする。
        return "(" + getRegisterName(regNo) + ")+";
      case 3:
        //自動インクリメント間接
        //registerにオペランドへのポインタのアドレスがあり、命令実行後にregisterの内容を2だけインクリメントする。
        return "*(" + getRegisterName(regNo) + ")+";
      case 4:
        //自動デクリメント
        //命令実行前にregisterをデクリメントし、それをオペランドのアドレスとして使用する。
        return "-(" + getRegisterName(regNo) + ")";
      case 5:
        //自動デクリメント間接
        //命令実行前にregisterを2だけデクリメントし、それをオペランドへのポインタのアドレスとして使用する。
        return "*-(" + getRegisterName(regNo) + ")";
      case 6:
        //インデックス
        //register+Xがオペランドのアドレス。Xはこの命令に続くワード。
        return String.format("%o",readMemory() << 16 >>> 16) + "(" + getRegisterName(regNo) + ")";
      case 7:
        //インデックス間接
        //register+Xがオペランドへのポインタのアドレス。Xはこの命令に続くワード。
        return "*-" + String.format("%o",readMemory() << 16 >>> 16) + "(" + getRegisterName(regNo) + ")";
      }
      break;
    case 7:
      switch(mode){
      case 0:
        //レジスタ
        //registerにオペランドがある。
        return getRegisterName(regNo);
      case 1:
        //レジスタ間接
        //registerにオペランドのアドレスがある。
        return "(" + getRegisterName(regNo) + ")";
      case 2:
        //イミディエート
        //オペランドは命令内にある。
        return "$" + String.format("%o",readMemory() << 16 >>> 16);
      case 3:
        //絶対
        //オペランドの絶対アドレスが命令内にある。
        return "*$" + String.format("%o",readMemory() << 16 >>> 16);
      case 4:
        //自動デクリメント
        //命令実行前にregisterをデクリメントし、それをオペランドのアドレスとして使用する。
        return "-(" + getRegisterName(regNo) + ")";
      case 5:
        //自動デクリメント間接
        //命令実行前にregisterを2だけデクリメントし、それをオペランドへのポインタのアドレスとして使用する。
        return "*-(" + getRegisterName(regNo) + ")";
      case 6:
        //相対
        //命令に続くワードの内容 a を PC+2 に加算したものをアドレスとして使用する。
        return "0x" + String.format("%02x",(readMemory() + Register.get(7)) << 16 >>> 16);
      case 7:
        //相対間接
        //命令に続くワードの内容 a を PC+2 に加算したものをアドレスのアドレスとして使用する。
        return "*$0x" + String.format("%02x",readMemory() + Register.get(7));
      }
    }
    return "";
  }

  /*
   * データ編集
   */
  //出力編集
  void printDisasm(String mnemonic, String srcOperand, String dstOperand){
    for(;strnum<3;strnum++) System.out.print("     ");

    System.out.print(" ");
    System.out.print(mnemonic);

    for(int j=mnemonic.length();j<9;j++) System.out.print(" ");

    System.out.print(srcOperand);

    if(!(srcOperand.equals("")) && !(dstOperand.equals(""))) System.out.print(", ");

    System.out.print(dstOperand + "\n");
  }

  //8進数から10進数に変換
  int getDex(int first,int second){
    return Integer.parseInt(Integer.toString(first * 10 + second), 8);
  }

  //レジスタ名称取得
  String getRegisterName(int no){
    if(no == 7){
      return "pc";
    }else if(no == 6){
      return "sp";
    }else{
      return "r" + no;
    }
  }
}


/*
 * オペランド
 */
class Operand{

  int immediate;
  int address;
  int register;

  boolean flgImmediate;
  boolean flgAddress;
  boolean flgRegister;

  public void reset(){
    immediate = 0;
    address = 0;
    register = 0;
    flgImmediate = false;
    flgAddress = false;
    flgRegister = false;
  }

  public int getValue() throws MemoryException{
    return getValue(false,Register.getNowMode());
  }

  public int getValue(boolean flgByte) throws MemoryException{
    return getValue(flgByte,Register.getNowMode());
  }

  public int getValue(int mode) throws MemoryException{
    return getValue(false,mode);
  }

  public int getValue(boolean flgByte,int mode) throws MemoryException{
    if(flgRegister){
      return Register.get(register, mode);
    }else if(flgAddress){
      if(flgByte){
        return Cpu.getMemory1(address, mode);
      }else{
        return Cpu.getMemory2(address, mode);
      }
    }else{
      return immediate;
    }
  }

  public void setImmediate(int input){
    immediate = input << 16 >>> 16;
    flgImmediate = true;
  }

  public void setAddress(int input){
    address = input << 16 >>> 16;
    flgAddress = true;
  }

  public void setRegister(int input){
    register = input;
    flgRegister = true;
  }
}
