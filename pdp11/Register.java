package pdp11;

/*
 * レジスタ
 */
public class Register{
  /*
   * レジスタ
   */
  static int[] reg;         //汎用レジスタ
  static int reg6_u;        //ユーザモードR6
  
  static int PSW;           //PSW モード 0:カーネル,3:ユーザー

  static int STACK_LIMIT;   //STACK_LIMIT
  static int PIRQ;          //PIRQ
  static int PB;            //PB
  static int CPUERR;        //CPUERR

  static int[] kernelPAR;   //カーネルPAR
  static int[] userPAR;     //ユーザーPAR
  
  static int[] kernelPDR;   //カーネルPDR
  static int[] userPDR;     //ユーザーPDR
  
  static int CLOCK1;        //CLOCK1

  static void reset(){
    reg = new int[8];
    
    kernelPAR = new int[8];
    userPAR = new int[8];
    kernelPDR = new int[8];
    userPDR = new int[8];
    
    reg[0] = 0;
    reg[1] = 0;
    reg[2] = 0;
    reg[3] = 0;
    reg[4] = 0;
    reg[5] = 0;
    reg[6] = 0;
    reg[7] = 0;

    reg6_u = 0;
    
    PSW = 0;
    STACK_LIMIT = 0;
    PIRQ = 0;
    PB = 0;
    CPUERR = 0;

    for(int i=0;i<8;i++){
      kernelPAR[i] = 0;
      userPAR[i] = 0;
      kernelPDR[i] = 0;
      userPDR[i] = 0;
    }
    
    CLOCK1 = 0;
  }

  /*
   * レジスタ設定
   */
  static void set(int regNo,int val){
    set(regNo,val,getNowMode());
  }
  static void set(int regNo,int val,int mode){
    //R6(SP)はユーザモードとカーネルモードで別のレジスタを持つ
    if(regNo == 6 && mode != 0){
      reg6_u = val << 16 >>> 16;
    }else{
      reg[regNo] = val << 16 >>> 16;
    }
  }
  
  /*
   * レジスタ加算
   */
  static void add(int regNo,int val){
    //R6(SP)はユーザモードとカーネルモードで別のレジスタを持つ
    if(regNo == 6 && getNowMode() != 0){
      reg6_u = (reg6_u+val) << 16 >>> 16;
    }else{
      set(regNo, (get(regNo)+val) << 16 >>> 16);
    }
  }

  /*
   * レジスタ取得
   */
  static int get(int regNo){
    return get(regNo,getNowMode());
  }
  static int get(int regNo,int mode){
    if(regNo == 6 && mode != 0){
      return reg6_u;
    }else{
      return reg[regNo];
    }
  }
  
  /*
   * PSW取得
   */
  //現モード取得
  static int getNowMode(){
    return PSW << 16 >>> 30;
  }

  //前モード取得
  static int getPreMode(){
    return PSW << 18 >>> 30;
  }
  
  //優先度取得
  static int getPriority(){
    return PSW << 24 >>> 29;
  }

  /*
   * PAR/PDR取得
   */
  //カーネルPARブロックアドレス取得
  static int getKernelBaseBlockNo(int i){
    return Register.kernelPAR[i] << 20 >>> 20;
  }

  //ユーザPARブロックアドレス取得
  static int getUserBaseBlockNo(int i){
    return Register.userPAR[i] << 20 >>> 20;
  }

  //カーネルPDRブロック数取得
  static int getKernelBlockCnt(int i){
    return Register.kernelPDR[i] << 17 >>> 25;
  }

  //ユーザPDRブロック数取得
  static int getUserBlockCnt(int i){
    return Register.userPDR[i] << 17 >>> 25;
  }

  /*
   * CC設定
   */
  static void setCC(boolean args_n,boolean args_z,boolean args_v,boolean args_c){
    setN(args_n);
    setZ(args_z);
    setV(args_v);
    setC(args_c);
  }
  static void setN(boolean args_n){
    if(args_n){
      PSW = PSW | (1 << 3);
    }else{
      if((PSW & (1 << 3)) != 0) PSW = PSW - (1 << 3);
    }
  }
  static void setZ(boolean args_z){
    if(args_z){
      PSW = PSW | (1 << 2);
    }else{
      if((PSW & (1 << 2)) != 0) PSW = PSW - (1 << 2);
    }
  }
  static void setV(boolean args_v){
    if(args_v){
      PSW = PSW | (1 << 1);
    }else{
      if((PSW & (1 << 1)) != 0) PSW = PSW - (1 << 1);
    }
  }
  static void setC(boolean args_c){
    if(args_c){
      PSW = PSW | 1;
    }else{
      if((PSW & 1) != 0)  PSW = PSW - 1;
    }
  }
  
  /*
   * CC取得
   */
  static boolean getN(){
    return !((PSW & (1 << 3)) == 0);
  }
  static boolean getZ(){
    return !((PSW & (1 << 2)) == 0);
  }
  static boolean getV(){
    return !((PSW & (1 << 1)) == 0);
  }
  static boolean getC(){
    return !((PSW & (1)) == 0);
  }
  
  /*
   * デバッグ用出力
   */
  static void printDebug(){

    System.out.print("\n");

    //R0-SP
    if(Pdp11.flgOctMode){
      System.out.printf("%06o", Register.get(0));
      System.out.printf(" %06o", Register.get(1));
      System.out.printf(" %06o", Register.get(2));
      System.out.printf(" %06o", Register.get(3));
      System.out.printf(" %06o", Register.get(4));
      System.out.printf(" %06o", Register.get(5));
      System.out.printf(" %06o", Register.get(6));
    }else{
      System.out.printf("%04x", Register.get(0));
      System.out.printf(" %04x", Register.get(1));
      System.out.printf(" %04x", Register.get(2));
      System.out.printf(" %04x", Register.get(3));
      System.out.printf(" %04x", Register.get(4));
      System.out.printf(" %04x", Register.get(5));
      System.out.printf(" %04x", Register.get(6));
    }

    //PSW
    if(Pdp11.flgOctMode){
      System.out.printf(" %06o ", PSW);
    }else{
      System.out.printf(" %04x ", PSW);
    }
    System.out.print(String.format("%x",(getNowMode())));
    System.out.print(String.format("%x",(getPreMode())));
    System.out.print(String.format("%x",(getPriority())));
    if(getZ()){
      System.out.print("Z");
    }else{
      System.out.print("-");
    }
    if(getN()){
      System.out.print("N");
    }else{
      System.out.print("-");
    }
    if(getC()){
      System.out.print("C");
    }else{
      System.out.print("-");
    }
    if(getV()){
      System.out.print("V");
    }else{
      System.out.print("-");
    }

    //PC
    System.out.print(" ");
    if(Pdp11.flgOctMode){
      System.out.print(String.format("%06o",Register.get(7),Register.get(7)));
    }else{
      System.out.print(String.format("%04x",Register.get(7),Register.get(7)));
    }
    System.out.print(":");
  }
}
