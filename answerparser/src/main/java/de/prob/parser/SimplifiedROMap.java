package de.prob.parser;

import java.util.Map;
import java.util.Set;

/**
 * The default implementation of {@link ISimplifiedROMap}, which is just a
 * wrapper around a {@link Map}.
 * 
 * @author plagge
 */
public class SimplifiedROMap<K, V> implements ISimplifiedROMap<K, V> {
	private final Map<K, V> map;

	public SimplifiedROMap(final Map<K, V> map) {
		this.map = map;
	}

	@Override
	public V get(final K key) {
		return map.get(key);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		Set<Map.Entry<K, V>> entrySet = map.entrySet();
		for (Map.Entry<K, V> entry : entrySet) {
			sb.append(entry.getKey());
			sb.append("->");
			sb.append(entry.getValue());
			sb.append(" ");
		}
		return sb.toString();
	}
}
