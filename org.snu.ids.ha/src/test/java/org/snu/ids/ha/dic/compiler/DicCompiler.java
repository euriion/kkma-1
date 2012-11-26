package org.snu.ids.ha.dic.compiler;


public class DicCompiler
{
	final static String DIC_ROOT = "data/dic";

	public static void main(String[] args)
	{
		// 음절 확률 사전
		ProbDicCompiler.compileLnprSyllableBi(DIC_ROOT + "/prob/lnpr_syllable_bi.txt", "src/main/resources/dic/compile_lnpr_syllable_bi.dic");
		ProbDicCompiler.compileLnprSyllableUniNoun(DIC_ROOT + "/prob/lnpr_syllable_uni_noun.txt", "src/main/resources/dic/compile_lnpr_syllable_uni_noun.dic");

		// 확률 사전 compile
		ProbDicCompiler.compileLnprPos(DIC_ROOT + "/prob/lnpr_pos.txt", "src/main/resources/dic/compile_lnpr_pos.dic");
		ProbDicCompiler.compilePosGPos(DIC_ROOT + "/prob/lnpr_pos_g_pos_intra.txt", "src/main/resources/dic/compile_lnpr_pos_g_pos_intra.dic");
		ProbDicCompiler.compilePosGPos(DIC_ROOT + "/prob/lnpr_pos_g_pos_inter.txt", "src/main/resources/dic/compile_lnpr_pos_g_pos_inter.dic");
		ProbDicCompiler.compileLnprMorp(DIC_ROOT + "/prob/lnpr_morp.txt", "src/main/resources/dic/compile_lnpr_morp.dic");
		ProbDicCompiler.compileLnprMorp(DIC_ROOT + "/prob/lnpr_pos_g_exp.txt", "src/main/resources/dic/compile_lnpr_pos_g_exp.dic");
		ProbDicCompiler.compileLnprMorpsGExp(DIC_ROOT + "/prob/lnpr_morps_g_exp.txt", "src/main/resources/dic/compile_lnpr_morps_g_exp.dic");
		ProbDicCompiler.compileLnprPosGMorp(DIC_ROOT + "/prob/lnpr_pos_g_morp_intra.txt", "src/main/resources/dic/compile_lnpr_pos_g_morp_intra.dic");
		ProbDicCompiler.compileLnprPosGMorp(DIC_ROOT + "/prob/lnpr_pos_g_morp_inter.txt", "src/main/resources/dic/compile_lnpr_pos_g_morp_inter.dic");
		
		// 기호 사전 compile
		SymbolDicCompiler.compile(DIC_ROOT + "/60symbol.txt", "src/main/resources/dic/compile_symbol.dic");

		// 형태소 사전 compile하기 전에 확률 사전 먼저 compile해 두어야 함.
		// 기분석 사전 compile
		MorpDicCompiler.compile("src/main/resources/dic/compile.dic");
		MorpDicCompiler.print(DIC_ROOT + "/dic.txt");
	}

}
