package org.snu.ids.ha.dic.compiler;


public class DicCompiler
{
	final static String	DIC_ROOT	= "data/dic";


	public static void main(String[] args)
	{
		// 기호 사전 compile
		SymbolDicCompiler.compile(DIC_ROOT + "/60symbol.txt", "src/main/resources/dic/compile_symbol.dic");

		// 형태소 사전 compile하기 전에 확률 사전 먼저 compile해 두어야 함.
		// 기분석 사전 compile
		MorpDicCompiler.compile("src/main/resources/dic/compile.dic");
		//MorpDicCompiler.print(DIC_ROOT + "/dic.txt");
	}
}
