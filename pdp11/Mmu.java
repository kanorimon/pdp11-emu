package pdp11;

public class Mmu {

	static int SR0;
	static int SR2;

	static void reset(){
		SR0 = 0;
		SR2 = 0;
	}
	
	static int analyzeMemory(int addr, int mode){
		if(mode == 0){
			addr = analyzeMemoryKernel(addr);
		}else{
			addr = analyzeMemoryUser(addr);
		}
		return addr;
	}
	
	static int analyzeMemoryKernel(int addr){
		if((SR0 & 1) == 1){
			int par = getPAR(addr);
			int blockno = addr << 19 >>> 25 << 6;
			int offset = addr << 26 >>> 26;
		
			return  (Register.getKernelBaseBlockNo(par) << 6) + blockno + offset;

		}else if(addr >= Memory.IOADDRV){
			return addr - Memory.IOADDRV + Memory.IOADDRP;
		}else{
			return addr;
		}
	}
	
	static int analyzeMemoryUser(int addr){
		if((SR0 & 1) == 1){
			int par = getPAR(addr);
			int blockno = addr << 19 >>> 25 << 6;
			int offset = addr << 26 >>> 26;
			
			if(Util.checkBit(Register.userPDR[par],3) == 1){
				int start_block = 128 - Register.getUserBlockCnt(par);

				System.out.printf(" user addr=%o phyaddr=%o baseblock=%o blockcnt=%o  startblock=%o blockno=%o offset=%o par=%o pdr=%o\n",
						addr,
						(Register.getUserBaseBlockNo(par)  << 6) + (start_block << 6) + blockno + offset,
						(Register.getUserBaseBlockNo(par)  << 6),
						Register.getUserBlockCnt(par),
						start_block,
						blockno,
						offset,
						Register.userPAR[par],
						Register.userPDR[par]);

				return (Register.getUserBaseBlockNo(par)  << 6) + (start_block << 6) + blockno + offset;
			}

			return (Register.getUserBaseBlockNo(par)  << 6)  + blockno + offset;
		}else{
			return addr;
		}
	}
	
	static int getPAR(int addr){
		return addr << 16 >>> 29;
	}
	
}
