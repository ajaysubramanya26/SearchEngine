package neu.ir.cs6200.evaluator;

/**
 * @author kamlendrak
 *
 */
public enum Mode {

		BM25("BM25"),
		TFIDF("TFIDF"),
		LUCENE("Lucene"),
		PSEUDO_REL_QE("BM25PseudoRel"),
		QE2("QE2"),
		STOPPING("BM25Stopping"),
		T7("t7");

	public final String mode;

	private Mode(String mode) {
		this.mode = mode;
	}

	public String mode() {
		return this.mode;
	}
}
