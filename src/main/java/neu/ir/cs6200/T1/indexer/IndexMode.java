package neu.ir.cs6200.T1.indexer;

/**
 * @author smitha
 *
 */
public enum IndexMode {

		NORMAL(0), STOP(1), STEM(2);

	public final int mode;

	private IndexMode(int mode) {
		this.mode = mode;
	}

	public int mode() {
		return this.mode;
	}
}
