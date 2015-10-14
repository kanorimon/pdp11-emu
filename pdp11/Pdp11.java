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
	static boolean flgOctMode = false;
	
	/*
	 * カーネル本体
	 */
	static String argsFileName = "";
	
	/*
	 * メイン処理
	 */
	public static void main(String[] args){
		//モード設定
		if(args.length < 1 || !(args[0].substring(0,1).equals("-"))){
			System.out.println("オプションを指定してください。\n-e:実行 -v:デバッグモードで実行 -o:デバッグモード（8進数表示）で実行 -s:シンボルを出力して実行 -d:逆アセンブル");
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
		if(args[0].equals("-o")){
			flgDebugMode = 2; //デバッグモード（すべて）
			flgExeMode = true; //実行モード
			flgOctMode = true; //8進数モード
		}
		if(args[0].equals("-d")) flgDismMode = true; //逆アセンブルモード
		if(args[0].equals("-e")) flgExeMode = true; //実行モード
		
		try{
			argsFileName = args[1];
		}catch(Exception e){
			/*
			 * RK11のBOOTROMからブート
			 */
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

				Memory.load(Rk11.boot_rom, 1024);
				Register.set(7,1024);

				cpu.start();
			}
			return;
		}
		
		/*
		 * ファイルを指定して実行
		 */
		//バイナリ取得
		File file = new File(argsFileName);
		Path fileName = file.toPath();
		byte[] bf = null;
		try {
	        bf = java.nio.file.Files.readAllBytes(fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}

		Cpu cpu = new Cpu();
		
		if(flgDismMode){
			
			//周辺装置リセット
			Register.reset();
			Memory.reset();
			
			Memory.load(bf, 0);
			cpu.dissAssemble();
		}

		if(flgExeMode){
			
			//周辺装置リセット
			Register.reset();
			Mmu.reset();
			Memory.reset();
			Kl11.reset();
			Rk11.reset();

			Kl11 kl11 = new Kl11();
			kl11.start();
			
			Memory.fileload(bf);
			cpu.start();
		}
	}
}



