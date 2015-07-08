package pdp11;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

public class Cpu extends Thread {

	/*
	 * 変数定義
	 */
	int strnum; //逆アセンブラ出力用
	int exeCnt = 0; //実行回数制御
	ArrayList<Integer> dbgList; //スタックトレース出力用 
	ArrayList<Integer> rtnList; //スタックトレース出力用
	
	boolean waitFlg;
	
	Cpu(){

		dbgList = new ArrayList<Integer>();
		rtnList = new ArrayList<Integer>();
		
		waitFlg = false;
	}
	
	/*
	void load(String[] args,int argsNo){
		//バイナリ取得
		File file = new File(args[argsNo]);
		Path fileName = file.toPath();
		byte[] bf = null;
		try {
	        bf = java.nio.file.Files.readAllBytes(fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}

		//メモリにロード
		Memory.load(bf);
	}
	*/
	
	public void run(){
		if(Pdp11.flgDismMode) dissAssemble(); //逆アセンブル
		if(Pdp11.flgExeMode) execute(); //実行
	}

	//インタプリタ
	public void execute(){
		//Register.set(7,0); //PCを初期化

		FieldDto srcObj = new FieldDto();
		FieldDto dstObj = new FieldDto();

		boolean aprPrintFlg = false;
		
		//for(;Register.get(7)<Memory.textSize;){
		for(;;){
			
			exeCnt++;
			//if(exeCnt > 1000000) System.exit(0);
			
			if(Pdp11.flgDebugMode>1) printDebug(); //レジスタ・フラグ出力
			if(Pdp11.flgDebugMode==1) printCall(); //関数コール出力
			if(Pdp11.flgMemoryDump) printMemory(); //メモリダンプ出力

			if(Kl11.BR_PRI < Rk11.BR_PRI){
				if(Rk11.BR_PRI > Register.getPriority()){
					//if(Register.get(6) < 50110) System.exit(0);
					pushStack(Register.PSW);
					pushStack(Register.get(7));
					Register.set(7, getMemory2(Rk11.BR_VEC));
					Rk11.BR_PRI = 0;
					waitFlg = false;
				}
			}else{
				if(Kl11.BR_PRI > Register.getPriority()){
					//if(Register.get(6) < 50110) System.exit(0);
					pushStack(Register.PSW);
					pushStack(Register.get(7));
					Register.set(7, getMemory2(Kl11.BR_VEC));
					Kl11.BR_PRI = 0;
					waitFlg = false;
				}
			}

			if(!waitFlg){
			//ワーク
			int tmp = 0;
			
			int opnum = getMem(); //命令取得
			Opcode opcode = getOpcode(opnum); //ニーモニック取得

			if(Register.get(7)==0x7ca && !aprPrintFlg){
				aprPrintFlg = true;
				Memory.printPAR(); //メモリーマップ出力
			}
			
			switch(opcode){
			case ADC:
				dstObj = getField(dstObj,(opnum >> 3) & 7,opnum & 7);
				
				int adctmp = 0;
				if(Register.getC()) adctmp = 1;
				
				int adctmp2 = 0;
				if(dstObj.flgRegister){
					adctmp2 = Register.get(dstObj.register);
					tmp = Register.get(dstObj.register) + adctmp;
					Register.set(dstObj.register, tmp);
				}else if(dstObj.flgAddress){
					adctmp2 = getMemory2(dstObj.address);
					tmp = getMemory2(dstObj.address) + adctmp;
					setMemory2(dstObj.address, tmp);
				}else{
					adctmp2 = dstObj.operand;
					tmp = dstObj.operand + adctmp;
					setMemory2(dstObj.address, tmp);
				}

				Register.setCC((tmp << 16 >>> 31)>0, tmp==0, ((adctmp2 == 077777) && adctmp == 1), ((adctmp2 == -1) && adctmp == 1));
				
				break;
			case ADD:
				srcObj = getField(srcObj,(opnum >> 9) & 7,(opnum >> 6) & 7);
				dstObj = getField(dstObj,(opnum >> 3) & 7,opnum  & 7);
				tmp = srcObj.operand + dstObj.operand;
				
				if(dstObj.flgRegister){
					Register.set(dstObj.register, tmp);
				}else{
					setMemory2(dstObj.address, tmp);
				}				
				
				Register.setCC((tmp >> 15)>0, 
						tmp==0, 
						getAddOverflow(srcObj.operand, dstObj.operand, tmp), 
						getAddCarry(srcObj.operand, dstObj.operand, tmp));

				break;
			case ASH: 
                int ashReg = Register.get((opnum >> 6) & 7);
                srcObj = getField(srcObj,(opnum >> 3) & 7,opnum  & 7);
                int ashInt = srcObj.operand << 26;
                ashInt = ashInt >> 26;
                if(ashInt < 0){
                        ashReg = ashReg << 16;
                        ashReg = ashReg >> 16;
                        Register.set((opnum >> 6) & 7, ashReg >> Math.abs(ashInt));
                        Register.set((opnum >> 6) & 7, (Register.get((opnum >> 6) & 7) << 16) >>> 16);
                }else{
                        Register.set((opnum >> 6) & 7, ashReg << ashInt);
                        Register.set((opnum >> 6) & 7, (Register.get((opnum >> 6) & 7) << 16) >>> 16);
                }
                
                Register.setCC((Register.get((opnum >> 6) & 7) << 1 >>> 16)>0, //TODO
                                Register.get((opnum >> 6) & 7)==0, 
                                ((ashReg << 16 ) >>> 31) != ((Register.get((opnum >> 6) & 7) << 16) >>> 31), //TODO
                                false); //TODO
                
				break;
			case ASHC: //TODO
				int ashcReg1 = Register.get((opnum >> 6) & 7);
				int ashcReg2 = Register.get(((opnum >> 6) & 7) + 1);
				int ashcTmp = (ashcReg1 << 16) + (ashcReg2 << 16 >>> 16);
				
				srcObj = getField(srcObj,(opnum >> 3) & 7,opnum  & 7);
				int ashcInt = srcObj.operand << 26 >> 26;
			
				if(ashcInt < 0){
					tmp = ashcTmp >> Math.abs(ashcInt);
					Register.set((opnum >> 6) & 7, ashcTmp >> Math.abs(ashcInt) >>> 16);
					Register.set(((opnum >> 6) & 7)+1, ashcTmp >> Math.abs(ashcInt) << 16 >>> 16);
				}else{
					tmp = ashcTmp << Math.abs(ashcInt);
					Register.set((opnum >> 6) & 7, ashcTmp << Math.abs(ashcInt) >>> 16);
					Register.set(((opnum >> 6) & 7)+1, ashcTmp << Math.abs(ashcInt) << 16 >>> 16);
				}
				
				Register.setCC(tmp>0, //TODO
						tmp==0, 
						(ashcTmp >>> 31) != (tmp  >>> 31), //TODO
						false); //TODO
				
				break;
			case ASL:
				dstObj = getField(dstObj,(opnum >> 3) & 7,opnum  & 7);
				tmp = dstObj.operand << 1;
				
				if(dstObj.flgRegister){
					Register.set(dstObj.register, tmp);
				}else{
					setMemory2(dstObj.address, tmp);
				}				

				Register.setCC((tmp >> 15)>0, tmp==0, Register.getV(), (tmp >>> 16)>0);
				Register.setCC(Register.getN(), Register.getZ(), Register.getN()^Register.getC(), Register.getC());

				break;
			case ASR:
				dstObj = getField(dstObj,(opnum >> 3) & 7,opnum  & 7);
				tmp = dstObj.operand << 16 >> 16;
				tmp = tmp >> 1;
				
				if(dstObj.flgRegister){
					Register.set(dstObj.register, tmp);
				}else{
					setMemory2(dstObj.address, tmp);
				}				

				Register.setCC((tmp >> 15)>0, tmp==0, Register.getV(), (tmp >>> 16)>0);
				Register.setCC(Register.getN(), Register.getZ(), Register.getN()^Register.getC(), Register.getC());

				break;
			case BCC:
				if(Register.getC() == false) Register.set(7,getOffset(dstObj,(opnum >> 6) & 7,(opnum >> 3) & 7,opnum  & 7).address);
				break;
			case BCS:
				if(Register.getC() == true) Register.set(7,getOffset(dstObj,(opnum >> 6) & 7,(opnum >> 3) & 7,opnum  & 7).address);
				break;
			case BEQ:
				if(Register.getZ() == true) Register.set(7,getOffset(dstObj,(opnum >> 6) & 7,(opnum >> 3) & 7,opnum  & 7).address);
				break;
			case BGE:
				if(Register.getN() == Register.getV()) Register.set(7,getOffset(dstObj,(opnum >> 6) & 7,(opnum >> 3) & 7,opnum  & 7).address);
				break;
			case BGT:
				if(Register.getZ() == false && Register.getN() == Register.getV()) Register.set(7,getOffset(dstObj,(opnum >> 6) & 7,(opnum >> 3) & 7,opnum  & 7).address);
				break;
			case BHI:
				if(Register.getC() == false && Register.getZ() == false) Register.set(7,getOffset(dstObj,(opnum >> 6) & 7,(opnum >> 3) & 7,opnum  & 7).address);
				break;
			case BIC:
				srcObj = getField(srcObj,(opnum >> 9) & 7,(opnum >> 6) & 7);
				dstObj = getField(dstObj,(opnum >> 3) & 7,opnum  & 7);

				tmp = ~(srcObj.operand) & dstObj.operand;

				if(dstObj.flgRegister){
					Register.set(dstObj.register, tmp);
				}else{
					setMemory2(dstObj.address, tmp);
				}
				
				Register.setCC((tmp << 16 >>> 31) > 0, tmp==0, false, Register.getC());

				break;
			case BICB:
				srcObj = getField(srcObj,(opnum >> 9) & 7,(opnum >> 6) & 7,true);
				dstObj = getField(dstObj,(opnum >> 3) & 7,opnum  & 7,true);

				tmp = ~(srcObj.operand) & dstObj.operand;

				if(dstObj.flgRegister){
					Register.set(dstObj.register, tmp);
				}else{
					setMemory1(dstObj.address, tmp);
				}

				Register.setCC(tmp>0xFF, tmp==0, false, Register.getC());

				break;
			case BIS:
				srcObj = getField(srcObj,(opnum >> 9) & 7,(opnum >> 6) & 7);
				dstObj = getField(dstObj,(opnum >> 3) & 7,opnum  & 7);

				if(srcObj.flgRegister){
					if(dstObj.flgRegister){
						tmp = Register.get(srcObj.register) | Register.get(dstObj.register);
						Register.set(dstObj.register, Register.get(srcObj.register) | Register.get(dstObj.register));
					}else if(dstObj.flgAddress){
						tmp = Register.get(srcObj.register) | getMemory2(dstObj.address);
						setMemory2(dstObj.address, Register.get(srcObj.register) | getMemory2(dstObj.address));
					}

				}else if(srcObj.flgAddress){
					if(dstObj.flgRegister){
						tmp = getMemory2(srcObj.address) | Register.get(dstObj.register);
						Register.set(dstObj.register, getMemory2(srcObj.address) | Register.get(dstObj.register));
					}else if(dstObj.flgAddress){
						tmp = getMemory2(srcObj.address) | getMemory2(dstObj.address);
						setMemory2(dstObj.address, getMemory2(srcObj.address) | getMemory2(dstObj.address));
					}
				}else{
					if(dstObj.flgRegister){
						tmp = srcObj.operand | Register.get(dstObj.register);
						Register.set(dstObj.register, srcObj.operand | Register.get(dstObj.register));
					}else if(dstObj.flgAddress){
						tmp = srcObj.operand | getMemory2(dstObj.address);
						setMemory2(dstObj.address, srcObj.operand | getMemory2(dstObj.address));
					}
				}
				
				Register.setCC(false, //TODO 
						tmp==0, 
						false, 
						Register.getC());

				break;
			case BISB:
				srcObj = getField(srcObj,(opnum >> 9) & 7,(opnum >> 6) & 7);
				dstObj = getField(dstObj,(opnum >> 3) & 7,opnum  & 7);
				
				if(srcObj.flgRegister){
					if(dstObj.flgRegister){
						tmp = (Register.get(srcObj.register) | Register.get(dstObj.register)) << 24 >>> 24;
						Register.set(dstObj.register, (Register.get(srcObj.register) | Register.get(dstObj.register)) << 24 >>> 24);
					}else if(dstObj.flgAddress){
						tmp = (Register.get(srcObj.register) | getMemory2(dstObj.address)) << 24 >>> 24;
						setMemory1(dstObj.address, (Register.get(srcObj.register) | getMemory2(dstObj.address)) << 24 >>> 24);
					}

				}else if(srcObj.flgAddress){
					if(dstObj.flgRegister){
						tmp = (getMemory2(srcObj.address) | Register.get(dstObj.register)) << 24 >>> 24;
						Register.set(dstObj.register, (getMemory2(srcObj.address) | Register.get(dstObj.register)) << 24 >>> 24);
					}else if(dstObj.flgAddress){
						tmp = (getMemory2(srcObj.address) | getMemory2(dstObj.address)) << 24 >>> 24;
						setMemory1(dstObj.address, (getMemory2(srcObj.address) | getMemory2(dstObj.address)) << 24 >>> 24);
					}
				}else{
					if(dstObj.flgRegister){
						tmp = (srcObj.operand | Register.get(dstObj.register)) << 24 >>> 24;
						Register.set(dstObj.register, (srcObj.operand | Register.get(dstObj.register)) << 24 >>> 24);
					}else if(dstObj.flgAddress){
						tmp = (srcObj.operand | getMemory2(dstObj.address)) << 24 >>> 24;
						setMemory1(dstObj.address, (srcObj.operand | getMemory2(dstObj.address)) << 24 >>> 24);
					}
				}
				
				Register.setCC(false, //TODO
						tmp == 0, 
						false, 
						Register.getC());

				break;
			case BIT:
				srcObj = getField(srcObj,(opnum >> 9) & 7,(opnum >> 6) & 7);
				dstObj = getField(dstObj,(opnum >> 3) & 7,opnum  & 7);
				tmp = srcObj.operand & dstObj.operand;
				
				Register.setCC((tmp << 16 >>> 31) > 0, 
						tmp==0, 
						false, 
						Register.getC());
				
				break;
			case BITB:
				srcObj = getField(srcObj,(opnum >> 9) & 7,(opnum >> 6) & 7);
				dstObj = getField(dstObj,(opnum >> 3) & 7,opnum  & 7);
				tmp = srcObj.operand & dstObj.operand;
				tmp = tmp << 24 >>> 24;
				
				Register.setCC(false, //TODO 
						tmp==0, 
						false, 
						Register.getC());

				break;
			case BLE:
				if(Register.getZ() == true || Register.getN() != Register.getV()) Register.set(7,getOffset(dstObj,(opnum >> 6) & 7,(opnum >> 3) & 7,opnum  & 7).address);
				break;
			case BLOS:
				if(Register.getC() == true || Register.getZ() == true) Register.set(7,getOffset(dstObj,(opnum >> 6) & 7,(opnum >> 3) & 7,opnum  & 7).address);
				break;
			case BLT:
				if(Register.getN() != Register.getV()) Register.set(7,getOffset(dstObj,(opnum >> 6) & 7,(opnum >> 3) & 7,opnum  & 7).address);
				break;
			case BMI:
				if(Register.getN() == true) Register.set(7,getOffset(dstObj,(opnum >> 6) & 7,(opnum >> 3) & 7,opnum  & 7).address);
				break;
			case BNE:
				if(Register.getZ() == false) Register.set(7,getOffset(dstObj,(opnum >> 6) & 7,(opnum >> 3) & 7,opnum  & 7).address);
				break;
			case BPL:
				if(Register.getN() == false) Register.set(7,getOffset(dstObj,(opnum >> 6) & 7,(opnum >> 3) & 7,opnum  & 7).address);
				break;
			case BR:
				Register.set(7,getOffset(dstObj,(opnum >> 6) & 7,(opnum >> 3) & 7,opnum  & 7).address);
				break;
			case BVS:
				if(Register.getV() == true) Register.set(7,getOffset(dstObj,(opnum >> 6) & 7,(opnum >> 3) & 7,opnum  & 7).address);
				break;
			case CLR:
				dstObj = getField(dstObj,(opnum >> 3) & 7,opnum  & 7);
				if(dstObj.flgRegister){
					Register.set(dstObj.register, 0);
				}else{
					setMemory2(dstObj.address,0);
				}
				
				Register.setCC(false, true, false, false);
				
				break;
			case CLRB:
				dstObj = getField(dstObj,(opnum >> 3) & 7,opnum  & 7, true);
				if(dstObj.flgRegister){
					Register.set(dstObj.register, 0);
				}else{
					setMemory1(dstObj.address,0);
				}
				
				Register.setCC(false, true, false, false);
				
				break;
			case CMP:
				srcObj = getField(srcObj,(opnum >> 9) & 7,(opnum >> 6) & 7);
				dstObj = getField(dstObj,(opnum >> 3) & 7,opnum  & 7);
				tmp = (srcObj.operand << 16 >>> 16) - (dstObj.operand << 16 >>> 16);

				
				Register.setCC((tmp << 16 >>> 31)>0, 
						tmp==0, 
						getSubOverflow(srcObj.operand, dstObj.operand, tmp), 
						getSubBorrow(srcObj.operand, dstObj.operand, tmp));

				break;
			case CMPB:
				srcObj = getField(srcObj,(opnum >> 9) & 7,(opnum >> 6) & 7, true);
				dstObj = getField(dstObj,(opnum >> 3) & 7,opnum  & 7, true);

				tmp = (srcObj.operand << 24 >>> 24) - (dstObj.operand << 24 >>> 24);
				
				Register.setCC((tmp << 1 >>> 16)>0, 
						tmp==0, 
						getSubOverflow(srcObj.operand << 24 >>> 24, dstObj.operand << 24 >>> 24, tmp), 
						getSubBorrow(srcObj.operand << 24 >>> 24, dstObj.operand << 24 >>> 24, tmp));

				break;
			case COM:
				dstObj = getField(dstObj,(opnum >> 3) & 7,opnum  & 7);
				if(dstObj.flgRegister){
					Register.set(dstObj.register, ~dstObj.operand);
				}else{
					setMemory2(dstObj.address, ~dstObj.operand);
				}
				
				Register.setCC(((~dstObj.operand)<<16>>>31)>0, ((~dstObj.operand)<<16>>>16)==0, false, true);
				
				break;
			case DEC:
				dstObj = getField(dstObj,(opnum >> 3) & 7,opnum  & 7);
				
				if(dstObj.flgRegister){
					tmp = Register.get(dstObj.register) - 1;
					Register.set(dstObj.register, tmp);
				}else if(dstObj.flgAddress){
					tmp = getMemory2(dstObj.address) - 1;
					setMemory2(dstObj.address, tmp);
				}else{
					tmp = dstObj.operand - 1;
					setMemory2(dstObj.address, tmp);
				}

				Register.setCC((tmp << 16 >>> 31)>0, tmp==0, Register.getV(), Register.getC());
				
				break;
			case DECB:
				dstObj = getField(dstObj,(opnum >> 3) & 7,opnum  & 7, true);
				if(dstObj.flgRegister){
					tmp = Register.get(dstObj.register) - 1;
					Register.set(dstObj.register, tmp);
				}else if(dstObj.flgAddress){
					tmp = getMemory2(dstObj.address) - 1;
					setMemory2(dstObj.address, tmp);
				}else{
					tmp = dstObj.operand - 1;
					setMemory2(dstObj.address, tmp);
				}

				Register.setCC((tmp << 1 >>> 16)>0, tmp==0, Register.getV(), Register.getC());
				
				break;
			case DIV: 
				int divR1 = Register.get((opnum >> 6) & 7) << 16;
				int divR2 = Register.get(((opnum >> 6) & 7)+1);
				
				int divValue = divR1 + divR2;
				
				srcObj = getField(srcObj,(opnum >> 3) & 7,opnum & 7);
				
				Register.set((opnum >> 6) & 7, divValue / srcObj.operand);
				Register.set(((opnum >> 6) & 7)+1, divValue % srcObj.operand);

				Register.setCC((Register.get((opnum >> 6) & 7) >> 15)>0, 
						Register.get((opnum >> 6) & 7)==0, 
						srcObj.operand==0, //TODO
						srcObj.operand==0);
				
				break;
			case INC:
				dstObj = getField(dstObj,(opnum >> 3) & 7,opnum  & 7);
				if(dstObj.flgRegister){
					tmp = Register.get(dstObj.register) + 1;
					Register.set(dstObj.register, tmp);
				}else if(dstObj.flgAddress){
					tmp = getMemory2(dstObj.address) + 1;
					setMemory2(dstObj.address, tmp);
				}else{
					tmp = dstObj.operand + 1;
					setMemory2(dstObj.address, tmp);
				}

				Register.setCC((tmp << 1 >>> 16)>0, tmp==0, Register.getV(), Register.getC());

				break;
			case INCB:
				dstObj = getField(dstObj,(opnum >> 3) & 7,opnum  & 7,true);
				if(dstObj.flgRegister){
					tmp = Register.get(dstObj.register) + 1;
					Register.set(dstObj.register, tmp);
				}else if(dstObj.flgAddress){
					tmp = getMemory2(dstObj.address) + 1;
					setMemory1(dstObj.address, tmp);
				}else{
					tmp = dstObj.operand + 1;
					setMemory1(dstObj.address, tmp);
				}

				Register.setCC((tmp << 1 >>> 16)>0, tmp==0, Register.getV(), Register.getC());

				break;
			case JMP:
				dstObj = getField(dstObj,(opnum >> 3) & 7,opnum  & 7);

				tmp = Register.get(7);

				if(dstObj.flgRegister){
					Register.set(7,Register.get(dstObj.register));
				}else if(dstObj.flgAddress){
					Register.set(7,dstObj.address);
				}else{
					Register.set(7,dstObj.operand);
				}
				
				Register.setCC(Register.getN(), Register.getZ(), Register.getV(), Register.getC());

				pushCall(Register.get(7),tmp);
				printCall();

				break;
			case JSR:
				dstObj = getField(dstObj,(opnum >> 3) & 7,opnum  & 7);
				
				tmp = Register.get(7);
				
				pushStack(Register.get((opnum >> 6) & 7));
				Register.set((opnum >> 6) & 7,Register.get(7));
				Register.set(7, dstObj.address);

				Register.setCC(Register.getN(), Register.getZ(), Register.getV(), Register.getC());
				
				pushCall(Register.get(7),tmp);
				printCall();

				break;
			case MFPI:
				srcObj = getField(srcObj,(opnum >> 3) & 7,opnum  & 7);

				try{
					//tmp = getMemory2(srcObj.operand, Register.getPreMode());
					tmp = getMemory2(srcObj.address, Register.getPreMode());
					//System.out.printf("\nmfpi=%04x,%04x,%04x\n",srcObj.address,Register.getPreMode(),tmp);
					pushStack(tmp);
				}catch(ArrayIndexOutOfBoundsException e){
					int oldPSW = Register.PSW;
					int oldPC = Register.get(7);
					//pushStack(Register.PSW);
					//pushStack(Register.get(7));

					//Register.PSW = Register.PSW & 4095;
					//Register.PSW = Register.PSW | Memory.getMemory2(06);
					Register.PSW = Memory.getMemory2(06);
					Register.set(7, Memory.getMemory2(04));
					pushStack(oldPSW);
					pushStack(oldPC);
				}
				
				Register.setCC((srcObj.operand << 1 >>> 16)>0, srcObj.operand==0, false, Register.getC());
				
				break;
			case MOV:
				srcObj = getField(srcObj,(opnum >> 9) & 7,(opnum >> 6) & 7);
				dstObj = getField(dstObj,(opnum >> 3) & 7,opnum  & 7);

				if(srcObj.flgRegister){
					if(dstObj.flgRegister){
						Register.set(dstObj.register, Register.get(srcObj.register));
					}else if(dstObj.flgAddress){
						setMemory2(dstObj.address, Register.get(srcObj.register));
					}
				}else if(srcObj.flgAddress){
					if(dstObj.flgRegister){
						Register.set(dstObj.register, getMemory2(srcObj.address));
					}else if(dstObj.flgAddress){
						setMemory2(dstObj.address, getMemory2(srcObj.address));
					}
				}else{
					if(dstObj.flgRegister){
						Register.set(dstObj.register, srcObj.operand);
					}else if(dstObj.flgAddress){
						setMemory2(dstObj.address, srcObj.operand);
					}
				}

				Register.setCC((srcObj.operand << 1 >>> 16)>0, srcObj.operand==0, false, Register.getC());

				break;
			case MOVB:
				srcObj = getField(srcObj,(opnum >> 9) & 7,(opnum >> 6) & 7, true);
				dstObj = getField(dstObj,(opnum >> 3) & 7,opnum  & 7, true);
				
				if(srcObj.flgRegister){
					if(dstObj.flgRegister){
						//モード0の場合、符号拡張を行う
						tmp = Register.get(srcObj.register) << 24;
						tmp = tmp >> 24;
						Register.set(dstObj.register, tmp);
					}else if(dstObj.flgAddress){
						tmp = Register.get(srcObj.register);
						setMemory1(dstObj.address, tmp);
					}
				}else if(srcObj.flgAddress){
					if(dstObj.flgRegister){
						//モード0の場合、符号拡張を行う
						tmp = getMemory1(srcObj.address) << 24;
						tmp = tmp >> 24;
						Register.set(dstObj.register, tmp);
					}else if(dstObj.flgAddress){
						tmp = getMemory1(srcObj.address);
						setMemory1(dstObj.address, tmp);
					}
				}else{
					if(dstObj.flgRegister){
						//モード0の場合、符号拡張を行う
						tmp = srcObj.operand << 24;
						tmp = tmp >> 24;
						Register.set(dstObj.register, tmp);
					}else if(dstObj.flgAddress){
						tmp = srcObj.operand;
						setMemory1(dstObj.address, tmp);
					}
				}

				Register.setCC((tmp << 1 >>> 16)>0, tmp==0, false, Register.getC());
				
				break;
			case MTPI:
				dstObj = getField(dstObj,(opnum >> 3) & 7,opnum  & 7);
				
				try{
					tmp = popStack();
					setMemory2(dstObj.address, tmp, Register.getPreMode());
				}catch(ArrayIndexOutOfBoundsException e){
					System.out.println("catch mtpi");
					//Register.PSW = Register.PSW | 0x10;
					/*
					pushStack(Register.PSW);
					pushStack(Register.get(7));
					Register.PSW = Register.PSW & 4095;
					Register.PSW = Register.PSW | Memory.getMemory2(04);
					Register.set(7, Memory.getMemory2(04));
					*/
					
				}
				
				Register.setCC((dstObj.operand << 1 >>> 16)>0, dstObj.operand==0, false, Register.getC());

				break;
			case MUL: //TODO
				int mulR = Register.get((opnum >> 6) & 7);
				srcObj = getField(srcObj,(opnum >> 3) & 7,opnum  & 7);
				
				if(((opnum >> 6) & 7) %2 ==0){
					Register.set((opnum >> 6) & 7, (mulR * srcObj.operand >> 16) << 16);
					Register.set(((opnum >> 6) & 7)+1, (mulR * srcObj.operand << 16) >>> 16);
				}else{
					Register.set((opnum >> 6) & 7, (mulR * srcObj.operand << 16) >>> 16);
				}
				Register.setCC((mulR * srcObj.operand  >>> 15)>0, 
						mulR * srcObj.operand==0, 
						false,
						false);
				break;
			case NEG:
				dstObj = getField(dstObj,(opnum >> 3) & 7,opnum  & 7);
				tmp = (~(dstObj.operand << 16) >>> 16) + 1;
				
				if(dstObj.flgRegister){
					Register.set(dstObj.register, tmp);
				}else{
					setMemory2(dstObj.address, tmp);
				}				

				Register.setCC((tmp >> 15)>0, tmp==0, tmp==100000, tmp!=0);

				break;
			case RESET:
				Register.setCC(Register.getN(), Register.getZ(), Register.getV(), Register.getC());

				break;
				
			case RTS:
				Register.set(7,Register.get(opnum  & 7));
				Register.set(opnum  & 7,getMemory2(Register.get(6)));
				Register.add(6,2);
				
				Register.setCC(Register.getN(), Register.getZ(), Register.getV(), Register.getC());
				
				break;
			case ROL:
				dstObj = getField(dstObj,(opnum >> 3) & 7,opnum  & 7);
				int roltmp = 0;
				if(Register.getC()) roltmp = 1;
				if(dstObj.operand << 16 >>> 31 == 1) Register.c=true;
				if(dstObj.operand << 16 >>> 31 == 0) Register.c=false;
				
				if(dstObj.flgRegister){
					tmp = (Register.get(dstObj.register) << 1) + roltmp;
					Register.set(dstObj.register, tmp);
				}else if(dstObj.flgAddress){
					tmp =  (getMemory2(dstObj.address) << 1) + roltmp;
					setMemory2(dstObj.address, tmp);
				}else{
					tmp = (dstObj.operand >> 1) + roltmp;
					setMemory2(dstObj.address, tmp);
				}

				Register.setCC(Register.getN(), Register.getZ(), Register.getV(), Register.getC());

				break;
			case ROR:
				dstObj = getField(dstObj,(opnum >> 3) & 7,opnum  & 7);
				int rortmp = 0;
				if(Register.getC()) rortmp = 1;
				if(dstObj.operand << 31 >>> 31 == 1) Register.c=true;
				if(dstObj.operand << 31 >>> 31 == 0) Register.c=false;
				
				if(dstObj.flgRegister){
					tmp = (rortmp << 15) + (Register.get(dstObj.register) << 16 >>> 16 >> 1);
					Register.set(dstObj.register, tmp);
				}else if(dstObj.flgAddress){
					tmp =  (rortmp << 15) + (getMemory2(dstObj.address) << 16 >>> 16 >> 1);
					setMemory2(dstObj.address, tmp);
				}else{
					tmp =  (rortmp << 15) + (dstObj.operand << 16 >>> 16 >> 1);
					setMemory2(dstObj.address, tmp);
				}

				Register.setCC(Register.getN(), Register.getZ(), Register.getV(), Register.getC());

				break;
			case RTI:
			case RTT:
				//Register.PSW = Register.PSW & 4095;

				Register.set(7, popStack());
				Register.PSW = popStack();
				
				break;
			case SETD:
				break;
			case SEV:
				Register.setCC(Register.getN(), Register.getZ(), true, Register.getC());
				break;
			case SOB:
				short tmpShort = (short)(Register.get((opnum >> 6) & 7) - 1);
				Register.set((opnum >> 6) & 7,((Register.get((opnum >> 6) & 7) - 1) << 16) >>> 16);
				if(tmpShort != 0) Register.set(7,getOffset6(dstObj,(opnum >> 3) & 7,opnum  & 7).address);
				break;
			case SUB:
				srcObj = getField(srcObj,(opnum >> 9) & 7,(opnum >> 6) & 7);
				dstObj = getField(dstObj,(opnum >> 3) & 7,opnum  & 7);

				tmp = (dstObj.operand - srcObj.operand);
				tmp = tmp << 16 >>> 16;

				if(dstObj.flgRegister){
					Register.set(dstObj.register, tmp);
				}else{
					setMemory2(dstObj.address, tmp);
				}


				Register.setCC((tmp << 16 >>> 31)>0, 
						tmp==0, 
						getSubOverflow(srcObj.operand, dstObj.operand, tmp), 
						!getSubBorrow(srcObj.operand, dstObj.operand, tmp));
	
				break;
			case SWAB:
				dstObj = getField(dstObj,(opnum >> 3) & 7,opnum  & 7);
				
				tmp = (dstObj.operand << 16 >>> 24 ) + (dstObj.operand << 24 >>> 16);

				if(dstObj.flgRegister){
					Register.set(dstObj.register, tmp);
				}else if(dstObj.flgAddress){
					setMemory2(dstObj.address, tmp);
				}

				Register.setCC((tmp << 24 >>> 31)>0, tmp << 24 >>> 24 == 0, false, false);
				Register.setCC(Register.getN(), Register.getZ(), Register.getV(), Register.getC());

				break;
			case SXT:
				dstObj = getField(dstObj,(opnum >> 3) & 7,opnum  & 7);
				if(Register.getN() == true){
					if(dstObj.flgRegister){
						Register.set(dstObj.register, 0xffff);
					}else if(dstObj.flgAddress){
						setMemory2(dstObj.address, 0xffff);
					}
				}else{
					if(dstObj.flgRegister){
						Register.set(dstObj.register, 0);
					}else if(dstObj.flgAddress){
						setMemory2(dstObj.address, 0);
					}
				}
				
				Register.setCC(false,
						!Register.getN(), 
						false, 
						Register.getC());

				break;
			case SYS:
				int oldPSW = Register.PSW;
				int oldPC = Register.get(7);
				//pushStack(Register.PSW);
				//pushStack(Register.get(7));

				//Register.PSW = Register.PSW & 4095;
				//Register.PSW = Register.PSW | Memory.getMemory2(036);
				Register.PSW = Memory.getMemory2(036);
				Register.set(7, Memory.getMemory2(034));

				pushStack(oldPSW);
				pushStack(oldPC);
				
				break;
			case TST:
				dstObj = getField(dstObj,(opnum >> 3) & 7,opnum  & 7);
				Register.setCC((dstObj.operand << 16 >>> 31)>0, (dstObj.operand << 16 >>> 16)==0, false, false);
				break;
			case TSTB:
				dstObj = getField(dstObj,(opnum >> 3) & 7,opnum  & 7, true);
				Register.setCC((dstObj.operand << 24 >>> 31)>0, (dstObj.operand << 24 >>> 24)==0, false, false);
				break;
			case XOR:
				int srcreg = Register.get((opnum >> 6) & 7);
				dstObj = getField(dstObj,(opnum >> 3) & 7,opnum  & 7);
				tmp = srcreg^(dstObj.operand);
				
				if(dstObj.flgRegister){
					Register.set(dstObj.register, tmp);
				}else if(dstObj.flgAddress){
					setMemory2(dstObj.address, tmp);
				}
				
				Register.setCC((tmp << 16 >>> 31)>0, (tmp << 16 >>> 16)==0, false, Register.getC());
				break;
			case WAIT:
				waitFlg = true; 
				break;
			case WORD:
				System.out.print("\n");
				System.out.println("not case");
				System.out.println(getMemory2(Register.get(7)-2));
				
				//printMemory();
				System.exit(0);
				break;
			}
			}
		}
	}
	
	
	//フィールド取得（PC+オフセット*2 8bit（符号付））
	FieldDto getOffset(FieldDto operand,int first,int second,int third){
		operand.reset();
		operand.setAddress(Register.get(7) + ((byte)((first << 6) + (second << 3) + third)) * 2);
		return operand;
	}

	//フィールド取得（PC-オフセット*2 6bit（符号なし、正の数値））
	FieldDto getOffset6(FieldDto operand,int first,int second){
		operand.reset();
		operand.setAddress(Register.get(7) - ((first << 3) + second) * 2);
		return operand;
	}

	//フィールド取得（8進数 6bit）
	FieldDto getNormal(FieldDto operand,int first,int second,int third){
		operand.reset();
		operand.setAddress(((first << 3) + second) * 2 + Register.get(7));
		return operand;
	}

	//フィールド取得（dst,src）
	FieldDto getField(FieldDto field,int mode, int regNo){
		return getField(field, mode, regNo, false);
	}

	//フィールド取得（dst,src）
	FieldDto getField(FieldDto field,int mode, int regNo, boolean byteFlg){
		field.reset();
		
		//ワーク
		short opcodeShort;
		int opcodeInt;
		int tmp;

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
				field.setOperand(Register.get(regNo));
				field.setAddress(Register.get(regNo));
				field.setReg(regNo);
				break;
			case 1:
				//レジスタ間接
				//registerにオペランドのアドレスがある。
				if(byteFlg){
					field.setOperand(getMemory1(Register.get(regNo)));
					field.setAddress(Register.get(regNo));
				}else{
					field.setOperand(getMemory2(Register.get(regNo)));
					field.setAddress(Register.get(regNo));
				}
				break;
			case 2:
				//自動インクリメント
				//registerにオペランドのアドレスがあり、命令実行後にregisterの内容をインクリメントする。
				if(byteFlg){
					field.setOperand(getMemory1(Register.get(regNo)));
					field.setAddress(Register.get(regNo));
					if(regNo==6){
						Register.add(regNo,2);
					}else{
						Register.add(regNo,1);
					}
				}else{
					field.setOperand(getMemory2(Register.get(regNo)));
					field.setAddress(Register.get(regNo));
					Register.add(regNo,2);
				}
				break;
			case 3:
				//自動インクリメント間接
				//registerにオペランドへのポインタのアドレスがあり、命令実行後にregisterの内容を2だけインクリメントする。
				field.setOperand(getMemory2(getMemory2(Register.get(regNo))));
				field.setAddress(getMemory2(Register.get(regNo)));
				Register.add(regNo,2);
				break;
			case 4:
				//自動デクリメント
				//命令実行前にregisterをデクリメントし、それをオペランドのアドレスとして使用する。
				if(byteFlg){
					if(regNo==6){
						Register.add(regNo,-2);
					}else{
						Register.add(regNo,-1);
					}
					field.setOperand(getMemory1(Register.get(regNo)));
					field.setAddress(Register.get(regNo));
				}else{
					Register.add(regNo,-2);
					field.setOperand(getMemory2(Register.get(regNo)));
					field.setAddress(Register.get(regNo));
				}
				break;
			case 5:
				//自動デクリメント間接
				//命令実行前にregisterを2だけデクリメントし、それをオペランドへのポインタのアドレスとして使用する。
				Register.add(regNo,-2);
				field.setOperand(getMemory2(getMemory2(Register.get(regNo))));
				field.setAddress(getMemory2(Register.get(regNo)));
				break;
			case 6:
				//インデックス
				//register+Xがオペランドのアドレス。Xはこの命令に続くワード。
				opcodeShort = (short)getMem();
				if(byteFlg){
					field.setOperand(getMemory1(Register.get(regNo) + opcodeShort));
					field.setAddress(Register.get(regNo) + opcodeShort);
				}else{
					field.setOperand(getMemory2(Register.get(regNo) + opcodeShort));
					field.setAddress(Register.get(regNo) + opcodeShort);
				}
				break;
			case 7:
				//インデックス間接
				//register+Xがオペランドへのポインタのアドレス。Xはこの命令に続くワード。
				opcodeShort = (short)getMem();
				field.setOperand(getMemory2(getMemory2(Register.get(regNo) + opcodeShort)));
				field.setAddress(getMemory2(Register.get(regNo) + opcodeShort));
				break;
			}
			break;

		case 7:
			switch(mode){
			case 0:
				//レジスタ
				//registerにオペランドがある。
				field.setOperand(Register.get(regNo));
				field.setReg(regNo);
				break;
			case 1:
				//レジスタ間接
				//registerにオペランドのアドレスがある。
				if(byteFlg){
					field.setOperand(getMemory1(Register.get(regNo)));
					field.setAddress(Register.get(regNo));
				}else{
					field.setOperand(getMemory2(Register.get(regNo)));
					field.setAddress(Register.get(regNo));
				}
				break;			
			case 2:
				//イミディエート
				//オペランドは命令内にある。
				opcodeShort = (short)getMem();
				field.setOperand((int)opcodeShort);
				break;
			case 3:
				//絶対
				//オペランドの絶対アドレスが命令内にある。
				opcodeShort = (short)getMem();
				field.setAddress((int)opcodeShort);
				field.setOperand(getMemory2(field.address)); //TODO
				break;
			case 4:
				//自動デクリメント
				//命令実行前にregisterをデクリメントし、それをオペランドのアドレスとして使用する。
				if(byteFlg){
					Register.add(regNo,-2);
					field.setOperand(getMemory1(Register.get(regNo)));
					field.setAddress(Register.get(regNo));
				}else{
					Register.add(regNo,-2);
					field.setOperand(getMemory2(Register.get(regNo)));
					field.setAddress(Register.get(regNo));
				}
				break;
			case 5:
				//自動デクリメント間接
				//命令実行前にregisterを2だけデクリメントし、それをオペランドへのポインタのアドレスとして使用する。
				Register.add(regNo,-4);
				field.setOperand(getMemory2(getMemory2(Register.get(regNo))));
				field.setAddress(getMemory2(Register.get(regNo)));
				break;
			case 6:
				//相対
				//命令に続くワードの内容 a を PC+2 に加算したものをアドレスとして使用する。
				opcodeInt = (int)getMem() << 16 >>> 16;
				tmp = (opcodeInt + Register.get(7)) << 16 >>> 16;
				field.setOperand(getMemory2(tmp));
				field.setAddress(tmp);
				break;
			case 7:
				//相対間接
				//命令に続くワードの内容 a を PC+2 に加算したものをアドレスのアドレスとして使用する。
				opcodeInt = (int)getMem() << 16 >>> 16;
				tmp = opcodeInt + Register.get(7);
				field.setOperand(getMemory2(getMemory2(tmp))); //TODO
				field.setAddress(getMemory2(tmp));
				break;
			}
			break;
		}	
		return field;
	}

	



	/*
	 * ニーモニック
	 */
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
		SETD, SEV, SOB, SUB, SWAB, SXT, SYS,
		TST, TSTB,
		XOR,
		RESET,
		MFPI,MTPI,
		WAIT,
		WORD
	}
	
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
						case 6:
							switch(opnum  & 7){
							case 2:
								opcode = Opcode.SEV;
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
						opcode = Opcode.SYS;
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
	 * メモリアクセス関数
	 */
	//2バイト単位でリトルエンディアンを反転して10進数で取得
	static int getMemory2(int addr){
		return getMemory2(addr, Register.getNowMode());
	}
	static int getMemory2(int addr, int mode){
		int tmp = Memory.getMemory2(Mmu.analyzeMemory(addr, mode));
		if(tmp == Integer.MAX_VALUE){
			System.out.println("max value");
			System.exit(0);
		}
		return tmp;
	}	
	
	//1バイト単位で指定箇所のメモリを取得
	int getMemory1(int addr){
		return getMemory1(addr, Register.getNowMode());
	}
	int getMemory1(int addr,int mode){
		return Memory.getMemory1(Mmu.analyzeMemory(addr, mode));
	}

	//2バイト単位で指定箇所のメモリを更新
	void setMemory2(int addr,int src){
		setMemory2(addr, src, Register.getNowMode());
	}
	void setMemory2(int addr,int src,int mode){
		Memory.setMemory2(Mmu.analyzeMemory(addr, mode),src);
	}

	//1バイト単位で指定箇所のメモリを更新
	void setMemory1(int addr,int src){
		setMemory1(addr, src, Register.getNowMode());
	}
	void setMemory1(int addr,int src,int mode){
		Memory.setMemory1(Mmu.analyzeMemory(addr, mode),src);
	}
	
	//メモリ上のデータを取得して、PC+2する
	int getMem(){
		int opcode = getMemory2(Register.get(7));

		//逆アセンブル/デバッグモード（すべて）の場合は出力
		if(Pdp11.flgExeMode){
			if(Pdp11.flgDebugMode>1) printOpcode(opcode);
		}else{
			printOpcode(opcode);
			strnum++;
		}
		
		Register.add(7,2); //PC+2
		
		return opcode;
	}

	/*
	//カーネルスタックプッシュ
	void pushKernelStack(int n){
		Register.addKernelStack(-2);
		setMemory2(Register.reg[6],n,0);
	}
	*/
	
	//スタックプッシュ
	void pushStack(int n){
		Register.add(6,-2);
		setMemory2(Register.get(6),n);
	}
	
	//スタックポップ
	int popStack(){
		int tmp = getMemory2(Register.get(6));
		Register.add(6,2);
		return tmp;
	}
	

	/*
	 * データ編集関数
	 */
	//8進数から10進数に変換
	int getDex(int first,int second){
		return Integer.parseInt(Integer.toString(first * 10 + second), 8);
	}
	
	/*
	 * データ出力関数
	 */
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
	
	//指定した命令を出力
	void printOpcode(int opcode){
		System.out.print(String.format("%04x", opcode));
		System.out.print(" ");
	}

	//レジスタ・フラグの出力
	void printDebug(){
		popCall(Register.get(7));
		Register.printDebug();
	}
	
	//関数呼び出しをpush
	void pushCall(int pc,int nextPc){
		if(Util.isPush(pc)){
			dbgList.add(pc);
			rtnList.add(nextPc);
		}
	}

	//関数呼び出しをpop
	void popCall(int pc){
		int lastIndex = rtnList.lastIndexOf(pc);
		if( lastIndex > -1){
			rtnList.remove(lastIndex);
			dbgList.remove(lastIndex);
		}
	}
	
	//関数呼び出しをprint
	void printCall(){
		if(Pdp11.flgDebugMode>1 && dbgList.size() != 0){
			System.out.print("\n***StackTrace***\n");
			
			for(int i=0;i<dbgList.size(); i++){
				Util.printSub(dbgList.get(i));
				System.out.print(" - ");
			}
			System.out.print("\n****************");
		}
	}
	
	//メモリダンプの出力
	void printMemory(){
		System.out.print("\n--memory-start-------------");
		for(int m=0;m<253366;m++){
			if(m%16==0){
				System.out.print(String.format("\n%02x:",m/16));
			}
			System.out.print(String.format(" %02x",Memory.mem[m]));
		}
		System.out.println("\n--memory-end-------------");
	}
	
	/*
	 * オーバーフロー判定関数
	 */
	//加算オーバーフロー判定
	boolean getAddOverflow(int src, int dst, int val){
		boolean addV = false;
		if((dst << 1 >>> 16) == (src << 1 >>> 16)){
			if((dst << 1 >>> 16) != (val << 1 >>> 16)) addV = true;
		}
		return addV;
	}

	//減算オーバーフロー判定
	boolean getSubOverflow(int src, int dst, int val){
		boolean subV = false;
		if((dst << 16 >>> 31) != (src << 16 >>> 31)){
			if((dst << 16 >>> 31) == (val << 16 >>> 31)) subV = true;
		}
		return subV;
	}

	//加算キャリー判定
	boolean getAddCarry(int src, int dst, int val){
		boolean addC = false;
		if(((src << 16) >>> 31) == 1){
			if(((dst << 16) >>> 31) == 1){
				addC = true;
			}else{
				if(((val << 16) >>> 31) == 0) addC = true;
			}
		}else{
			if(((dst << 16) >>> 31) == 1){
				if(((val << 16) >>> 31) == 0) addC = true;
			}
		}
		return addC;
	}
	
	//減算ボロー判定
	boolean getSubBorrow(int src, int dst, int val){
		boolean subC = false;
		if(((src << 16) >>> 31) == 0){
			if(((dst << 16) >>> 31) == 1){
				subC = true;
			}else{
				if(((val << 16) >>> 31) == 1) subC = true;
			}
		}else{
			if(((dst << 16) >>> 31) == 1){
				if(((val << 16) >>> 31) == 1) subC = true;
			}
		}
		return subC;
	}

	
	/*
	 * 逆アセンブル関数
	 */ 
	void dissAssemble(){
		Pdp11.flgExeMode = false; //実行モードオフ
		
		//逆アセンブル
		for(Register.set(7, 0);Register.get(7)<Memory.MEMORY_SIZE;){

			//プログラムカウンタを出力
			System.out.print(String.format("%4x", Register.get(7)));
			System.out.print(":   ");

			strnum = 0;

			int opnum = getMem();
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
 			case SETD:
 				printDisasm("setd", "", "");
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
 			case SYS:
 				if(getDex((opnum >> 3) & 7,opnum  & 7) == 0) getMem();
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
			}
		}
	}
	
	void printDisasm(String mnemonic, String srcOperand, String dstOperand){
		for(;strnum<3;strnum++) System.out.print("     ");

		System.out.print(" ");
		System.out.print(mnemonic);

		for(int j=mnemonic.length();j<9;j++) System.out.print(" ");

		System.out.print(srcOperand);

		if(!(srcOperand.equals("")) && !(dstOperand.equals(""))) System.out.print(", ");

		System.out.print(dstOperand + "\n");
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
	String getOperandStr(int mode, int regNo){
		//ワーク
		int opcodeInt;
		int tmp;

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
				opcodeInt =  (short)getMem() << 16 >>> 16;
				return String.format("%o",opcodeInt) + "(" + getRegisterName(regNo) + ")";
			case 7:
				//インデックス間接
				//register+Xがオペランドへのポインタのアドレス。Xはこの命令に続くワード。
				opcodeInt =  (short)getMem() << 16 >>> 16;
				return "*-" + String.format("%o",opcodeInt) + "(" + getRegisterName(regNo) + ")";
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
				opcodeInt =  (short)getMem() << 16 >>> 16;
				return "$" + String.format("%o",opcodeInt);
			case 3:
				//絶対
				//オペランドの絶対アドレスが命令内にある。
				opcodeInt =  (short)getMem() << 16 >>> 16;
				return "*$" + String.format("%o",opcodeInt);
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
				opcodeInt = (int)getMem() << 16 >>> 16;
				tmp = opcodeInt + Register.get(7);
				tmp = tmp << 16 >>> 16;
				return "0x" + String.format("%02x",tmp);
			case 7:
				//相対間接
				//命令に続くワードの内容 a を PC+2 に加算したものをアドレスのアドレスとして使用する。
				opcodeInt = (int)getMem() << 16 >>> 16;
				tmp = opcodeInt + Register.get(7);
				return "*$0x" + String.format("%02x",(tmp));
			}
		}
		return "";
	}
}



/*
 * フィールドDto
 * for packing 1 line of asm
 */
class FieldDto{

	int operand;
	int address;
	int register;

	boolean flgRegister;
	boolean flgAddress;

	public void reset(){
		flgRegister = false;
		flgAddress = false;
		operand = 0;
		address = 0;
		register = 0;
	}

	public void setOperand(int input){
		operand = input;
		if(operand < 0){
			operand = operand << 16 >>> 16;
		}
	}

	public void setAddress(int input){
		address = input;
		if(address < 0){
			address = address << 16 >>> 16;
		}
		flgAddress = true;
	}

	public void setReg(int input){
		register = input;
		flgRegister = true;
	}

	public void set(FieldDto input){
		operand = input.operand;
		address = input.address;
		register = input.register;
		flgAddress = input.flgAddress;
		flgRegister = input.flgRegister;
	}
}



