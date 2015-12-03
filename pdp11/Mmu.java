package pdp11;

public class Mmu {

	static int SR0;
	static int SR2;

	static void reset(){
		SR0 = 0;
		SR2 = 0;
	}
	
	static int analyzeMemory(int addr, int mode) throws MemoryException{
		if(mode == 0){
			addr = analyzeMemoryKernel(addr);
		}else{
			addr = analyzeMemoryUser(addr);
		}
		return addr;
	}
	
	static int analyzeMemoryKernel(int addr) throws MemoryException{
		if((SR0 & 1) == 1){
			int par = getPAR(addr);
			int blockno = addr << 19 >>> 25 << 6;
			int offset = addr << 26 >>> 26;
		
			int baseblockno = Register.getKernelBaseBlockNo(par)  << 6;
			int blockcnt = (Register.getKernelBlockCnt(par) + 1) << 6 ;

			if(Util.checkBit(Register.kernelPDR[par],1) == 0 && Util.checkBit(Register.kernelPDR[par],2) == 0) {
				//System.out.print("\n00kerror\n");
				Util.setBit(SR0, 15);
				SR2 = Cpu.pc;
				if(Pdp11.flgExeMode || Pdp11.flgTapeMode) throw new MemoryException();
			}
			if(Util.checkBit(Register.kernelPDR[par],3) == 1){
				if(blockcnt > blockno+offset){
					//System.out.printf("\nk1 addr=%o blockcnt=%o blockno=%o offset=%o baseblockno=%o\n",
					//		addr,blockcnt,blockno,offset,baseblockno);
					Util.setBit(SR0, 14);
					SR2 = Cpu.pc;
					if(Pdp11.flgExeMode || Pdp11.flgTapeMode) throw new MemoryException();
				}
			}else{
				if(blockcnt <= blockno+offset){
					//System.out.printf("\nk0 addr=%o blockcnt=%o blockno=%o offset=%o baseblockno=%o\n",
					//		addr,blockcnt,blockno,offset,baseblockno);
					Util.setBit(SR0, 14);
					SR2 = Cpu.pc;
					if(Pdp11.flgExeMode || Pdp11.flgTapeMode) throw new MemoryException();
				}
			}

			return baseblockno + blockno + offset;

		}else if(addr >= Memory.IOADDRV){
			return addr - Memory.IOADDRV + Memory.IOADDRP;
		}else{
			return addr;
		}
	}
	
	static int analyzeMemoryUser(int addr) throws MemoryException{
		if((SR0 & 1) == 1){
			int par = getPAR(addr);
			int blockno = addr << 19 >>> 25 << 6;
			int offset = addr << 26 >>> 26;
			
			int baseblockno = Register.getUserBaseBlockNo(par)  << 6;
			int blockcnt = (Register.getUserBlockCnt(par) + 1) << 6 ;

			if(Util.checkBit(Register.userPDR[par],1) == 0 && Util.checkBit(Register.userPDR[par],2) == 0) {
				//System.out.print("\n00uerror\n");
				Util.setBit(SR0, 15);
				SR2 = Cpu.pc;
				if(Pdp11.flgExeMode || Pdp11.flgTapeMode) throw new MemoryException();
			}
			if(Util.checkBit(Register.userPDR[par],3) == 1){
				if(blockcnt > blockno+offset){
					//System.out.printf("\nu1 addr=%o blockcnt=%o blockno=%o offset=%o baseblockno=%o\n",
					//		addr,blockcnt,blockno,offset,baseblockno);
					Util.setBit(SR0, 14);
					SR2 = Cpu.pc;
					if(Pdp11.flgExeMode || Pdp11.flgTapeMode) throw new MemoryException();
				}
			}else{
				if(blockcnt <= blockno+offset){
					//System.out.printf("\nu0 addr=%o blockcnt=%o blockno=%o offset=%o baseblockno=%o\n",
					//		addr,blockcnt,blockno,offset,baseblockno);
					Util.setBit(SR0, 14);
					SR2 = Cpu.pc;
					if(Pdp11.flgExeMode || Pdp11.flgTapeMode) throw new MemoryException();
				}
			}

			return baseblockno + blockno + offset;
		}else{
			return addr;
		}
	}
	
	static int getPAR(int addr){
		return addr << 16 >>> 29;
	}
	
}

class MemoryException extends Exception{
	public MemoryException() {
		super("アクセス不可領域にアクセスしました");
	}
}
