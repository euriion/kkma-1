package org.snu.ids.ha.dic.compiler;


import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.StringTokenizer;

import org.snu.ids.ha.constants.Condition;
import org.snu.ids.ha.constants.POSTag;
import org.snu.ids.ha.core.MCandidate;
import org.snu.ids.ha.dic.ProbDicSet;
import org.snu.ids.ha.util.Convert;
import org.snu.ids.ha.util.Hangul;
import org.snu.ids.ha.util.RSHash;
import org.snu.ids.ha.util.Timer;
import org.snu.ids.ha.util.Util;


/**
 * <pre>
 * Singleton 으로 사용할 수 있는 형태소 사전
 * 표층 형태소에 의한 Set으로 구성되어 있다.
 * 표층 형태소에 대해 가질 수 있는,
 * 실재 형태소의 기분석 결과와와 이들의 접속 제한 조건등에 대한 정보를 가진다.
 * </pre>
 * @author 	therocks
 * @since	2007. 6. 4
 */
public class MorpDicCompiler
{
	public static void compile(String targetFileName)
	{
		MorpDicCompiler dicCompiler = new MorpDicCompiler();
		dicCompiler.compileToFile(targetFileName);
	}


	public static void print(String targetFileName)
	{
		MorpDicCompiler dicCompiler = new MorpDicCompiler();
		dicCompiler.printToFile(targetFileName);
	}


	final private Hashtable<String, MExpression>	table	= new Hashtable<String, MExpression>(530000);


	/**
	 * <pre>
	 * singleton으로 사용하기 위해 private 으로 지정함
	 * </pre>
	 * @author	therocks
	 * @since	2007. 6. 4
	 */
	private MorpDicCompiler()
	{
		Timer timer = new Timer();
		try {
			timer.start();
			loadDic();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			timer.stop();
			timer.printMsg("MorpDicCompiler Loading Time");
			System.out.println("Loaded Item " + table.size());
		}
	}


	/**
	 * <pre>
	 * 해당 표층형에 대한 가능한 기분석 결과를 추가한다.
	 * </pre>
	 * @author	therocks
	 * @since	2007. 6. 4
	 * @param exp
	 * @param mc
	 */
	private void add(String exp, MCandidate mc)
		throws Exception
	{
		// 품사 결합 확률 계산
		float lnprOfTagging = ProbDicSet.getLnprOfTagging(mc.getWordArr(), mc.getInfoEncArr());
		mc.setLnprOfTagging(lnprOfTagging);

		MExpression me = table.get(exp);

		if( me == null ) {
			me = new MExpression(exp, mc);
			float lnprOfSpacing = ProbDicSet.getLnprOfSpacing(exp);
			mc.setLnprOfSpacing(lnprOfSpacing);
			me.setLnprOfSpacing(lnprOfSpacing);
			table.put(exp, me);
		} else {
			mc.setLnprOfSpacing(me.getLnprOfSpacing());
			me.add(mc);
		}
	}


	private void loadDic()
		throws Exception
	{
		// simple
		loadSimple(DicCompiler.DIC_ROOT + "/00nng.txt");
		loadSimple(DicCompiler.DIC_ROOT + "/01nnp_person.txt");
		loadSimple(DicCompiler.DIC_ROOT + "/01nnp_place.txt");
		loadSimple(DicCompiler.DIC_ROOT + "/01nnp_chem.txt");
		loadSimple(DicCompiler.DIC_ROOT + "/01nnp_com.txt");
		loadSimple(DicCompiler.DIC_ROOT + "/02nnb.txt");
		loadSimple(DicCompiler.DIC_ROOT + "/03nr.txt");
		loadSimple(DicCompiler.DIC_ROOT + "/04np.txt");
		loadSimple(DicCompiler.DIC_ROOT + "/05comp.txt");
		loadSimple(DicCompiler.DIC_ROOT + "/06user.txt");
		loadSimple(DicCompiler.DIC_ROOT + "/10verb.txt");
		loadSimple(DicCompiler.DIC_ROOT + "/11vx.txt");
		loadSimple(DicCompiler.DIC_ROOT + "/12xr.txt");
		loadSimple(DicCompiler.DIC_ROOT + "/20mm.txt");
		loadSimple(DicCompiler.DIC_ROOT + "/21ma.txt");
		loadSimple(DicCompiler.DIC_ROOT + "/30ic.txt");
		loadSimple(DicCompiler.DIC_ROOT + "/40x.txt");
		// raw
		loadRaw(DicCompiler.DIC_ROOT + "/50josa.txt");
		loadRaw(DicCompiler.DIC_ROOT + "/51eomi.txt");
		loadRaw(DicCompiler.DIC_ROOT + "/52raw.txt");
	}


	/**
	 * <pre>
	 * 줄별로 사전 정의행을 읽어들여 기분석 사전을 구축한다.
	 * </pre>
	 * @author	Dongjoo
	 * @since	2009. 10. 21
	 * @param simpleDicReader
	 * @throws Exception
	 */
	private void loadSimple(String fileName)
		throws Exception
	{
		Timer timer = new Timer();
		timer.start();

		LineReader dicReader = null;
		String line = null;
		try {

			dicReader = new LineReader(fileName);

			while( (line = dicReader.readLine()) != null ) {
				MCandidate mCandidate = createSimple(line);
				String exp = mCandidate.getStringAt(0);
				mCandidate.initConds(exp);
				add(exp, mCandidate);

				// 활용되는 용언에 대한 확장형 추가
				if( mCandidate.isFirstTagOf(POSTag.V) ) {
					addVerbExtended(exp, mCandidate);
				}
				// 명사에 대한 변형 추가
				else if( mCandidate.isFirstTagOf(POSTag.N) && mCandidate.isHavingCond(Condition.MOEUM) ) {
					// 체언의 줄임말 표현, 아직 적용하지 않음 2011-07-15
					// addNounExtended(exp, mCandidate);
				}
			}
		} catch (Exception e) {
			System.err.println(line);
			throw e;
		} finally {
			dicReader.cleanup();
			timer.stop();
			timer.printMsg(fileName);
		}
	}


	private MCandidate createSimple(String line)
	{
		MCandidate mCandidate = new MCandidate();

		int idx = line.indexOf(';');
		String mpStr = null, condStr = null;
		if( idx > 0 ) {
			mpStr = line.substring(0, idx);
			condStr = line.substring(idx + 1);
		} else {
			mpStr = line;
		}

		// 형태소 추가
		mCandidate.addMorp(mpStr);

		// 부가 조건 추가
		if( condStr != null ) {
			// 부가 정보들에 대한 처리 수행
			StringTokenizer st = new StringTokenizer(condStr, MCandidate.DLMT_ATL + MCandidate.DLMT_HCL + MCandidate.DLMT_CCL + MCandidate.DLMT_ECL + MCandidate.DLMT_PRB + MCandidate.DLMT_CNL, true);
			while( st.hasMoreTokens() ) {
				String token = st.nextToken();
				// 접속 가능한 품사 정보
				if( token.equals(MCandidate.DLMT_ATL) ) {
					token = st.nextToken().trim();
					mCandidate.addApndblTags(token.substring(1, token.length() - 1).split(","));
				}
				// 현재 후보가 가진 접속 조건
				else if( token.equals(MCandidate.DLMT_HCL) ) {
					token = st.nextToken().trim();
					mCandidate.addHavingConds(token.substring(1, token.length() - 1).split(","));
				}
				// 접속할 때 확인해야 하는 조건
				else if( token.equals(MCandidate.DLMT_CCL) ) {
					token = st.nextToken().trim();
					mCandidate.addChkConds(token.substring(1, token.length() - 1).split(","));
				}
				// 접속할 때 배제해야 하는 조건
				else if( token.equals(MCandidate.DLMT_ECL) ) {
					token = st.nextToken().trim();
					mCandidate.addExclusionConds(token.substring(1, token.length() - 1).split(","));
				}
				// 분석 확률
				else if( token.equals(MCandidate.DLMT_PRB) ) {
					token = st.nextToken().trim();
					mCandidate.setLnprOfTagging(Integer.parseInt(token.substring(1, token.length() - 1)));
				}
				// 복합명사 분석 정보
				else if( token.equals(MCandidate.DLMT_CNL) ) {
					token = st.nextToken().trim();
					String[] arr = token.substring(1, token.length() - 1).split("\\+");
					char[][] compNounArr = new char[arr.length][];
					for( int i = 0; i < arr.length; i++ ) {
						compNounArr[i] = arr[i].toCharArray();
					}
					mCandidate.setCompNounArr(compNounArr);
				}
			}
		}

		return mCandidate;
	}


	/**
	 * <pre>
	 * 동사, 형용사의 기본형분석 후보를 받아들여, 사전에 저장될 표제어를 포함한 MCandidate을 저장
	 * </pre>
	 * @author	therocks
	 * @since	2009. 09. 29
	 * @param mCandidate
	 * @throws Exception 
	 */
	private void addVerbExtended(String stem, MCandidate mCandidate)
		throws Exception
	{
		int stemLen = stem.length();
		String preStem = stem.substring(0, stemLen - 1);

		char lastCh = stem.charAt(stemLen - 1), preLastCh = 0, mo = 0;
		Hangul lastHg = Hangul.split(lastCh), preLastHg = null;
		if( stemLen > 1 ) {
			preLastCh = stem.charAt(stemLen - 2);
			preLastHg = Hangul.split(preLastCh);
		} else {
			preLastCh = 0;
		}

		String exp = null;
		MCandidate mCandidateClone = null;

		// 사 주다 -> 사+아+주+다 와 같이 한글자 어간 'ㅏ'로 끝나는 말
		if( !lastHg.hasJong() && lastHg.cho != 'ㅎ' ) {
			exp = stem;
			if( lastHg.jung == 'ㅏ' || lastHg.jung == 'ㅐ' ) {
				mCandidateClone = mCandidate.clone();
				mCandidateClone.addMorp("아", POSTag.EC);
				mCandidateClone.clearHavingCondition();
				mCandidateClone.addHavingCond(Condition.MOEUM | Condition.YANGSEONG | Condition.AH);
				mCandidateClone.initBasicPhonemeCond(exp);
				add(exp, mCandidateClone);

				mCandidateClone = mCandidate.clone();
				mCandidateClone.addMorp("아", POSTag.EFN);
				mCandidateClone.clearHavingCondition();
				mCandidateClone.addHavingCond(Condition.MOEUM | Condition.YANGSEONG | Condition.AH);
				mCandidateClone.initBasicPhonemeCond(exp);
				add(exp, mCandidateClone);
			} else if( lastHg.jung == 'ㅓ' ) {
				mCandidateClone = mCandidate.clone();
				mCandidateClone.addMorp("어", POSTag.EC);
				mCandidateClone.clearHavingCondition();
				mCandidateClone.addHavingCond(Condition.MOEUM | Condition.EUMSEONG | Condition.AH);
				mCandidateClone.initBasicPhonemeCond(exp);
				add(exp, mCandidateClone);

				mCandidateClone = mCandidate.clone();
				mCandidateClone.addMorp("어", POSTag.EFN);
				mCandidateClone.clearHavingCondition();
				mCandidateClone.addHavingCond(Condition.MOEUM | Condition.EUMSEONG | Condition.AH);
				mCandidateClone.initBasicPhonemeCond(exp);
				add(exp, mCandidateClone);
			}
		}

		// 겹모음 'ㄶ'의 경우 'ㅎ'을 빼먹고 사용하는 경우가 많으므로 이를 처리해줌
		if( lastCh == '찮' || lastCh == '잖' ) {
			mCandidateClone = mCandidate.clone();
			exp = preStem + Hangul.combine(lastHg.cho, lastHg.jung, 'ㄴ');
			add(exp, mCandidateClone);
		}

		// 과거형 붙여주기
		if( lastCh == '하' ) {
			// 했 -> 하였
			mCandidateClone = mCandidate.clone();
			exp = preStem + "했";
			//mCandidateClone.addMorp("였", POSTag.EPT);
			mCandidateClone.addMorp("었", POSTag.EPT);
			mCandidateClone.clearHavingCondition();
			mCandidateClone.addHavingCond(Condition.EUT);
			mCandidateClone.initBasicPhonemeCond(exp);
			add(exp, mCandidateClone);

			// 해 -> 하여
			mCandidateClone = mCandidate.clone();
			exp = preStem + "해";
			//mCandidateClone.addMorp("여", POSTag.EC);
			mCandidateClone.addMorp("어", POSTag.EC);
			mCandidateClone.clearHavingCondition();
			mCandidateClone.addHavingCond(Condition.AH);
			mCandidateClone.initBasicPhonemeCond(exp);
			add(exp, mCandidateClone);

			// 종결형
			mCandidateClone = mCandidate.clone();
			exp = preStem + "해";
			//mCandidateClone.addMorp("여", POSTag.EFN);
			mCandidateClone.addMorp("어", POSTag.EFN);
			mCandidateClone.clearHavingCondition();
			mCandidateClone.addHavingCond(Condition.AH);
			mCandidateClone.initBasicPhonemeCond(exp);
			add(exp, mCandidateClone);

			// 형용사는 하지 -> 치 로 줄여질 수 있다.
			if( mCandidate.isLastTagOf(POSTag.VA | POSTag.VXA) ) {
				mCandidateClone = mCandidate.clone();
				exp = preStem + "치";
				mCandidateClone.addMorp("지", POSTag.EC);
				mCandidateClone.clearHavingCondition();
				mCandidateClone.initBasicPhonemeCond(exp);
				add(exp, mCandidateClone);
			}
		}
		// '이'로 끝나는 말
		else if( !lastHg.hasJong() && lastHg.jung == 'ㅣ' ) {
			// ㅣ -> ㅣ었->ㅕㅆ
			mCandidateClone = mCandidate.clone();
			exp = preStem + Hangul.combine(lastHg.cho, 'ㅕ', 'ㅆ');
			mCandidateClone.addMorp("었", POSTag.EPT);
			mCandidateClone.clearHavingCondition();
			mCandidateClone.addHavingCond(Condition.EUT);
			mCandidateClone.initBasicPhonemeCond(exp);
			add(exp, mCandidateClone);

			// ㅣ -> ㅣ어->ㅕ
			mCandidateClone = mCandidate.clone();
			exp = preStem + Hangul.combine(lastHg.cho, 'ㅕ', ' ');
			mCandidateClone.addMorp("어", POSTag.EC);
			mCandidateClone.clearHavingCondition();
			mCandidateClone.addHavingCond(Condition.MOEUM | Condition.EUMSEONG | Condition.AH);
			mCandidateClone.initBasicPhonemeCond(exp);
			add(exp, mCandidateClone);

			// ㅣ -> ㅣ어->ㅕ
			mCandidateClone = mCandidate.clone();
			exp = preStem + Hangul.combine(lastHg.cho, 'ㅕ', ' ');
			mCandidateClone.addMorp("어", POSTag.EFN);
			mCandidateClone.clearHavingCondition();
			mCandidateClone.addHavingCond(Condition.MOEUM | Condition.EUMSEONG | Condition.AH);
			mCandidateClone.initBasicPhonemeCond(exp);
			add(exp, mCandidateClone);
		}
		// ㅆ, ㅏㅆ, ㅐㅆ, ㅕㅆ  결합에 의한 어간 출력
		else if( !lastHg.hasJong() && isMoSet1(lastHg.jung) ) {
			mCandidateClone = mCandidate.clone();
			exp = preStem + Hangul.combine(lastHg.cho, lastHg.jung, 'ㅆ');
			mCandidateClone.addMorp("었", POSTag.EPT);
			mCandidateClone.clearHavingCondition();
			mCandidateClone.addHavingCond(Condition.EUT);
			mCandidateClone.initBasicPhonemeCond(exp);
			add(exp, mCandidateClone);
		}
		// '르'불규칙
		else if( lastCh == '르' ) {
			// 았
			mCandidateClone = mCandidate.clone();
			mCandidateClone.clearHavingCondition();
			// '르'불규칙 용언 중 으 탈란
			if( "다따들러치".indexOf(preLastCh) > -1 ) {
				exp = preStem + "랐";
				mCandidateClone.addMorp("았", POSTag.EPT);
				mCandidateClone.addHavingCond(Condition.EUT);
			} 
			// '러'불규칙 용언 중 으 탈란
			else if( "이푸".indexOf(preLastCh) > -1 ) {
				exp = stem + "렀";
				mCandidateClone.addMorp("었", POSTag.EPT);
				mCandidateClone.addHavingCond(Condition.EUT);
			} else {
				mo = getMoeum(lastHg, preLastHg);
				exp = stem.substring(0, stemLen - 2) + Hangul.combine(preLastHg.cho, preLastHg.jung, 'ㄹ') + Hangul.combine(lastHg.cho, mo, 'ㅆ');
				if( mo == 'ㅏ' ) {
					mCandidateClone.addMorp("았", POSTag.EPT);
				} else {
					mCandidateClone.addMorp("었", POSTag.EPT);
				}
				mCandidateClone.addHavingCond(Condition.EUT);
			}
			mCandidateClone.initBasicPhonemeCond(exp);

			add(exp, mCandidateClone);

			// 아
			mCandidateClone = mCandidate.clone();
			mCandidateClone.clearHavingCondition();
			if( "다따들러치".indexOf(preLastCh) > -1 ) {
				exp = preStem + "라";
				mCandidateClone.addMorp("아", POSTag.EC);
				mCandidateClone.addHavingCond(Condition.AH);
			} else if( "이푸".indexOf(preLastCh) > -1 ) {
				exp = stem + "러";
				mCandidateClone.addMorp("어", POSTag.EC);
				mCandidateClone.addHavingCond(Condition.AH);
			} else {
				mo = getMoeum(lastHg, preLastHg);
				exp = stem.substring(0, stemLen - 2) + Hangul.combine(preLastHg.cho, preLastHg.jung, 'ㄹ') + Hangul.combine(lastHg.cho, mo, ' ');
				if( mo == 'ㅏ' ) {
					mCandidateClone.addMorp("아", POSTag.EC);
					mCandidateClone.addHavingCond(Condition.AH);
				} else {
					mCandidateClone.addMorp("어", POSTag.EC);
					mCandidateClone.addHavingCond(Condition.AH);
				}
			}
			mCandidateClone.initBasicPhonemeCond(exp);
			add(exp, mCandidateClone);

			mCandidateClone = mCandidate.clone();
			mCandidateClone.clearHavingCondition();
			if( "다따들러치".indexOf(preLastCh) > -1 ) {
				exp = preStem + "라";
				mCandidateClone.addMorp("아", POSTag.EFN);
				mCandidateClone.addHavingCond(Condition.AH);
			} else if( "이푸".indexOf(preLastCh) > -1 ) {
				exp = stem + "러";
				mCandidateClone.addMorp("어", POSTag.EFN);
				mCandidateClone.addHavingCond(Condition.AH);
			} else {
				mo = getMoeum(lastHg, preLastHg);
				exp = stem.substring(0, stemLen - 2) + Hangul.combine(preLastHg.cho, preLastHg.jung, 'ㄹ') + Hangul.combine(lastHg.cho, mo, ' ');
				if( mo == 'ㅏ' ) {
					mCandidateClone.addMorp("아", POSTag.EFN);
					mCandidateClone.addHavingCond(Condition.AH);
				} else {
					mCandidateClone.addMorp("어", POSTag.EFN);
					mCandidateClone.addHavingCond(Condition.AH);
				}
			}
			mCandidateClone.initBasicPhonemeCond(exp);
			add(exp, mCandidateClone);

		}
		// 'ㅡ' 결합에 의한 어간 출력
		else if( !lastHg.hasJong() && lastHg.jung == 'ㅡ' ) {
			// 양성으로 한번 결합
			mo = getMoeum(lastHg, preLastHg);
			mCandidateClone = mCandidate.clone();
			mCandidateClone.clearHavingCondition();
			exp = preStem + Hangul.combine(lastHg.cho, mo, 'ㅆ');
			if( mo == 'ㅏ' ) {
				mCandidateClone.addMorp("았", POSTag.EPT);
				mCandidateClone.addHavingCond(Condition.EUT);
			} else {
				mCandidateClone.addMorp("었", POSTag.EPT);
				mCandidateClone.addHavingCond(Condition.EUT);
			}
			mCandidateClone.initBasicPhonemeCond(exp);
			add(exp, mCandidateClone);

			// ㅓ, ㅏ
			mCandidateClone = mCandidate.clone();
			mCandidateClone.clearHavingCondition();
			exp = preStem + Hangul.combine(lastHg.cho, mo, ' ');
			if( mo == 'ㅏ' ) {
				mCandidateClone.addMorp("아", POSTag.EC);
				mCandidateClone.addHavingCond(Condition.AH);
			} else {
				mCandidateClone.addMorp("어", POSTag.EC);
				mCandidateClone.addHavingCond(Condition.AH);
			}
			mCandidateClone.initBasicPhonemeCond(exp);
			add(exp, mCandidateClone);

			mCandidateClone = mCandidate.clone();
			mCandidateClone.clearHavingCondition();
			exp = preStem + Hangul.combine(lastHg.cho, mo, ' ');
			if( mo == 'ㅏ' ) {
				mCandidateClone.addMorp("아", POSTag.EFN);
				mCandidateClone.addHavingCond(Condition.AH);
			} else {
				mCandidateClone.addMorp("어", POSTag.EFN);
				mCandidateClone.addHavingCond(Condition.AH);
			}
			mCandidateClone.initBasicPhonemeCond(exp);
			add(exp, mCandidateClone);
		}
		// ㅜ, ㅗ결합에 의한 어간 출력
		else if( !lastHg.hasJong() && isMoSet2(lastHg.jung) ) {
			// 었, 았
			mCandidateClone = mCandidate.clone();
			exp = preStem + Hangul.combine(lastHg.cho, getMoeum(lastHg, preLastHg), 'ㅆ');
			if( lastHg.jung == 'ㅜ' ) {
				mCandidateClone.addMorp("었", POSTag.EPT);
			} else {
				mCandidateClone.addMorp("았", POSTag.EPT);
			}
			mCandidateClone.clearHavingCondition();
			mCandidateClone.addHavingCond(Condition.EUT);
			mCandidateClone.initBasicPhonemeCond(exp);
			add(exp, mCandidateClone);

			// 어, 아
			mCandidateClone = mCandidate.clone();
			exp = preStem + Hangul.combine(lastHg.cho, getMoeum(lastHg, preLastHg), ' ');
			if( lastHg.jung == 'ㅜ' ) {
				mCandidateClone.addMorp("어", POSTag.EC);
			} else {
				mCandidateClone.addMorp("아", POSTag.EC);
			}
			mCandidateClone.clearHavingCondition();
			mCandidateClone.addHavingCond(Condition.AH);
			mCandidateClone.initBasicPhonemeCond(exp);
			add(exp, mCandidateClone);

			mCandidateClone = mCandidate.clone();
			exp = preStem + Hangul.combine(lastHg.cho, getMoeum(lastHg, preLastHg), ' ');
			if( lastHg.jung == 'ㅜ' ) {
				mCandidateClone.addMorp("어", POSTag.EFN);
			} else {
				mCandidateClone.addMorp("아", POSTag.EFN);
			}
			mCandidateClone.clearHavingCondition();
			mCandidateClone.addHavingCond(Condition.AH);
			mCandidateClone.initBasicPhonemeCond(exp);
			add(exp, mCandidateClone);
		}
		// 겹모음 처리
		else if( !lastHg.hasJong() && lastHg.jung == 'ㅚ' ) {
			// 'ㅓ' 결합
			mCandidateClone = mCandidate.clone();
			exp = preStem + Hangul.combine(lastHg.cho, 'ㅙ', ' ');
			mCandidateClone.addMorp("어", POSTag.EC);
			mCandidateClone.clearHavingCondition();
			mCandidateClone.addHavingCond(Condition.AH);
			mCandidateClone.initBasicPhonemeCond(exp);
			add(exp, mCandidateClone);

			mCandidateClone = mCandidate.clone();
			exp = preStem + Hangul.combine(lastHg.cho, 'ㅙ', ' ');
			mCandidateClone.addMorp("어", POSTag.EFN);
			mCandidateClone.clearHavingCondition();
			mCandidateClone.addHavingCond(Condition.AH);
			mCandidateClone.initBasicPhonemeCond(exp);
			add(exp, mCandidateClone);

			// '었' 결합
			mCandidateClone = mCandidate.clone();
			exp = preStem + Hangul.combine(lastHg.cho, 'ㅙ', 'ㅆ');
			mCandidateClone.addMorp("었", POSTag.EPT);
			mCandidateClone.clearHavingCondition();
			mCandidateClone.addHavingCond(Condition.EUT);
			mCandidateClone.initBasicPhonemeCond(exp);
			add(exp, mCandidateClone);
		}

		// ㅂ 불규칙
		// ㅂ불규칙 활용하는 어간의 마지막 어절
		// '뵙뽑씹업입잡접좁집' 들은 활용 안함~
		if( "갑겁겹곱굽깁깝껍꼽납눕답덥돕둡땁떱랍럽렵롭립맙맵밉볍섭쉽습엽줍쭙춥탑".indexOf(lastCh) > -1 ) {

			// ㅂ탈락된 음절 생성
			char bChar = Hangul.combine(lastHg.cho, lastHg.jung, ' ');

			// 럽은 '러운' 뿐만 아니라 짧게 '런' 등으로도 활용됨
			if( lastCh == '럽' ) {
				mCandidateClone = mCandidate.clone();
				exp = preStem + '런';
				mCandidateClone.addMorp("ㄴ", POSTag.ETM);
				mCandidateClone.clearHavingCondition();
				mCandidateClone.initBasicPhonemeCond(exp);
				add(exp, mCandidateClone);
			}

			// 워, 와
			mCandidateClone = mCandidate.clone();
			if( lastHg.jung == 'ㅗ' ) {
				mo = 'ㅘ';
				mCandidateClone.addMorp("아", POSTag.EC);
			} else {
				mo = 'ㅝ';
				mCandidateClone.addMorp("어", POSTag.EC);
			}
			exp = preStem + bChar + Hangul.combine('ㅇ', mo, ' ');
			mCandidateClone.clearHavingCondition();
			mCandidateClone.addHavingCond(Condition.AH);
			mCandidateClone.initBasicPhonemeCond(exp);
			add(exp, mCandidateClone);

			mCandidateClone = mCandidate.clone();
			if( lastHg.jung == 'ㅗ' ) {
				mo = 'ㅘ';
				mCandidateClone.addMorp("아", POSTag.EFN);
			} else {
				mo = 'ㅝ';
				mCandidateClone.addMorp("어", POSTag.EFN);
			}
			exp = preStem + bChar + Hangul.combine('ㅇ', mo, ' ');
			mCandidateClone.clearHavingCondition();
			mCandidateClone.addHavingCond(Condition.AH);
			mCandidateClone.initBasicPhonemeCond(exp);
			add(exp, mCandidateClone);

			// 웠, 왔
			mCandidateClone = mCandidate.clone();
			mCandidateClone.clearHavingCondition();
			// ㅂ 불규칙 중 '돕', '곱'과 같이 단음절 어간에서는 '도워', '고워'가 아니라 '도와', '고와'와 같이 됨.
			// 롭과 같은 형태에서는 발생하지 않음.
			if( mCandidate.isLastTagOf(POSTag.VV | POSTag.VA | POSTag.VX) && stemLen == 1 && lastHg.jung == 'ㅗ' ) {
				mo = 'ㅘ';
				mCandidateClone.addMorp("았", POSTag.EPT);
			} else {
				mo = 'ㅝ';
				mCandidateClone.addMorp("었", POSTag.EPT);
			}
			exp = preStem + bChar + Hangul.combine('ㅇ', mo, 'ㅆ');
			mCandidateClone.addHavingCond(Condition.EUT);
			mCandidateClone.initBasicPhonemeCond(exp);
			add(exp, mCandidateClone);

			// 우
			mCandidateClone = mCandidate.clone();
			exp = preStem + bChar + '우';
			mCandidateClone.clearHavingCondition();
			mCandidateClone.addHavingCond(Condition.MINUS_BIEUB);
			mCandidateClone.addBackwardCond(Condition.MINUS_BIEUB);
			mCandidateClone.initBasicPhonemeCond(exp);
			add(exp, mCandidateClone);

			// ㄴ, ㄹ, ㅁ 에 의한 활용
			mCandidateClone = mCandidate.clone();
			exp = preStem + bChar + '운';
			mCandidateClone.addMorp("ㄴ", POSTag.ETM);
			mCandidateClone.clearHavingCondition();
			mCandidateClone.initBasicPhonemeCond(exp);
			add(exp, mCandidateClone);

			mCandidateClone = mCandidate.clone();
			mCandidateClone.addMorp("ㄹ", POSTag.ETM);
			exp = preStem + bChar + '울';
			mCandidateClone.clearHavingCondition();
			mCandidateClone.initBasicPhonemeCond(exp);
			add(exp, mCandidateClone);

			mCandidateClone = mCandidate.clone();
			mCandidateClone.addMorp("ㅁ", POSTag.ETN);
			exp = preStem + bChar + '움';
			mCandidateClone.clearHavingCondition();
			mCandidateClone.initBasicPhonemeCond(exp);
			add(exp, mCandidateClone);
		}
		// 'ㅅ' 뷸규칙
		else if( "긋끗낫뭇붓잇잣젓짓".indexOf(lastCh) > -1 ) {
			mCandidateClone = mCandidate.clone();
			exp = preStem + Hangul.combine(lastHg.cho, lastHg.jung, ' ');
			mCandidateClone.clearHavingCondition();
			mCandidateClone.addHavingCond(Condition.MINUS_SIOT);
			mCandidateClone.addBackwardCond(Condition.MINUS_SIOT);
			mCandidateClone.initBasicPhonemeCond(exp);
			add(exp, mCandidateClone);
		}
		// 'ㄷ' 뷸규칙
		//else if( lastHg.jong == 'ㄷ' ) { 2012-05-25
		else if( "걷겯긷눋닫듣묻붇싣컫".indexOf(lastCh) > -1 ) {
			mCandidateClone = mCandidate.clone();
			exp = preStem + Hangul.combine(lastHg.cho, lastHg.jung, 'ㄹ');
			mCandidateClone.clearHavingCondition();
			mCandidateClone.addHavingCond(Condition.MINUS_SIOT);
			mCandidateClone.addBackwardCond(Condition.MINUS_SIOT);
			mCandidateClone.initBasicPhonemeCond(exp);
			add(exp, mCandidateClone);
		}
		// 그외 처리
		else if( !lastHg.hasJong() || lastHg.jong == 'ㄹ'
		// ㅎ 불규칙 처리 lastCh == '맣' || lastCh == '갛' || lastCh == '랗' || lastCh == '얗'
		|| lastHg.jong == 'ㅎ' ) {
			// ㄴ, ㄹ, ㅁ, ㅂ 에 의한 활용
			mCandidateClone = mCandidate.clone();
			mCandidateClone.addMorp("ㄴ", POSTag.ETM);
			exp = preStem + Hangul.combine(lastHg.cho, lastHg.jung, 'ㄴ');
			mCandidateClone.clearHavingCondition();
			mCandidateClone.initBasicPhonemeCond(exp);
			add(exp, mCandidateClone);

			mCandidateClone = mCandidate.clone();
			exp = preStem + Hangul.combine(lastHg.cho, lastHg.jung, 'ㄹ');
			mCandidateClone.addMorp("ㄹ", POSTag.ETM);
			mCandidateClone.clearHavingCondition();
			mCandidateClone.initBasicPhonemeCond(exp);
			add(exp, mCandidateClone);

			// 'ㄹ' 불규칙
			if( lastHg.jong == 'ㄹ' ) {
				mCandidateClone = mCandidate.clone();
				exp = preStem + Hangul.combine(lastHg.cho, lastHg.jung, 'ㄻ');
				mCandidateClone.addMorp("ㅁ", POSTag.ETN);
				mCandidateClone.clearHavingCondition();
				mCandidateClone.initBasicPhonemeCond(exp);
				add(exp, mCandidateClone);

				// ㄹ탈락 현상 처리
				mCandidateClone = mCandidate.clone();
				exp = preStem + Hangul.combine(lastHg.cho, lastHg.jung, ' ');
				mCandidateClone.clearHavingCondition();
				mCandidateClone.addHavingCond(Condition.MINUS_LIEUL);
				mCandidateClone.addBackwardCond(Condition.MINUS_LIEUL);
				mCandidateClone.initBasicPhonemeCond(exp);
				add(exp, mCandidateClone);
			}
			// 'ㅎ' 불규칙
			else if( lastHg.jong == 'ㅎ' && stemLen > 1 ) {
				/* 2011-07-23 수정
				 * ㅎ불규칙은 ㅎ이 탈락하면 어미도 같이 결합하면서 변형된다.
				 * ㅎ만 탈락하고 어미가 그대로 결합하지 않음
				mCandidateClone = mCandidate.clone();
				exp = preStem + Hangul.combine(lastHg.cho, lastHg.jung, ' ');
				mCandidateClone.clearHavingCondition();
				mCandidateClone.addHavingCond(Condition.MINUS_HIEUT);
				mCandidateClone.initBasicPhonemeCond(exp);
				add(exp, mCandidateClone);*/

				// ㅓ, ㅏ
				mCandidateClone = mCandidate.clone();
				mCandidateClone.clearHavingCondition();
				exp = preStem + Hangul.combine(lastHg.cho, 'ㅐ', ' ');
				if( mo == 'ㅏ' ) {
					mCandidateClone.addMorp("아", POSTag.EC);
					mCandidateClone.addHavingCond(Condition.AH);
				} else {
					mCandidateClone.addMorp("어", POSTag.EC);
					mCandidateClone.addHavingCond(Condition.AH);
				}
				mCandidateClone.initBasicPhonemeCond(exp);
				add(exp, mCandidateClone);

				// ㅓ, ㅏ
				mCandidateClone = mCandidate.clone();
				mCandidateClone.clearHavingCondition();
				exp = preStem + Hangul.combine(lastHg.cho, 'ㅐ', ' ');
				if( mo == 'ㅏ' ) {
					mCandidateClone.addMorp("아", POSTag.EFN);
					mCandidateClone.addHavingCond(Condition.AH);
				} else {
					mCandidateClone.addMorp("어", POSTag.EFN);
					mCandidateClone.addHavingCond(Condition.AH);
				}
				mCandidateClone.initBasicPhonemeCond(exp);
				add(exp, mCandidateClone);
			} else {
				mCandidateClone = mCandidate.clone();
				mCandidateClone.addMorp("ㅁ", POSTag.ETN);
				exp = preStem + Hangul.combine(lastHg.cho, lastHg.jung, 'ㅁ');
				mCandidateClone.clearHavingCondition();
				mCandidateClone.initBasicPhonemeCond(exp);
				add(exp, mCandidateClone);
			}

			mCandidateClone = mCandidate.clone();
			exp = preStem + Hangul.combine(lastHg.cho, lastHg.jung, 'ㅂ');

			mCandidateClone.clearHavingCondition();
			mCandidateClone.addHavingCond(Condition.BIEUB);
			mCandidateClone.addBackwardCond(Condition.BIEUB);
			mCandidateClone.initBasicPhonemeCond(exp);
			add(exp, mCandidateClone);
		}
	}


	/**
	 * <pre>
	 * ㅆ, ㅏㅆ, ㅐㅆ, ㅕㅆ  결합하는 모음인지 확인
	 * </pre>
	 * @author	Dongjoo
	 * @since	2011. 8. 4.
	 * @param ch
	 * @return
	 */
	static boolean isMoSet1(char ch)
	{
		if( ch == 'ㅏ' || ch == 'ㅓ' || ch == 'ㅐ' || ch == 'ㅔ' ) return true;
		return false;
	}


	/**
	 * <pre>
	 * ㅜ, ㅗ결합하는 모임은지 확인
	 * </pre>
	 * @author	Dongjoo
	 * @since	2011. 8. 4.
	 * @param ch
	 * @return
	 */
	static boolean isMoSet2(char ch)
	{
		if( ch == 'ㅗ' || ch == 'ㅜ' || ch == 'ㅡ' ) return true;
		return false;
	}


	/**
	 * <pre>
	 * ㅗ, ㅜ, ㅡ에 ㅏㅆ, ㅓㅆ 이 결합될 때의 모음을 반환한다.
	 * </pre>
	 * @author	therocks
	 * @since	2007. 6. 5
	 * @param mo1
	 * @return
	 */
	static char getMoeum(Hangul lastHg, Hangul preLastHg)
	{
		char mo = 0;
		char mo1 = lastHg.jung;
		if( mo1 == 'ㅗ' ) {
			mo = 'ㅘ';
		} else if( mo1 == 'ㅜ' ) {
			if( lastHg.cho == 'ㅍ' ) {
				mo = 'ㅓ';
			} else {
				mo = 'ㅝ';
			}
		} else if( mo1 == 'ㅡ' ) {
			if( preLastHg != null && preLastHg.isPositiveMo() ) {
				mo = 'ㅏ';
			} else {
				mo = 'ㅓ';
			}
		}
		return mo;
	}


	@SuppressWarnings("unused")
	private void addNounExtended(String exp, MCandidate mCandidate)
		throws Exception
	{
		// TODO
		// 명사는 '는', '를'에 대한 조사 줄임말 처리를 해준다.
		// 모음으로 끝난 말에 대해서만 처리해줌
		// 일단 줄임말 처리하지 않음 -- 2007-07-23
		MCandidate mCandidateClone = null;
		char lastCh = exp.charAt(exp.length() - 1);
		String stem = exp.substring(0, exp.length() - 1), temp = null;
		Hangul lastHg = Hangul.split(lastCh);

		// '는' 하나는 -> 하난
		mCandidateClone = mCandidate.clone();
		mCandidateClone.clearHavingCondition();
		mCandidateClone.addMorp("는", POSTag.JKS);
		mCandidateClone.addHavingCond(Condition.JAEUM);
		temp = stem + Hangul.combine(lastHg.cho, lastHg.jung, 'ㄴ');
		add(temp, mCandidateClone);

		// '를' 하나를 -> 하날
		mCandidateClone = mCandidate.clone();
		mCandidateClone.clearHavingCondition();
		mCandidateClone.addMorp("를", POSTag.JKO);
		mCandidateClone.addHavingCond(Condition.JAEUM);
		temp = stem + Hangul.combine(lastHg.cho, lastHg.jung, 'ㄹ');
		add(temp, mCandidateClone);
	}


	private void loadRaw(String fileName)
		throws Exception
	{
		Timer timer = new Timer();
		timer.start();

		LineReader dicReader = null;
		String line = null;
		try {

			dicReader = new LineReader(fileName);

			while( (line = dicReader.readLine()) != null ) {
				int idx = line.indexOf(':');
				String exp = line.substring(0, idx);
				String[] arr = line.substring(idx + 1).split(";");
				for( int i = 0, stop = arr.length; i < stop; i++ ) {
					String temp = arr[i].trim();
					add(exp, createRaw(exp, temp.substring(1, temp.length() - 1)));
				}
			}
		} catch (Exception e) {
			System.err.println(line);
			throw e;
		} finally {
			dicReader.cleanup();
			timer.stop();
			timer.printMsg(fileName);
		}
	}


	private MCandidate createRaw(String exp, String source)
	{
		MCandidate mCandidate = new MCandidate();
		StringTokenizer st = new StringTokenizer(source, "[]", false);

		// 기분석 결과 저장
		String token = null, infos = "";
		String[] arr = null;
		for( int i = 0; st.hasMoreTokens(); i++ ) {
			token = st.nextToken();
			if( i == 0 ) {
				arr = token.split("\\+");
				for( int j = 0; j < arr.length; j++ ) {
					// 일반적인 형태소 분석 결과 저장
					mCandidate.addMorp(arr[j]);
				}
			} else {
				infos = token;
			}
		}

		// 부가 정보들에 대한 처리 수행
		st = new StringTokenizer(infos, "*" + MCandidate.DLMT_ATL + MCandidate.DLMT_BCL + MCandidate.DLMT_HCL + MCandidate.DLMT_CCL + MCandidate.DLMT_ECL + MCandidate.DLMT_PRB, true);
		while( st.hasMoreTokens() ) {
			token = st.nextToken();
			// 접속 가능한 품사 정보
			if( token.equals(MCandidate.DLMT_ATL) ) {
				token = st.nextToken().trim();
				token = token.substring(1, token.length() - 1);
				mCandidate.addApndblTags(token.split(","));
			}
			// 현재 후보가 가진 접속 조건
			else if( token.equals(MCandidate.DLMT_HCL) ) {
				token = st.nextToken().trim();
				token = token.substring(1, token.length() - 1);
				mCandidate.addHavingConds(token.split(","));
			}
			// 접속 확인 조건
			else if( token.equals(MCandidate.DLMT_CCL) ) {
				token = st.nextToken().trim();
				token = token.substring(1, token.length() - 1);
				mCandidate.addChkConds(token.split(","));
			}
			// 접속 배제 조건
			else if( token.equals(MCandidate.DLMT_ECL) ) {
				token = st.nextToken().trim();
				token = token.substring(1, token.length() - 1);
				mCandidate.addExclusionConds(token.split(","));
			}
		}

		// 기본 음운, 접속 조건 초기화
		mCandidate.initConds(exp);
		return mCandidate;
	}


	/**
	 * <pre>
	 * 로딩된 사전을 주어진 파일에 작성한다.
	 * </pre>
	 * @author	therocks
	 * @since	2007. 6. 6
	 * @param fileName
	 */
	private void printToFile(String fileName)
	{
		Timer timer = new Timer();
		timer.start();
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new FileOutputStream(new File(fileName)));
			ArrayList<MExpression> list = new ArrayList<MExpression>(table.values());
			Collections.sort(list, new Comparator<MExpression>()
			{
				public int compare(MExpression o1, MExpression o2)
				{
					return o1.exp.compareTo(o2.exp);
				}
			});

			for( int i = 0, stop = list.size(); i < stop; i++ ) {
				MExpression me = list.get(i);
				pw.println(me);
				//pw.println(me);
				pw.flush();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if( pw != null ) pw.close();
			timer.stop();
			timer.printMsg(fileName);
		}
	}


	private void compileToFile(String targetFileName)
	{
		Timer timer = new Timer();
		timer.start();
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(targetFileName);
			ArrayList<MExpression> list = new ArrayList<MExpression>(table.values());

			final int bucketSize = 100000;

			// bucket 순으로 sorting
			Collections.sort(list, new Comparator<MExpression>()
			{
				public int compare(MExpression o1, MExpression o2)
				{
					int bucketPos1 = Math.abs(o1.hashCode) % bucketSize;
					int bucketPos2 = Math.abs(o2.hashCode) % bucketSize;
					return bucketPos1 - bucketPos2;
				}
			});

			// init size
			int valSize = list.size();
			int keyArrSize = 0;
			for( MExpression me : list ) {
				keyArrSize += me.exp.length();
			}

			// init memory for hashing
			int[] bucket = new int[bucketSize];
			int[] nextPosArr = new int[valSize];
			int[] keyHeadPosArr = new int[valSize];
			char[] keyArr = new char[keyArrSize];

			// init position variables
			int curPos = 0;
			int curKeyHeadPos = 0;

			Arrays.fill(bucket, -1);
			Arrays.fill(nextPosArr, -1);
			Arrays.fill(keyHeadPosArr, -1);

			// load hash
			char[] key = null;
			for( MExpression me : list ) {
				int bucketPos = Math.abs(me.hashCode) % bucketSize;

				int valPos = bucket[bucketPos];

				// blank
				if( valPos == -1 ) {
					bucket[bucketPos] = curPos;
				}
				// collision
				else {
					while( nextPosArr[valPos] != -1 ) {
						valPos = nextPosArr[valPos];
					}
					nextPosArr[valPos] = curPos;
				}

				// set key position
				keyHeadPosArr[curPos] = curKeyHeadPos;
				key = me.exp.toCharArray();
				System.arraycopy(key, 0, keyArr, curKeyHeadPos, key.length);

				curPos++;
				curKeyHeadPos += key.length;
			}

			// write hash table
			byte[] temp = null;
			temp = Convert.toByta(bucketSize);
			fos.write(temp);
			temp = Convert.toByta(valSize);
			fos.write(temp);
			temp = Convert.toByta(keyArrSize);
			fos.write(temp);
			temp = Convert.toByta(bucket);
			fos.write(temp);
			temp = Convert.toByta(nextPosArr);
			fos.write(temp);
			temp = Convert.toByta(keyHeadPosArr);
			fos.write(temp);
			temp = Convert.toByta(keyArr);
			fos.write(temp);

			// write candidates
			for( MExpression me : list ) {
				temp = me.toBytes();
				fos.write(temp);
			}
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			timer.stop();
			timer.printMsg(targetFileName);
		}
	}
}

class MExpression
	extends ArrayList<MCandidate>
{
	String	exp				= null;
	float	lnprOfSpacing	= 0;
	int		hashCode		= 0;


	MExpression(String exp)
	{
		super();
		this.exp = exp;
		hashCode = RSHash.hash(exp);
	}


	public MExpression(String exp, MCandidate mc)
		throws Exception
	{
		this(exp);
		add(mc);
	}


	public String getExp()
	{
		return exp;
	}


	public boolean add(MCandidate mc)
	{
		if( mc != null && !contains(mc) ) {
			return super.add(mc);
		}
		return false;
	}


	public float getLnprOfSpacing()
	{
		return lnprOfSpacing;
	}


	public void setLnprOfSpacing(float lnprOfSpacing)
	{
		this.lnprOfSpacing = lnprOfSpacing;
	}


	public String toString()
	{
		StringBuffer sb = new StringBuffer(exp + Util.LINE_SEPARATOR);
		//sb.append(String.format("\t %4s%8s%8s%8s%8s%8s%8s", "siz", "spcing", "tging", "lnpr", "spcing!", "tging!", "lnpr!") + Util.LINE_SEPARATOR);
		for( int i = 0, stop = size(); i < stop; i++ ) {
			sb.append("\t{" + get(i).toDicString() + "};" + Util.LINE_SEPARATOR);
		}
		return sb.toString();
	}


	public int hashCode()
	{
		return hashCode;
	}


	public byte[] toBytes()
	{
		int byteSize = 0;
		int size = size();

		// 띄어쓰기 확률 저장 공간
		byteSize += Convert.FLT_SIZE;

		// 분석 후보 저장 공간
		byteSize++;
		byte[][] buff = new byte[size][];
		for( int i = 0; i < size; i++ ) {
			buff[i] = get(i).toBytes();
			byteSize += buff[i].length;
		}

		// set memory = byte + byteSize 
		byte[] ret = new byte[byteSize];

		int offset = 0;
		byte[] temp = Convert.toByta(lnprOfSpacing);
		System.arraycopy(temp, 0, ret, offset, temp.length);
		offset += temp.length;

		ret[offset] = (byte) size;
		offset++;
		for( int i = 0; i < size; i++ ) {
			System.arraycopy(buff[i], 0, ret, offset, buff[i].length);
			offset += buff[i].length;
		}

		return ret;
	}
}
