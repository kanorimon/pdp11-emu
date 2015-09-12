package pdp11;

public class Mmu {

	static int SR0;
	static int SR1;
	static int SR2;
	
	static void reset(){
		SR0 = 0;
		SR1 = 0;
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
			int blockno = addr << 19 >>> 25;
			int offset = addr << 26 >>> 26;
		
			int btrblockno = (Register.getKernelBaseBlockNo(par) + blockno) << 6;
			 
			return btrblockno + offset;
		}else if(addr >= Memory.IOADDRV){
			return addr - Memory.IOADDRV + Memory.IOADDRP;
		}else{
			return addr;
		}
	}
	
	static int analyzeMemoryUser(int addr){
		if((SR0 & 1) == 1){
			int par = getPAR(addr);
			int blockno = addr << 19 >>> 25;
			int offset = addr << 26 >>> 26;
			
			int btrblockno = (Register.getUserBaseBlockNo(par) + blockno) << 6;

			return btrblockno + offset;
		}else{
			return addr;
		}
	}
	
	static int getPAR(int addr){
		return addr << 16 >>> 29;
	}
	
}
