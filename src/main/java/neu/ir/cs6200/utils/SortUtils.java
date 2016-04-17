package neu.ir.cs6200.utils;

import java.util.Comparator;
import java.util.HashMap;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;

/**
 * Sort Utility
 * 
 * @author smitha
 *
 */
public class SortUtils {
	public static ListMultimap<Long, String> sortMostToLeastFrequent(HashMap<String, Long> hm) {
		ListMultimap<Long, String> lm = MultimapBuilder.treeKeys(DESC_ORDER).arrayListValues().build();
		for (String word : hm.keySet()) {
			lm.put(hm.get(word), word);
		}

		return lm;
	}

	static final Comparator<Long> DESC_ORDER = new Comparator<Long>() {
		@Override
		public int compare(Long e1, Long e2) {
			return e2.compareTo(e1);
		}
	};

	public static ListMultimap<Double, String> sortMostToLeastScore(HashMap<String, Double> hm) {
		ListMultimap<Double, String> lm = MultimapBuilder.treeKeys(DESC_ORDER_DOUBLE).arrayListValues().build();
		for (String word : hm.keySet()) {
			lm.put(hm.get(word), word);
		}
		return lm;
	}

	static final Comparator<Double> DESC_ORDER_DOUBLE = new Comparator<Double>() {
		@Override
		public int compare(Double e1, Double e2) {
			return e2.compareTo(e1);
		}
	};
}
