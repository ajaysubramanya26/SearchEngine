/**
 * 
 */
package neu.ir.cs6200.evaluator;

/**
 * @author kamlendrak
 *
 */
public class EvaluationResult {

	private int id;
	private double precision;
	private double recall;
	
	public EvaluationResult(int id, double precision, double recall) {
		this.id = id;
		this.precision = precision;
		this.recall = recall;
	}
	
	@Override
	public String toString() {
		return id + " , " + precision + " , " + recall;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the precision
	 */
	public double getPrecision() {
		return precision;
	}

	/**
	 * @param precision the precision to set
	 */
	public void setPrecision(double precision) {
		this.precision = precision;
	}

	/**
	 * @return the recall
	 */
	public double getRecall() {
		return recall;
	}

	/**
	 * @param recall the recall to set
	 */
	public void setRecall(double recall) {
		this.recall = recall;
	}
}
