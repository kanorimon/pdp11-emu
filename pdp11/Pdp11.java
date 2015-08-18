package pdp11;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class Pdp11{
	/*
	 * モード設定
	 */
	static int flgDebugMode = 0;
	static boolean flgDismMode = false;
	static boolean flgExeMode = false;
	//static boolean flgMemoryDump = false;
	
	/*
	 * カーネル本体
	 */
	static String argsFileName = "";
	
	/*
	 * メイン処理
	 */
	public static void main(String[] args){
		//モード設定
		if(args.length < 2 || !(args[0].substring(0,1).equals("-"))){
			System.out.println("オプションを指定してください。\n-e:実行 -v:デバッグモードで実行 -d:逆アセンブル");
			return;
		}

		if(args[0].equals("-s")){
			flgDebugMode = 1; //デバッグモード（システムコールのみ）
			flgExeMode = true; //実行モード
		}
		if(args[0].equals("-v")){
			flgDebugMode = 2; //デバッグモード（すべて）
			flgExeMode = true; //実行モード
		}
		//if(args[0].equals("-m")) flgMemoryDump = true; //デバッグモード（メモリダンプ）
		if(args[0].equals("-d")) flgDismMode = true; //逆アセンブルモード
		if(args[0].equals("-e")) flgExeMode = true; //実行モード
		
		try{
			argsFileName = args[1];
		}catch(Exception e){
			//CPUを生成
			Cpu cpu = new Cpu();
			
			//周辺装置リセット
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
			return;
		}
		
		//バイナリ取得
		File file = new File(argsFileName);
		Path fileName = file.toPath();
		byte[] bf = null;
		try {
	        bf = java.nio.file.Files.readAllBytes(fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Register.reset();
		Memory.reset();

		Cpu cpu = new Cpu();
		
		if(flgDismMode){
			Memory.load(bf, 0);
			cpu.dissAssemble();
		}

		if(flgExeMode){
			Memory.fileload(bf);
			cpu.start();
		}
			
		/*
		//オプション指定がなければ逆アセンブルモード
		if((flgDebugMode==0 && !flgDismMode && !flgExeMode) || flgDismMode){
			//バイナリ取得
			File file = new File(args[i]);
			Path fileName = file.toPath();
			byte[] bf = null;
			try {
		        bf = java.nio.file.Files.readAllBytes(fileName);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			Register.reset();

			Memory.reset();
			Memory.load(bf, 0);

			Cpu cpu = new Cpu();
			cpu.dissAssemble();
			return;
		}

		//CPUを生成
		Cpu cpu = new Cpu();
		
		//周辺装置リセット
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
		*/


	}

}



