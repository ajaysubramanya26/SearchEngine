package neu.ir.cs6200.evaluator;

import neu.ir.cs6200.constants.Consts;

/**
 * @author kamlendrak
 *
 */
public enum Mode {

		BM25(Consts.BM25_FName),
		TFIDF(Consts.TFIDF),
		LUCENE(Consts.Lucene_Fname),
		PSEUDO_REL_QE(Consts.BM25PseudoRel_Fname),
		SYN_QE(Consts.BM25_Synonym_Fname),
		STOPPING(Consts.BM25_Stopping_Fname),
		T7(Consts.TFIDFStopSyn_Fname);

	public final String mode;

	private Mode(String mode) {
		this.mode = mode;
	}

	public String mode() {
		return this.mode;
	}
}
