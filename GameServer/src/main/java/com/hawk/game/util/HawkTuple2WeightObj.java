package com.hawk.game.util;

import org.hawk.os.HawkRandObj;
import org.hawk.tuple.HawkTuple2;

public class HawkTuple2WeightObj<T> extends HawkTuple2<T, Integer> implements HawkRandObj, Comparable<HawkTuple2WeightObj<T>> {

	public HawkTuple2WeightObj(T first, Integer second) {
		super(first, second);
	}

	@Override
	public int getWeight() {
		return second;
	}

	@Override
	public int compareTo(HawkTuple2WeightObj<T> o) {
		return this.getWeight() - o.getWeight();
	}

}
