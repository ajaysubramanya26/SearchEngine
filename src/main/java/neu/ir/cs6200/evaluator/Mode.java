package neu.ir.cs6200.evaluator;

/**
 * @author kamlendrak
 *
 */
public enum Mode {

		BM25("BM25"), TFIDF("TFIDF"), LUCENE("Lucene");

	public final String mode;

	private Mode(String mode) {
		this.mode = mode;
	}

	public String mode() {
		return this.mode;
	}
}
