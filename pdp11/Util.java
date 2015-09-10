package pdp11;

public class Util {
	
	static int checkBit(int bits,int setNo){
		if((bits & (1 << setNo)) == 0){
			return 0;
		}else{
			return 1;
		}
	}
	
	static int setBit(int bits,int setNo){
		return (bits | (1 << setNo));
	}

	static int clearBit(int bits,int setNo){
		return (bits & (~(1 << setNo)));
	}

	/*
	static boolean isPush(int pc){

		boolean ret = false;
		
		if(Pdp11.argsFileName.length()!=0){
			switch(pc){
			case 0320:
			//case 0412:
			case 0524:
			case 0526:
			case 0634:
			case 01000:
			case 01154:
			case 02102:
			case 02134:
			case 02206:
			case 02252:
			case 02352:
			case 02366:
			case 02466:
			case 02510:
			case 02536:
			case 02552:
			case 02606:
			case 02616:
			case 02634:
			case 02652:
			case 02670:
			case 02700:
			case 03034:
			case 03124:
			case 03142:
			case 03230:
			case 03332:
			case 03514:
			case 03530:
			case 03546:
			case 03566:
			//case 03602:
			case 03620:
			case 04424:
			case 04604:
			case 05446:
			case 05472:
			case 05676:
			case 06232:
			case 06524:
			case 06612:
			case 07346:
			case 07436:
			case 07576:
			case 010056:
			case 010532:
			case 010662:
			case 011140:
			case 011530:
			case 011636:
			case 011742:
			case 012130:
			case 012226:
			case 012350:
			case 012412:
			case 012456:
			case 012540:
			case 013230:
			case 013730:
			case 013762:
			case 014014:
			case 014252:
			case 014450:
			case 014516:
			case 014610:
			case 014704:
			case 015562:
			case 016026:
			case 016514:
			case 017014:
			case 020052:
			case 020172:
			case 020304:
			case 020322:
			case 020332:
			case 020362:
			case 020624:
			case 020756:
			case 021456:
			case 021536:
			case 022506:
			case 022544:
			case 022562:
			case 022572:
			case 022644:
			case 022734:
			case 023026:
			case 023144:
			case 023440:
			case 023744:
			case 024222:
			case 024520:
			case 025216:
			case 026054:
			case 026230:
			case 026302:
			case 026420:
			case 026556:
			case 026722:
			case 027114:
			case 027202:
			case 027236:
			case 027314:
			case 027432:
			case 027536:
			case 027766:
			case 031046:
			case 031072:
			case 031136:
			case 031320:
			case 031526:
			case 032016:
			case 032066:
			case 032126:
			case 033662:
			case 033712:
			case 034302:
			case 034736:
			case 035076:
			case 035434:
			case 035454:
			case 035474:
			case 035720:
			case 035772:
			case 036100:
			case 036354:
			case 036420:
			case 036712:
			case 037144:
			case 037254:
			case 037512:
			case 037554:
			case 037626:
			case 040112:
			case 040162:
			case 040576:
			case 041020:
			case 041130:
			case 041146:
			case 041176:
			case 041246:
			case 041320:
			case 041350:
			case 041412:
			case 041442:
			case 041464:
			case 041500:
			case 041554:
			case 041754:
			case 042102:
			case 042172:
			case 042250:
			case 042362:
			case 042540:
			case 042604:
			case 042662:
			case 042764:
			case 043246:
			case 043356:
			case 043444:
			case 043472:
			case 043632:
			case 043724:
			case 044320:
			case 044376:
			case 044456:
			case 044542:
			case 044572:
			case 045022:
			case 045134:
			case 045250:
			case 045442:
			case 045504:
			case 045764:
			case 046074:
			case 046706:
			case 046746:
			case 047056:
			case 047146:
			case 047252:
			case 047342:
			case 047442:
			case 047564:
			case 050110:
			case 050412:
			case 051246:
			case 051276:
			case 051450:
			case 051546:
			case 051702:
			case 052016:
			case 052226:
			case 052264:
			case 052316:
			case 052350:
			case 052432:
			case 052516:
			case 052556:
			case 053004:
			case 053264:
			case 053452:
			case 053626:
			case 053702:
			case 054040:
			case 054100:
			case 054136:
			case 054172:
			case 054342:
			case 054532:
			case 055120:
			case 055200:
			case 055256:
			case 055350:
			case 055540:
			case 056060:
			case 056322:
			case 056404:
			case 056456:
			case 056534:
			case 056536:
			case 056540:
			case 056642:
			case 057104:
			case 057106:
			case 057110:
			case 057112:
			case 057114:
			case 057512:
			case 057712:
			case 060440:
			case 060700:
			case 061100:
			case 061102:
			case 061104:
			case 061116:
			case 061130:
			case 061142:
			case 061542:
			case 062052:
			case 062362:
			case 062364:
			case 062366:
			case 062370:
			case 062374:
			case 062400:
			case 062570:
			case 062626:
			case 062630:
			case 062632:
			case 062634:
			case 062636:
			case 062640:
			case 062642:
			case 062644:
			case 062646:
			case 064762:
			case 065602:
			case 074002:
			case 074004:
			case 074006:
			case 074556:
			case 074606:
			case 074610:
			case 076250:
			case 076260:
			case 0115316:
			case 0115346:
			case 0115360:
			case 0115362:
			case 0117022:
			case 0117062:
			case 0117112:
			case 0117122:
			case 0117152:
			case 0117162:
			case 0117202:
			case 0117224:
			case 0117226:
			case 0117234:
			case 0117240:
			case 0140000:
				ret = true;
				break;
			}
		}else{
			switch(pc){
			case 0320:
			//case 0412:
			case 0524:
			case 0526:
			case 0634:
			case 01000:
			case 01154:
			case 02102:
			case 02134:
			case 02206:
			case 02252:
			case 02352:
			case 02366:
			case 02466:
			case 02510:
			case 02536:
			case 02552:
			case 02606:
			case 02616:
			case 02634:
			case 02652:
			case 02670:
			case 02700:
			case 03034:
			case 03124:
			case 03142:
			case 03230:
			case 03332:
			case 03514:
			case 03530:
			case 03546:
			case 03566:
			//case 03602:
			case 03620:
			case 04466:
			case 04650:
			case 05544:
			case 05572:
			case 05776:
			case 06332:
			case 06624:
			case 06712:
			case 07446:
			case 07536:
			case 07676:
			case 010156:
			case 010632:
			case 010762:
			case 011240:
			case 011630:
			case 011736:
			case 012042:
			case 012230:
			case 012326:
			case 012450:
			case 012512:
			case 012556:
			case 012640:
			case 013330:
			case 014030:
			case 014062:
			case 014114:
			case 014352:
			case 014550:
			case 014616:
			case 014710:
			case 015004:
			case 015662:
			case 016126:
			case 016614:
			case 017114:
			case 020152:
			case 020272:
			case 020404:
			case 020422:
			case 020432:
			case 020462:
			case 020724:
			case 021056:
			case 021556:
			case 021636:
			case 022606:
			case 022644:
			case 022662:
			case 022672:
			case 022744:
			case 023034:
			case 023126:
			case 023244:
			case 023540:
			case 024044:
			case 024322:
			case 024620:
			case 025316:
			case 026154:
			case 026330:
			case 026402:
			case 026520:
			case 026656:
			case 027022:
			case 027214:
			case 027302:
			case 027336:
			case 027414:
			case 027532:
			case 027636:
			case 030066:
			case 031146:
			case 031172:
			case 031236:
			case 031420:
			case 031626:
			case 032116:
			case 032166:
			case 032226:
			case 033762:
			case 034012:
			case 034402:
			case 035036:
			case 035176:
			case 035534:
			case 035554:
			case 035574:
			case 036020:
			case 036072:
			case 036200:
			case 036454:
			case 036520:
			case 037012:
			case 037244:
			case 037354:
			case 037612:
			case 037654:
			case 037726:
			case 040212:
			case 040262:
			case 040676:
			case 041120:
			case 041230:
			case 041246:
			case 041276:
			case 041346:
			case 041420:
			case 041450:
			case 041512:
			case 041542:
			case 041564:
			case 041600:
			case 041654:
			case 042054:
			case 042202:
			case 042272:
			case 042350:
			case 042462:
			case 042640:
			case 042704:
			case 042762:
			case 043064:
			case 043346:
			case 043456:
			case 043544:
			case 043572:
			case 043732:
			case 044024:
			case 044420:
			case 044476:
			case 044556:
			case 044642:
			case 044672:
			case 045122:
			case 045234:
			case 045350:
			case 045542:
			case 045604:
			case 046064:
			case 046174:
			case 047006:
			case 047046:
			case 047156:
			case 047246:
			case 047352:
			case 047442:
			case 047542:
			case 047664:
			case 050210:
			case 050512:
			case 051346:
			case 051376:
			case 051550:
			case 051646:
			case 052002:
			case 052116:
			case 052326:
			case 052364:
			case 052416:
			case 052450:
			case 052532:
			case 052616:
			case 052656:
			case 053104:
			case 053364:
			case 053552:
			case 053726:
			case 054002:
			case 054140:
			case 054200:
			case 054236:
			case 054272:
			case 054442:
			case 054632:
			case 055220:
			case 055300:
			case 055356:
			case 055450:
			case 055640:
			case 056160:
			case 056422:
			case 056504:
			case 056556:
			case 056634:
			case 056636:
			case 056640:
			case 056742:
			case 057204:
			case 057206:
			case 057210:
			case 057212:
			case 057214:
			case 060022:
			case 060222:
			case 060750:
			case 061210:
			case 061410:
			case 061412:
			case 061414:
			case 061426:
			case 061440:
			case 061452:
			case 062052:
			case 062362:
			case 062672:
			case 062674:
			case 062676:
			case 062700:
			case 062704:
			case 062710:
			case 063100:
			case 063136:
			case 063140:
			case 063142:
			case 063144:
			case 063146:
			case 063150:
			case 063152:
			case 063154:
			case 063156:
			case 065272:
			case 066112:
			case 074312:
			case 074314:
			case 074316:
			case 075066:
			case 075116:
			case 075120:
			case 076560:
			case 076570:
			case 0115626:
			case 0115656:
			case 0115670:
			case 0115672:
			case 0117332:
			case 0117372:
			case 0117422:
			case 0117432:
			case 0117462:
			case 0117472:
			case 0117512:
			case 0117534:
			case 0117536:
			case 0117544:
			case 0117550:
			case 0140000:
				ret = true;
				break;
			}
		}
		return ret;
	}
	*/


	static void printSub(int pc){

		if(Pdp11.argsFileName.length()!=0){
			switch(pc){
				case 0320:
					System.out.print("trap");
					break;
				case 0412:
					//System.out.print("call");
					break;
				case 0524:
					System.out.print("_display");
					System.out.print("_savfp");
					break;
				case 0526:
					System.out.print("_incupc");
					break;
				case 0634:
					System.out.print("_getc");
					break;
				case 01000:
					System.out.print("_putc");
					break;
				case 01154:
					System.out.print("_backup");
					break;
				case 02102:
					System.out.print("_fubyte");
					System.out.print("_fuibyte");
					break;
				case 02134:
					System.out.print("_subyte");
					System.out.print("_suibyte");
					break;
				case 02206:
					System.out.print("_fuiword");
					System.out.print("_fuword");
					break;
				case 02252:
					System.out.print("_suiword");
					System.out.print("_suword");
					break;
				case 02352:
					System.out.print("_copyin");
					break;
				case 02366:
					System.out.print("_copyout");
					break;
				case 02466:
					System.out.print("_idle");
					break;
				case 02510:
					System.out.print("_savu");
					break;
				case 02536:
					System.out.print("_aretu");
					break;
				case 02552:
					System.out.print("_retu");
					break;
				case 02606:
					System.out.print("_spl0");
					break;
				case 02616:
					System.out.print("_spl1");
					break;
				case 02634:
					System.out.print("_spl4");
					System.out.print("_spl5");
					break;
				case 02652:
					System.out.print("_spl6");
					break;
				case 02670:
					System.out.print("_spl7");
					break;
				case 02700:
					System.out.print("_copyseg");
					break;
				case 03034:
					System.out.print("_clearse");
					break;
				case 03124:
					System.out.print("_dpadd");
					break;
				case 03142:
					System.out.print("_dpcmp");
					break;
				case 03230:
					System.out.print("dump");
					break;
				case 03332:
					System.out.print("start");
					break;
				case 03514:
					System.out.print("_ldiv");
					break;
				case 03530:
					System.out.print("_lrem");
					break;
				case 03546:
					System.out.print("_lshift");
					break;
				case 03566:
					//System.out.print("csv");
					break;
				case 03602:
					//System.out.print("cret");
					break;
				case 03620:
					System.out.print("_main");
					break;
				case 04424:
					System.out.print("_sureg");
					break;
				case 04604:
					System.out.print("_estabur");
					break;
				case 05446:
					System.out.print("_nseg");
					break;
				case 05472:
					System.out.print("_iinit");
					break;
				case 05676:
					System.out.print("_alloc");
					break;
				case 06232:
					System.out.print("_free");
					break;
				case 06524:
					System.out.print("_badbloc");
					break;
				case 06612:
					System.out.print("_ialloc");
					break;
				case 07346:
					System.out.print("_ifree");
					break;
				case 07436:
					System.out.print("_getfs");
					break;
				case 07576:
					System.out.print("_update");
					break;
				case 010056:
					System.out.print("_iget");
					break;
				case 010532:
					System.out.print("_iput");
					break;
				case 010662:
					System.out.print("_iupdat");
					break;
				case 011140:
					System.out.print("_itrunc");
					break;
				case 011530:
					System.out.print("_maknode");
					break;
				case 011636:
					System.out.print("_wdir");
					break;
				case 011742:
					System.out.print("_printf");
					break;
				case 012130:
					System.out.print("_printn");
					break;
				case 012226:
					System.out.print("_putchar");
					break;
				case 012350:
					System.out.print("_panic");
					break;
				case 012412:
					System.out.print("_prdev");
					break;
				case 012456:
					System.out.print("_deverro");
					break;
				case 012540:
					System.out.print("_readi");
					break;
				case 013230:
					System.out.print("_writei");
					break;
				case 013730:
					System.out.print("_max");
					break;
				case 013762:
					System.out.print("_min");
					break;
				case 014014:
					System.out.print("_iomove");
					break;
				case 014252:
					System.out.print("_sleep");
					break;
				case 014450:
					System.out.print("_wakeup");
					break;
				case 014516:
					System.out.print("_setrun");
					break;
				case 014610:
					System.out.print("_setpri");
					break;
				case 014704:
					System.out.print("_sched");
					break;
				case 015562:
					System.out.print("_swtch");
					break;
				case 016026:
					System.out.print("_newproc");
					break;
				case 016514:
					System.out.print("_expand");
					break;
				case 017014:
					System.out.print("_bmap");
					break;
				case 020052:
					System.out.print("_passc");
					break;
				case 020172:
					System.out.print("_cpass");
					break;
				case 020304:
					System.out.print("_nodev");
					break;
				case 020322:
					System.out.print("_nulldev");
					break;
				case 020332:
					System.out.print("_bcopy");
					break;
				case 020362:
					System.out.print("_xswap");
					break;
				case 020624:
					System.out.print("_xfree");
					break;
				case 020756:
					System.out.print("_xalloc");
					break;
				case 021456:
					System.out.print("_xccdec");
					break;
				case 021536:
					System.out.print("_trap");
					break;
				case 022506:
					System.out.print("_trap1");
					break;
				case 022544:
					System.out.print("_nosys");
					break;
				case 022562:
					System.out.print("_nullsys");
					break;
				case 022572:
					System.out.print("_signal");
					break;
				case 022644:
					System.out.print("_psignal");
					break;
				case 022734:
					System.out.print("_issig");
					break;
				case 023026:
					System.out.print("_stop");
					break;
				case 023144:
					System.out.print("_psig");
					break;
				case 023440:
					System.out.print("_core");
					break;
				case 023744:
					System.out.print("_grow");
					break;
				case 024222:
					System.out.print("_ptrace");
					break;
				case 024520:
					System.out.print("_procxmt");
					break;
				case 025216:
					System.out.print("_clock");
					break;
				case 026054:
					System.out.print("_timeout");
					break;
				case 026230:
					System.out.print("_getf");
					break;
				case 026302:
					System.out.print("_closef");
					break;
				case 026420:
					System.out.print("_closei");
					break;
				case 026556:
					System.out.print("_openi");
					break;
				case 026722:
					System.out.print("_access");
					break;
				case 027114:
					System.out.print("_owner");
					break;
				case 027202:
					System.out.print("_suser");
					break;
				case 027236:
					System.out.print("_ufalloc");
					break;
				case 027314:
					System.out.print("_falloc");
					break;
				case 027432:
					System.out.print("_malloc");
					break;
				case 027536:
					System.out.print("_mfree");
					break;
				case 027766:
					System.out.print("_namei");
					break;
				case 031046:
					System.out.print("_schar");
					break;
				case 031072:
					System.out.print("_uchar");
					break;
				case 031136:
					System.out.print("_pipe");
					break;
				case 031320:
					System.out.print("_readp");
					break;
				case 031526:
					System.out.print("_writep");
					break;
				case 032016:
					System.out.print("_plock");
					break;
				case 032066:
					System.out.print("_prele");
					break;
				case 032126:
					System.out.print("_exec");
					break;
				case 033662:
					System.out.print("_rexit");
					break;
				case 033712:
					System.out.print("_exit");
					break;
				case 034302:
					System.out.print("_wait");
					break;
				case 034736:
					System.out.print("_fork");
					break;
				case 035076:
					System.out.print("_sbreak");
					break;
				case 035434:
					System.out.print("_read");
					break;
				case 035454:
					System.out.print("_write");
					break;
				case 035474:
					System.out.print("_rdwr");
					break;
				case 035720:
					System.out.print("_open");
					break;
				case 035772:
					System.out.print("_creat");
					break;
				case 036100:
					System.out.print("_open1");
					break;
				case 036354:
					System.out.print("_close");
					break;
				case 036420:
					System.out.print("_seek");
					break;
				case 036712:
					System.out.print("_link");
					break;
				case 037144:
					System.out.print("_mknod");
					break;
				case 037254:
					System.out.print("_sslep");
					break;
				case 037512:
					System.out.print("_fstat");
					break;
				case 037554:
					System.out.print("_stat");
					break;
				case 037626:
					System.out.print("_stat1");
					break;
				case 040112:
					System.out.print("_dup");
					break;
				case 040162:
					System.out.print("_smount");
					break;
				case 040576:
					System.out.print("_sumount");
					break;
				case 041020:
					System.out.print("_getmdev");
					break;
				case 041130:
					System.out.print("_getswit");
					break;
				case 041146:
					System.out.print("_gtime");
					break;
				case 041176:
					System.out.print("_stime");
					break;
				case 041246:
					System.out.print("_setuid");
					break;
				case 041320:
					System.out.print("_getuid");
					break;
				case 041350:
					System.out.print("_setgid");
					break;
				case 041412:
					System.out.print("_getgid");
					break;
				case 041442:
					System.out.print("_getpid");
					break;
				case 041464:
					System.out.print("_sync");
					break;
				case 041500:
					System.out.print("_nice");
					break;
				case 041554:
					System.out.print("_unlink");
					break;
				case 041754:
					System.out.print("_chdir");
					break;
				case 042102:
					System.out.print("_chmod");
					break;
				case 042172:
					System.out.print("_chown");
					break;
				case 042250:
					System.out.print("_ssig");
					break;
				case 042362:
					System.out.print("_kill");
					break;
				case 042540:
					System.out.print("_times");
					break;
				case 042604:
					System.out.print("_profil");
					break;
				case 042662:
					System.out.print("_bread");
					break;
				case 042764:
					System.out.print("_breada");
					break;
				case 043246:
					System.out.print("_bwrite");
					break;
				case 043356:
					System.out.print("_bdwrite");
					break;
				case 043444:
					System.out.print("_bawrite");
					break;
				case 043472:
					System.out.print("_brelse");
					break;
				case 043632:
					System.out.print("_incore");
					break;
				case 043724:
					System.out.print("_getblk");
					break;
				case 044320:
					System.out.print("_iowait");
					break;
				case 044376:
					System.out.print("_notavai");
					break;
				case 044456:
					System.out.print("_iodone");
					break;
				case 044542:
					System.out.print("_clrbuf");
					break;
				case 044572:
					System.out.print("_binit");
					break;
				case 045022:
					System.out.print("_devstar");
					break;
				case 045134:
					System.out.print("_rhstart");
					break;
				case 045250:
					System.out.print("_mapallo");
					break;
				case 045442:
					System.out.print("_mapfree");
					break;
				case 045504:
					System.out.print("_swap");
					break;
				case 045764:
					System.out.print("_bflush");
					break;
				case 046074:
					System.out.print("_physio");
					break;
				case 046706:
					System.out.print("_geterro");
					break;
				case 046746:
					System.out.print("_gtty");
					break;
				case 047056:
					System.out.print("_stty");
					break;
				case 047146:
					System.out.print("_sgtty");
					break;
				case 047252:
					System.out.print("_wflusht");
					break;
				case 047342:
					System.out.print("_cinit");
					break;
				case 047442:
					System.out.print("_flushtt");
					break;
				case 047564:
					System.out.print("_canon");
					break;
				case 050110:
					System.out.print("_ttyinpu");
					break;
				case 050412:
					System.out.print("_ttyoutp");
					break;
				case 051246:
					System.out.print("_ttrstrt");
					break;
				case 051276:
					System.out.print("_ttstart");
					break;
				case 051450:
					System.out.print("_ttread");
					break;
				case 051546:
					System.out.print("_ttwrite");
					break;
				case 051702:
					System.out.print("_ttystty");
					break;
				case 052016:
					System.out.print("_klopen");
					break;
				case 052226:
					System.out.print("_klclose");
					break;
				case 052264:
					System.out.print("_klread");
					break;
				case 052316:
					System.out.print("_klwrite");
					break;
				case 052350:
					System.out.print("_klxint");
					break;
				case 052432:
					System.out.print("_klrint");
					break;
				case 052516:
					System.out.print("_klsgtty");
					break;
				case 052556:
					System.out.print("_mmread");
					break;
				case 053004:
					System.out.print("_mmwrite");
					break;
				case 053264:
					System.out.print("_rkstrat");
					break;
				case 053452:
					System.out.print("_rkaddr");
					break;
				case 053626:
					System.out.print("_rkstart");
					break;
				case 053702:
					System.out.print("_rkintr");
					break;
				case 054040:
					System.out.print("_rkread");
					break;
				case 054100:
					System.out.print("_rkwrite");
					break;
				case 054136:
					System.out.print("_tcclose");
					break;
				case 054172:
					System.out.print("_tcstrat");
					break;
				case 054342:
					System.out.print("_tcstart");
					break;
				case 054532:
					System.out.print("_tcintr");
					break;
				case 055120:
					System.out.print("_tmopen");
					break;
				case 055200:
					System.out.print("_tmclose");
					break;
				case 055256:
					System.out.print("_tcomman");
					break;
				case 055350:
					System.out.print("_tmstrat");
					break;
				case 055540:
					System.out.print("_tmstart");
					break;
				case 056060:
					System.out.print("_tmintr");
					break;
				case 056322:
					System.out.print("_tmread");
					break;
				case 056404:
					System.out.print("_tmwrite");
					break;
				case 056456:
					System.out.print("_tmphys");
					break;
				case 056534:
					System.out.print("_ka6");
					break;
				case 056536:
					System.out.print("_cputype");
					break;
				case 056540:
					System.out.print("_bdevsw");
					break;
				case 056642:
					System.out.print("_cdevsw");
					break;
				case 057104:
					System.out.print("_rootdev");
					break;
				case 057106:
					System.out.print("_swapdev");
					break;
				case 057110:
					System.out.print("_swplo");
					break;
				case 057112:
					System.out.print("_nswap");
					break;
				case 057114:
					System.out.print("_icode");
					break;
				case 057512:
					System.out.print("_regloc");
					break;
				case 057712:
					System.out.print("_sysent");
					break;
				case 060440:
					System.out.print("_maptab");
					break;
				case 060700:
					System.out.print("_partab");
					break;
				case 061100:
					System.out.print("_runrun");
					System.out.print("_edata");
					break;
				case 061102:
					System.out.print("_cfreeli");
					break;
				case 061104:
					System.out.print("_rktab");
					break;
				case 061116:
					System.out.print("_tmtab");
					break;
				case 061130:
					System.out.print("_tctab");
					break;
				case 061142:
					System.out.print("_canonb");
					break;
				case 061542:
					System.out.print("_coremap");
					break;
				case 062052:
					System.out.print("_swapmap");
					break;
				case 062362:
					System.out.print("_rootdir");
					break;
				case 062364:
					System.out.print("_execnt");
					break;
				case 062366:
					System.out.print("_lbolt");
					break;
				case 062370:
					System.out.print("_time");
					break;
				case 062374:
					System.out.print("_tout");
					break;
				case 062400:
					System.out.print("_callout");
					break;
				case 062570:
					System.out.print("_mount");
					break;
				case 062626:
					System.out.print("_mpid");
					break;
				case 062630:
					System.out.print("_runin");
					break;
				case 062632:
					System.out.print("_runout");
					break;
				case 062634:
					System.out.print("_curpri");
					break;
				case 062636:
					System.out.print("_maxmem");
					break;
				case 062640:
					System.out.print("_lks");
					break;
				case 062642:
					System.out.print("_updlock");
					break;
				case 062644:
					System.out.print("_rablock");
					break;
				case 062646:
					System.out.print("_proc");
					break;
				case 064762:
					System.out.print("_text");
					break;
				case 065602:
					System.out.print("_inode");
					break;
				case 074002:
					System.out.print("_nblkdev");
					break;
				case 074004:
					System.out.print("_nchrdev");
					break;
				case 074006:
					System.out.print("_buf");
					break;
				case 074556:
					System.out.print("_bfreeli");
					break;
				case 074606:
					System.out.print("_panicst");
					break;
				case 074610:
					System.out.print("_file");
					break;
				case 076250:
					System.out.print("_ipc");
					break;
				case 076260:
					System.out.print("_buffers");
					break;
				case 0115316:
					System.out.print("_swbuf");
					break;
				case 0115346:
					System.out.print("_httab");
					break;
				case 0115360:
					System.out.print("_maplock");
					break;
				case 0115362:
					System.out.print("_cfree");
					break;
				case 0117022:
					System.out.print("_kl11");
					break;
				case 0117062:
					System.out.print("_rrkbuf");
					break;
				case 0117112:
					System.out.print("_tcper");
					break;
				case 0117122:
					System.out.print("_rtmbuf");
					break;
				case 0117152:
					System.out.print("_t_openf");
					break;
				case 0117162:
					System.out.print("_t_blkno");
					break;
				case 0117202:
					System.out.print("_t_nxrec");
					break;
				case 0117224:
					System.out.print("nofault");
					break;
				case 0117226:
					System.out.print("ssr");
					break;
				case 0117234:
					System.out.print("badtrap");
					break;
				case 0117240:
					System.out.print("_end");
					break;
				case 0140000:
					System.out.print("_u");
					break;
			}
		}else{
			switch(pc){
				case 0320:
					System.out.print("trap");
					break;
				case 0412:
					//System.out.print("call");
					break;
				case 0524:
					System.out.print("_display");
					System.out.print("_savfp");
					break;
				case 0526:
					System.out.print("_incupc");
					break;
				case 0634:
					System.out.print("_getc");
					break;
				case 01000:
					System.out.print("_putc");
					break;
				case 01154:
					System.out.print("_backup");
					break;
				case 02102:
					System.out.print("_fubyte");
					System.out.print("_fuibyte");
					break;
				case 02134:
					System.out.print("_subyte");
					System.out.print("_suibyte");
					break;
				case 02206:
					System.out.print("_fuiword");
					System.out.print("_fuword");
					break;
				case 02252:
					System.out.print("_suiword");
					System.out.print("_suword");
					break;
				case 02352:
					System.out.print("_copyin");
					break;
				case 02366:
					System.out.print("_copyout");
					break;
				case 02466:
					System.out.print("_idle");
					break;
				case 02510:
					System.out.print("_savu");
					break;
				case 02536:
					System.out.print("_aretu");
					break;
				case 02552:
					System.out.print("_retu");
					break;
				case 02606:
					System.out.print("_spl0");
					break;
				case 02616:
					System.out.print("_spl1");
					break;
				case 02634:
					System.out.print("_spl4");
					System.out.print("_spl5");
					break;
				case 02652:
					System.out.print("_spl6");
					break;
				case 02670:
					System.out.print("_spl7");
					break;
				case 02700:
					System.out.print("_copyseg");
					break;
				case 03034:
					System.out.print("_clearse");
					break;
				case 03124:
					System.out.print("_dpadd");
					break;
				case 03142:
					System.out.print("_dpcmp");
					break;
				case 03230:
					System.out.print("dump");
					break;
				case 03332:
					System.out.print("start");
					break;
				case 03514:
					System.out.print("_ldiv");
					break;
				case 03530:
					System.out.print("_lrem");
					break;
				case 03546:
					System.out.print("_lshift");
					break;
				case 03566:
					//System.out.print("csv");
					break;
				case 03602:
					//System.out.print("cret");
					break;
				case 03620:
					System.out.print("_main");
					break;
				case 04466:
					System.out.print("_sureg");
					break;
				case 04650:
					System.out.print("_estabur");
					break;
				case 05544:
					System.out.print("_nseg");
					break;
				case 05572:
					System.out.print("_iinit");
					break;
				case 05776:
					System.out.print("_alloc");
					break;
				case 06332:
					System.out.print("_free");
					break;
				case 06624:
					System.out.print("_badbloc");
					break;
				case 06712:
					System.out.print("_ialloc");
					break;
				case 07446:
					System.out.print("_ifree");
					break;
				case 07536:
					System.out.print("_getfs");
					break;
				case 07676:
					System.out.print("_update");
					break;
				case 010156:
					System.out.print("_iget");
					break;
				case 010632:
					System.out.print("_iput");
					break;
				case 010762:
					System.out.print("_iupdat");
					break;
				case 011240:
					System.out.print("_itrunc");
					break;
				case 011630:
					System.out.print("_maknode");
					break;
				case 011736:
					System.out.print("_wdir");
					break;
				case 012042:
					System.out.print("_printf");
					break;
				case 012230:
					System.out.print("_printn");
					break;
				case 012326:
					System.out.print("_putchar");
					break;
				case 012450:
					System.out.print("_panic");
					break;
				case 012512:
					System.out.print("_prdev");
					break;
				case 012556:
					System.out.print("_deverro");
					break;
				case 012640:
					System.out.print("_readi");
					break;
				case 013330:
					System.out.print("_writei");
					break;
				case 014030:
					System.out.print("_max");
					break;
				case 014062:
					System.out.print("_min");
					break;
				case 014114:
					System.out.print("_iomove");
					break;
				case 014352:
					System.out.print("_sleep");
					break;
				case 014550:
					System.out.print("_wakeup");
					break;
				case 014616:
					System.out.print("_setrun");
					break;
				case 014710:
					System.out.print("_setpri");
					break;
				case 015004:
					System.out.print("_sched");
					break;
				case 015662:
					System.out.print("_swtch");
					break;
				case 016126:
					System.out.print("_newproc");
					break;
				case 016614:
					System.out.print("_expand");
					break;
				case 017114:
					System.out.print("_bmap");
					break;
				case 020152:
					System.out.print("_passc");
					break;
				case 020272:
					System.out.print("_cpass");
					break;
				case 020404:
					System.out.print("_nodev");
					break;
				case 020422:
					System.out.print("_nulldev");
					break;
				case 020432:
					System.out.print("_bcopy");
					break;
				case 020462:
					System.out.print("_xswap");
					break;
				case 020724:
					System.out.print("_xfree");
					break;
				case 021056:
					System.out.print("_xalloc");
					break;
				case 021556:
					System.out.print("_xccdec");
					break;
				case 021636:
					System.out.print("_trap");
					break;
				case 022606:
					System.out.print("_trap1");
					break;
				case 022644:
					System.out.print("_nosys");
					break;
				case 022662:
					System.out.print("_nullsys");
					break;
				case 022672:
					System.out.print("_signal");
					break;
				case 022744:
					System.out.print("_psignal");
					break;
				case 023034:
					System.out.print("_issig");
					break;
				case 023126:
					System.out.print("_stop");
					break;
				case 023244:
					System.out.print("_psig");
					break;
				case 023540:
					System.out.print("_core");
					break;
				case 024044:
					System.out.print("_grow");
					break;
				case 024322:
					System.out.print("_ptrace");
					break;
				case 024620:
					System.out.print("_procxmt");
					break;
				case 025316:
					System.out.print("_clock");
					break;
				case 026154:
					System.out.print("_timeout");
					break;
				case 026330:
					System.out.print("_getf");
					break;
				case 026402:
					System.out.print("_closef");
					break;
				case 026520:
					System.out.print("_closei");
					break;
				case 026656:
					System.out.print("_openi");
					break;
				case 027022:
					System.out.print("_access");
					break;
				case 027214:
					System.out.print("_owner");
					break;
				case 027302:
					System.out.print("_suser");
					break;
				case 027336:
					System.out.print("_ufalloc");
					break;
				case 027414:
					System.out.print("_falloc");
					break;
				case 027532:
					System.out.print("_malloc");
					break;
				case 027636:
					System.out.print("_mfree");
					break;
				case 030066:
					System.out.print("_namei");
					break;
				case 031146:
					System.out.print("_schar");
					break;
				case 031172:
					System.out.print("_uchar");
					break;
				case 031236:
					System.out.print("_pipe");
					break;
				case 031420:
					System.out.print("_readp");
					break;
				case 031626:
					System.out.print("_writep");
					break;
				case 032116:
					System.out.print("_plock");
					break;
				case 032166:
					System.out.print("_prele");
					break;
				case 032226:
					System.out.print("_exec");
					break;
				case 033762:
					System.out.print("_rexit");
					break;
				case 034012:
					System.out.print("_exit");
					break;
				case 034402:
					System.out.print("_wait");
					break;
				case 035036:
					System.out.print("_fork");
					break;
				case 035176:
					System.out.print("_sbreak");
					break;
				case 035534:
					System.out.print("_read");
					break;
				case 035554:
					System.out.print("_write");
					break;
				case 035574:
					System.out.print("_rdwr");
					break;
				case 036020:
					System.out.print("_open");
					break;
				case 036072:
					System.out.print("_creat");
					break;
				case 036200:
					System.out.print("_open1");
					break;
				case 036454:
					System.out.print("_close");
					break;
				case 036520:
					System.out.print("_seek");
					break;
				case 037012:
					System.out.print("_link");
					break;
				case 037244:
					System.out.print("_mknod");
					break;
				case 037354:
					System.out.print("_sslep");
					break;
				case 037612:
					System.out.print("_fstat");
					break;
				case 037654:
					System.out.print("_stat");
					break;
				case 037726:
					System.out.print("_stat1");
					break;
				case 040212:
					System.out.print("_dup");
					break;
				case 040262:
					System.out.print("_smount");
					break;
				case 040676:
					System.out.print("_sumount");
					break;
				case 041120:
					System.out.print("_getmdev");
					break;
				case 041230:
					System.out.print("_getswit");
					break;
				case 041246:
					System.out.print("_gtime");
					break;
				case 041276:
					System.out.print("_stime");
					break;
				case 041346:
					System.out.print("_setuid");
					break;
				case 041420:
					System.out.print("_getuid");
					break;
				case 041450:
					System.out.print("_setgid");
					break;
				case 041512:
					System.out.print("_getgid");
					break;
				case 041542:
					System.out.print("_getpid");
					break;
				case 041564:
					System.out.print("_sync");
					break;
				case 041600:
					System.out.print("_nice");
					break;
				case 041654:
					System.out.print("_unlink");
					break;
				case 042054:
					System.out.print("_chdir");
					break;
				case 042202:
					System.out.print("_chmod");
					break;
				case 042272:
					System.out.print("_chown");
					break;
				case 042350:
					System.out.print("_ssig");
					break;
				case 042462:
					System.out.print("_kill");
					break;
				case 042640:
					System.out.print("_times");
					break;
				case 042704:
					System.out.print("_profil");
					break;
				case 042762:
					System.out.print("_bread");
					break;
				case 043064:
					System.out.print("_breada");
					break;
				case 043346:
					System.out.print("_bwrite");
					break;
				case 043456:
					System.out.print("_bdwrite");
					break;
				case 043544:
					System.out.print("_bawrite");
					break;
				case 043572:
					System.out.print("_brelse");
					break;
				case 043732:
					System.out.print("_incore");
					break;
				case 044024:
					System.out.print("_getblk");
					break;
				case 044420:
					System.out.print("_iowait");
					break;
				case 044476:
					System.out.print("_notavai");
					break;
				case 044556:
					System.out.print("_iodone");
					break;
				case 044642:
					System.out.print("_clrbuf");
					break;
				case 044672:
					System.out.print("_binit");
					break;
				case 045122:
					System.out.print("_devstar");
					break;
				case 045234:
					System.out.print("_rhstart");
					break;
				case 045350:
					System.out.print("_mapallo");
					break;
				case 045542:
					System.out.print("_mapfree");
					break;
				case 045604:
					System.out.print("_swap");
					break;
				case 046064:
					System.out.print("_bflush");
					break;
				case 046174:
					System.out.print("_physio");
					break;
				case 047006:
					System.out.print("_geterro");
					break;
				case 047046:
					System.out.print("_gtty");
					break;
				case 047156:
					System.out.print("_stty");
					break;
				case 047246:
					System.out.print("_sgtty");
					break;
				case 047352:
					System.out.print("_wflusht");
					break;
				case 047442:
					System.out.print("_cinit");
					break;
				case 047542:
					System.out.print("_flushtt");
					break;
				case 047664:
					System.out.print("_canon");
					break;
				case 050210:
					System.out.print("_ttyinpu");
					break;
				case 050512:
					System.out.print("_ttyoutp");
					break;
				case 051346:
					System.out.print("_ttrstrt");
					break;
				case 051376:
					System.out.print("_ttstart");
					break;
				case 051550:
					System.out.print("_ttread");
					break;
				case 051646:
					System.out.print("_ttwrite");
					break;
				case 052002:
					System.out.print("_ttystty");
					break;
				case 052116:
					System.out.print("_klopen");
					break;
				case 052326:
					System.out.print("_klclose");
					break;
				case 052364:
					System.out.print("_klread");
					break;
				case 052416:
					System.out.print("_klwrite");
					break;
				case 052450:
					System.out.print("_klxint");
					break;
				case 052532:
					System.out.print("_klrint");
					break;
				case 052616:
					System.out.print("_klsgtty");
					break;
				case 052656:
					System.out.print("_mmread");
					break;
				case 053104:
					System.out.print("_mmwrite");
					break;
				case 053364:
					System.out.print("_rkstrat");
					break;
				case 053552:
					System.out.print("_rkaddr");
					break;
				case 053726:
					System.out.print("_rkstart");
					break;
				case 054002:
					System.out.print("_rkintr");
					break;
				case 054140:
					System.out.print("_rkread");
					break;
				case 054200:
					System.out.print("_rkwrite");
					break;
				case 054236:
					System.out.print("_tcclose");
					break;
				case 054272:
					System.out.print("_tcstrat");
					break;
				case 054442:
					System.out.print("_tcstart");
					break;
				case 054632:
					System.out.print("_tcintr");
					break;
				case 055220:
					System.out.print("_tmopen");
					break;
				case 055300:
					System.out.print("_tmclose");
					break;
				case 055356:
					System.out.print("_tcomman");
					break;
				case 055450:
					System.out.print("_tmstrat");
					break;
				case 055640:
					System.out.print("_tmstart");
					break;
				case 056160:
					System.out.print("_tmintr");
					break;
				case 056422:
					System.out.print("_tmread");
					break;
				case 056504:
					System.out.print("_tmwrite");
					break;
				case 056556:
					System.out.print("_tmphys");
					break;
				case 056634:
					System.out.print("_ka6");
					break;
				case 056636:
					System.out.print("_cputype");
					break;
				case 056640:
					System.out.print("_bdevsw");
					break;
				case 056742:
					System.out.print("_cdevsw");
					break;
				case 057204:
					System.out.print("_rootdev");
					break;
				case 057206:
					System.out.print("_swapdev");
					break;
				case 057210:
					System.out.print("_swplo");
					break;
				case 057212:
					System.out.print("_nswap");
					break;
				case 057214:
					System.out.print("_icode");
					break;
				case 060022:
					System.out.print("_regloc");
					break;
				case 060222:
					System.out.print("_sysent");
					break;
				case 060750:
					System.out.print("_maptab");
					break;
				case 061210:
					System.out.print("_partab");
					break;
				case 061410:
					System.out.print("_runrun");
					System.out.print("_edata");
					break;
				case 061412:
					System.out.print("_cfreeli");
					break;
				case 061414:
					System.out.print("_rktab");
					break;
				case 061426:
					System.out.print("_tmtab");
					break;
				case 061440:
					System.out.print("_tctab");
					break;
				case 061452:
					System.out.print("_canonb");
					break;
				case 062052:
					System.out.print("_coremap");
					break;
				case 062362:
					System.out.print("_swapmap");
					break;
				case 062672:
					System.out.print("_rootdir");
					break;
				case 062674:
					System.out.print("_execnt");
					break;
				case 062676:
					System.out.print("_lbolt");
					break;
				case 062700:
					System.out.print("_time");
					break;
				case 062704:
					System.out.print("_tout");
					break;
				case 062710:
					System.out.print("_callout");
					break;
				case 063100:
					System.out.print("_mount");
					break;
				case 063136:
					System.out.print("_mpid");
					break;
				case 063140:
					System.out.print("_runin");
					break;
				case 063142:
					System.out.print("_runout");
					break;
				case 063144:
					System.out.print("_curpri");
					break;
				case 063146:
					System.out.print("_maxmem");
					break;
				case 063150:
					System.out.print("_lks");
					break;
				case 063152:
					System.out.print("_updlock");
					break;
				case 063154:
					System.out.print("_rablock");
					break;
				case 063156:
					System.out.print("_proc");
					break;
				case 065272:
					System.out.print("_text");
					break;
				case 066112:
					System.out.print("_inode");
					break;
				case 074312:
					System.out.print("_nblkdev");
					break;
				case 074314:
					System.out.print("_nchrdev");
					break;
				case 074316:
					System.out.print("_buf");
					break;
				case 075066:
					System.out.print("_bfreeli");
					break;
				case 075116:
					System.out.print("_panicst");
					break;
				case 075120:
					System.out.print("_file");
					break;
				case 076560:
					System.out.print("_ipc");
					break;
				case 076570:
					System.out.print("_buffers");
					break;
				case 0115626:
					System.out.print("_swbuf");
					break;
				case 0115656:
					System.out.print("_httab");
					break;
				case 0115670:
					System.out.print("_maplock");
					break;
				case 0115672:
					System.out.print("_cfree");
					break;
				case 0117332:
					System.out.print("_kl11");
					break;
				case 0117372:
					System.out.print("_rrkbuf");
					break;
				case 0117422:
					System.out.print("_tcper");
					break;
				case 0117432:
					System.out.print("_rtmbuf");
					break;
				case 0117462:
					System.out.print("_t_openf");
					break;
				case 0117472:
					System.out.print("_t_blkno");
					break;
				case 0117512:
					System.out.print("_t_nxrec");
					break;
				case 0117534:
					System.out.print("nofault");
					break;
				case 0117536:
					System.out.print("ssr");
					break;
				case 0117544:
					System.out.print("badtrap");
					break;
				case 0117550:
					System.out.print("_end");
					break;
				case 0140000:
					System.out.print("_u");
					break;
			}
		}
	}
}
