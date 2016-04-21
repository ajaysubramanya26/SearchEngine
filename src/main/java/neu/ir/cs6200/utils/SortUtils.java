package neu.ir.cs6200.utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

	/**
	 * Sorts HashMap by value
	 *
	 * @param map
	 * @return
	 * @info : http://stackoverflow.com/questions/109383/sort-a-mapkey-value-by-
	 *       values-java
	 */
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map, boolean sortDescending) {
		List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
		if (!sortDescending) {
			Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
				@Override
				public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
					return (o1.getValue()).compareTo(o2.getValue());
				}
			});
		} else {
			Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
				@Override
				public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
					return (o2.getValue()).compareTo(o1.getValue());
				}
			});
		}

		Map<K, V> result = new LinkedHashMap<>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
}
