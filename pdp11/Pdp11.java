package pdp11;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class Pdp11{
  /*
   * ディスク設定
   */
  static final String RK0="v6root";
  static final String RK1="v6src";
  static final String RK2="v6doc";

  /*
   * テープ設定
   */
  static final String TM0="dist.tap";

  /*
   * モード設定
   */
  static int flgDebugMode = 0;
  static boolean flgDismMode = false;
  static boolean flgExeMode = false;
  static boolean flgTapeMode = false;
  static boolean flgOctMode = false;
  
  /*
   * 実行ファイル設定
   */
  static String argsFileName = "";
  
  public static void main(String[] args){
    /*
     * 実行モード設定
     */
    if(args.length < 1 || !(args[0].substring(0,1).equals("-"))){
      System.out.println("オプションを指定してください。\n-e:ディスクから実行 -t:テープから実行 -v:デバッグモード（16進数）で実行 -o:デバッグモード（8進数）で実行 -s:シンボルを出力して実行 -d:逆アセンブル");
      return;
    }
    
    //ディスクから実行
    if(args[0].equals("-e")) flgExeMode = true; 
    //テープから実行
    if(args[0].equals("-t")) flgTapeMode = true;
    //デバッグモード（16進数）で実行
    if(args[0].equals("-v")){
      flgDebugMode = 2; //デバッグ（すべて）
      flgExeMode = true; //ディスクから実行
    }
    //デバッグモード（8進数）で実行
    if(args[0].equals("-o")){
      flgDebugMode = 2; //デバッグ（すべて）
      flgExeMode = true; //ディスクから実行
      flgOctMode = true; //8進数
    }
    //シンボルを出力して実行
    if(args[0].equals("-s")){
      flgDebugMode = 1; //デバッグ（シンボルのみ）
      flgExeMode = true; //ディスクから実行
    }
    //逆アセンブル
    if(args[0].equals("-d")) flgDismMode = true;

    /*
     * ファイル名を設定して実行
     */
    try{
      //ファイル名を指定して実行する場合にファイル名を設定
      argsFileName = args[1];
    }catch(Exception e){
      //ファイル名が指定されていない場合はディスクまたはテープから実行する
      //CPUを生成
      Cpu cpu = new Cpu();

      //周辺装置をリセット
      Register.reset();
      Mmu.reset();
      Memory.reset();
      Kl11.reset();
      Rk11.reset();
      Tm11.reset();

      /*
       * テープから実行
       */
      if(flgTapeMode) {
        //TM11のBOOTROMをメモリのBOOT_START番地にロード
        Memory.load(Tm11.boot_rom, Tm11.BOOT_START);
        //BOOT_START番地をPCに設定
        Register.set(7,Tm11.BOOT_START);
        //実行
        cpu.execute();
      }

      /*
       * ディスクから実行
       */
      if(flgExeMode){
        //RK11のBOOTROMをメモリのBOOT_START番地にロード
        Memory.load(Rk11.boot_rom, Rk11.BOOT_START);
        //BOOT_START番地をPCに設定
        Register.set(7,Rk11.BOOT_START);
        //実行
        cpu.execute();
      }
      return;
    }
    
    //ファイル名が指定されている場合はファイルを逆アセンブラまたは実行する
    //バイナリ取得
    File file = new File(argsFileName);
    Path fileName = file.toPath();
    byte[] bf = null;
    try {
          bf = java.nio.file.Files.readAllBytes(fileName);
    } catch (IOException e) {
      e.printStackTrace();
    }
    
    //CPUを生成
    Cpu cpu = new Cpu();
    
    if(flgDismMode){
      /*
       * 逆アセンブル
       */
      //周辺装置リセット
      Register.reset();
      Memory.reset();
      
      //ファイルをメモリにロード
      Memory.load(bf, 0);
      try {
        //逆アセンブル実行
        cpu.dissAssemble();
      } catch (MemoryException e) {
        e.printStackTrace();
      }
    }

    if(flgExeMode){
      /*
       * ファイル名を指定して実行
       */
      //周辺装置リセット
      Register.reset();
      Mmu.reset();
      Memory.reset();
      Kl11.reset();
      Rk11.reset();
      
      //ファイルをメモリにロード
      Memory.fileload(bf);
      //実行
      cpu.execute();
    }
  }
}
