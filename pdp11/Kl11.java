package pdp11;

public class Kl11 extends Thread {
	
	static int consoleSwitchRegister; //コンソールスイッチレジスタ
	static int XBUF; //出力バッファ
	static int XCSR; //出力レジスタ
	static int RBUF; //入力バッファ
	static int RCSR; //入力レジスタ
	
	static int BR_PRI; //割り込み優先度
	static int BR_VEC; //割り込みベクタ
	
	static void reset(){
		consoleSwitchRegister = 0;
		XBUF = 0;
		XCSR = 0;
		RBUF = 0;
		RCSR = 0;
		
		BR_PRI = 0;
		BR_VEC = 0;
	}
	
	public void run(){
		System.out.println("KL11 run");

		consoleSwitchRegister = 1; //電源ON
		XCSR = 128; //レディフラグON
		for(;;){
			try{
				  Thread.sleep(1);
			}catch (InterruptedException e){
			}
			if(XBUF != 0){
				switch(XBUF){
				case 010:
					System.out.print("");
					break;
				case 011:
					System.out.print(" ");
					break;
				case 012:
					System.out.print("\n");
					break;
				case 015:
					System.out.print("\r");
					break;
				default:
					//System.out.print("KL11OUT");
					System.out.printf("%c",XBUF);
				}
				XBUF = 0;
				XCSR = XCSR | 128;
				
				//割り込み
				if((XCSR & 64) != 0){
					BR_PRI = 4;
					BR_VEC = 064;
				}
			}
			
			
		}
	}

}
