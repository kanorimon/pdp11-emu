package pdp11;

/*
 * レジスタクラス
 * R1-R5:汎用レジスタ
 * R6:スタックポインタSP
 * R7:プログラムカウンタPC
 */
public class Register{
	static int[] reg; //汎用レジスタ
	static int reg6_u; //ユーザモードR6
	
	static boolean n; //負の場合
	static boolean z; //ゼロの場合
	static boolean v; //オーバーフローが発生した場合
	static boolean c; //MSB(最上位ビット)からキャリが発生、MSB/LSB(最下位ビット)から1がシフトされた場合

	static int PSW; //PSW モード 0:カーネル,3:ユーザー

	static int[] kernelPAR; //カーネルPAR
	static int[] userPAR; //ユーザーPAR
	
	static int[] kernelPDR; //カーネルPDR
	static int[] userPDR; //ユーザーPDR
	
	static int CLOCK1;

	//レジスタ初期化
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
		reg[6] = 0; //spは最後尾のアドレスを指す
		reg[7] = 0;

		reg6_u = 0;
		
		n = false;
		z = false;
		v = false;
		c = false;
		
		PSW = 0;
		
		for(int i=0;i<8;i++){
			kernelPAR[i] = 0;
			userPAR[i] = 0;
			kernelPDR[i] = 0;
			userPDR[i] = 0;
		}
		
		CLOCK1 = 0;

	}

	//レジスタを上書き
	static void set(int regNo,int val){
		if(regNo == 6 && getNowMode() != 0){
			reg6_u = val;
		}else{
			reg[regNo] = val;
		}
	}

	//カーネルスタックに加算
	static void addKernelStack(int val){
		if(reg[6]+val > 0xffff){
			reg[6] = (reg[6]+val) << 16 >>> 16;
		}else{
			reg[6] = reg[6]+val;
		}
	}
	
	//レジスタに加算
	static void add(int regNo,int val){
		if(get(regNo)+val > 0xffff){
			set(regNo, (get(regNo)+val) << 16 >>> 16);
		}else{
			set(regNo, get(regNo) + val);
		}
	}

	//レジスタを取得
	static int get(int regNo){
		if(regNo == 6 && getNowMode() != 0){
			return reg6_u;
		}else{
			return reg[regNo];
		}
	}
	
	//PAR設定
	static void setPar(int type,int no,int value){
		if(type == 0){
			kernelPAR[no] = value;
		}else{
			userPAR[no] = value;
		}
	}
	
	//PAR取得
	static int getPar(int type,int no){
		if(type == 0){
			return kernelPAR[no];
		}else{
			return userPAR[no];
		}
	}
	
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
	
	//カーネルPARブロックアドレス取得
	static int getKernelBaseBlockNo(int i){
		return Register.kernelPAR[i] << 20 >>> 20;
	}

	//ユーザPARブロックアドレス取得
	static int getUserBaseBlockNo(int i){
		return Register.userPAR[i] << 20 >>> 20;
	}
	
	//コンディションコード設定
	static void setCC(boolean args_n,boolean args_z,boolean args_v,boolean args_c){
		c = args_c;
		z = args_z;
		n = args_n;
		v = args_v;
	}
	static boolean getC(){
		return c;
	}
	static boolean getZ(){
		return z;
	}
	static boolean getV(){
		return v;
	}
	static boolean getN(){
		return n;
	}
	
	//デバッグ用出力
	static void printDebug(){

		System.out.print("\n");
		
		System.out.print(String.format("%04x",Register.get(0) << 16 >>> 16));
		System.out.print(" " + String.format("%04x",Register.get(1) << 16 >>> 16));
		System.out.print(" " + String.format("%04x",Register.get(2) << 16 >>> 16));
		System.out.print(" " + String.format("%04x",Register.get(3) << 16 >>> 16));
		System.out.print(" " + String.format("%04x",Register.get(4) << 16 >>> 16));
		System.out.print(" " + String.format("%04x",Register.get(5) << 16 >>> 16));
		System.out.print(" " + String.format("%04x",Register.get(6) << 16 >>> 16));
		
		System.out.print(" ");

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

		/*
		System.out.print(" ");
		System.out.print(String.format("%04x",Memory.getMemory2(Memory.RKCS)));
		
		System.out.print(" ");
		System.out.print(String.format("%04x",Memory.getMemory2(Memory.UISA0)));

		System.out.print(" ");
		System.out.print(String.format("%04x",Memory.getMemory2(Memory.UISA1)));

		System.out.print(" ");
		System.out.print(String.format("%04x",Memory.getMemory2(0x9ec0)));

		System.out.print(" ");
		System.out.print(String.format("%04x",Memory.getMemory2(0xa2c0)));
		
		System.out.print(" ");
		System.out.print(String.format("%04x",Memory.getMemory2(Mmu.analyzeMemoryKernel(Register.get(6)))));

		System.out.print(" ");
		System.out.print(String.format("%04x",Memory.getMemory2(Mmu.analyzeMemoryKernel(Register.get(6)))));
		
		System.out.print(" ");
		System.out.print(String.format("%04x",Mmu.analyzeMemoryKernel(0xc000)));

		System.out.print(" ");
		System.out.print(String.format("%04x",Memory.getMemory2(Mmu.analyzeMemoryKernel(0xc000))));
		System.out.print(":");
		*/
		
		System.out.print(" ");
		System.out.print(String.format("%04x",Register.get(7)));
		System.out.print(":");
		
	}
}



