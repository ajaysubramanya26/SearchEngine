/**
 * 
 */
package neu.ir.cs6200.evaluator;

import java.util.List;

/**
 * @author kamlendra
 *
 */
public class QueryEvaluationSummary {

	private double averagePrecision;
	private int reciprocalRank;
	private List<EvaluationResult> rankWisePrecisionRecall;

	public QueryEvaluationSummary(double averagePrecision, int reciprocalRank,
			List<EvaluationResult> rankWisePrecisionRecall) {
		this.averagePrecision = averagePrecision;
		this.reciprocalRank = reciprocalRank;
		this.rankWisePrecisionRecall = rankWisePrecisionRecall;
	}

	/**
	 * @return the averagePrecision
	 */
	public double getAveragePrecision() {
		return averagePrecision;
	}

	/**
	 * @param averagePrecision the averagePrecision to set
	 */
	public void setAveragePrecision(double averagePrecision) {
		this.averagePrecision = averagePrecision;
	}

	/**
	 * @return the reciprocalRank
	 */
	public int getReciprocalRank() {
		return reciprocalRank;
	}

	/**
	 * @param reciprocalRank the reciprocalRank to set
	 */
	public void setReciprocalRank(int reciprocalRank) {
		this.reciprocalRank = reciprocalRank;
	}

	/**
	 * @return the rankWisePrecisionRecall
	 */
	public List<EvaluationResult> getRankWisePrecisionRecall() {
		return rankWisePrecisionRecall;
	}

	/**
	 * @param rankWisePrecisionRecall the rankWisePrecisionRecall to set
	 */
	public void setRankWisePrecisionRecall(List<EvaluationResult> rankWisePrecisionRecall) {
		this.rankWisePrecisionRecall = rankWisePrecisionRecall;
	}
}
