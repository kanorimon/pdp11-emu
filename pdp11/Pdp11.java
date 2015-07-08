package pdp11;

public class Pdp11{
	/*
	 * モード設定
	 */
	static int flgDebugMode = 0;
	static boolean flgDismMode = false;
	static boolean flgExeMode = false;
	static boolean flgMemoryDump = false;
	
	/*
	 * メイン処理
	 */
	public static void main(String[] args){
		//モード設定
		int i = 0;
		while(true){
			if(!(args[i].substring(0,1).equals("-"))) break;

			if(args[i].equals("-s")) flgDebugMode = 1; //デバッグモード（システムコールのみ）
			if(args[i].equals("-v")) flgDebugMode = 2; //デバッグモード（すべて）
			if(args[i].equals("-m")) flgMemoryDump = true; //デバッグモード（メモリダンプ）
			if(args[i].equals("-d")) flgDismMode = true; //逆アセンブルモード
			if(args[i].equals("-e")) flgExeMode = true; //実行モード

			i++;
		}

		//オプション指定がなければ逆アセンブルモード
		if(flgDebugMode==0 && !flgDismMode && !flgExeMode && !flgMemoryDump){
			flgDismMode = true;
		}

		//CPUを生成
		Cpu cpu = new Cpu();
		
		//周辺装置リセット
		Unibus.reset();
		Register.reset();
		Mmu.reset();
		Memory.reset();
		Kl11.reset();
		Rk11.reset();

		if(flgExeMode){
			Kl11 kl11 = new Kl11();
			kl11.start();
			Rk11 rk11 = new Rk11();
			rk11.start();

			Memory.load(Rk11.boot_rom, 1024);
			Register.set(7,1024);

			cpu.start();
		}

		/*
		//実行
		cpu.load(args, i);
		cpu.start();
		
		//周辺装置実行
		if(flgExeMode){
			Kl11 kl11 = new Kl11();
			kl11.start();
			Rk11 rk11 = new Rk11();
			rk11.start();
		}
		*/
		
	}

}



