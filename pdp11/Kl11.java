package pdp11;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class Kl11 extends Thread {
	
	static int consoleSwitchRegister; //コンソールスイッチレジスタ
	static int XBUF; //出力バッファ
	static int XCSR; //出力レジスタ
	static int RBUF; //入力バッファ
	static int RCSR; //入力レジスタ
	
	static int BR_PRI; //割り込み優先度
	static int BR_VEC; //割り込みベクタ
	
	static final int XCSR_READY = 7;
	static final int XCSR_ID = 6;

	static final int RCSR_BUSY = 11;
	static final int RCSR_DONE = 7;
	static final int RCSR_ID = 6;
	static final int RCSR_ENB = 0;
	
	static String inputStr;
	static byte[] inputByte;
	
	static void reset(){
		consoleSwitchRegister = 1;
		XBUF = 0;
		XCSR = 128;
		RBUF = 0;
		RCSR = 0;
		
		BR_PRI = 0;
		BR_VEC = 0;
		
		inputStr = "";
		inputByte = new byte[14];
		Arrays.fill(inputByte, (byte)0);
	}
	
	static int getRBUF(){
		if(inputByte[0] != 0){
			RBUF = inputByte[0];

			if(RBUF == 0xd){
				RCSR = Util.clearBit(RCSR,RCSR_DONE);
				RCSR = Util.setBit(RCSR,RCSR_ENB);
			}

			for(int i=0;i<13;i++){
				inputByte[i] = inputByte[i+1];
			}
		}
		return RBUF;
	}
	
	static void setXBUF(int xbuf){
		XBUF = xbuf;
		switch(xbuf){
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
			System.out.printf("%c",xbuf);
		}
		XBUF = 0;
		XCSR = Util.setBit(XCSR,7);
	}
	
	public void run(){
		System.out.println("KL11 run");

		CommandReceiver commandReceiver = new CommandReceiver();
        commandReceiver.start();
        
		for(;;){
			try{
				  Thread.sleep(1);
			}catch (InterruptedException e){
			}
			
			//キーボードからの入力バッファ
			if(inputStr.length() > 0){
				RCSR = Util.setBit(RCSR,RCSR_BUSY);
				
				for(int i=0;i<inputStr.length();i++){
					inputByte[i] = (byte)inputStr.charAt(i); 
				}
				inputByte[inputStr.length()] = 0xd;
				inputStr = "";
				
				RCSR = Util.setBit(RCSR,RCSR_DONE);
				RCSR = Util.clearBit(RCSR,RCSR_ENB);
				RCSR = Util.clearBit(RCSR,RCSR_BUSY);
				Cpu.exeCnt = 0;

			}
		}
	}
	
	// I/O処理用スレッド
    private class CommandReceiver extends Thread {
        public void run() {
            BufferedReader reader 
                = new BufferedReader(new InputStreamReader(System.in));
            try {
            	inputStr = reader.readLine();
            } catch (IOException e) {
            }
        }
    }

}
