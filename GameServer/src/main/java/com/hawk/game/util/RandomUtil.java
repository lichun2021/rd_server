package com.hawk.game.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.hawk.os.HawkRand;
import org.hawk.os.HawkRandObj;

public class RandomUtil {
	private RandomUtil() {
	}

	static final ThreadLocal<Random> RANDOM = ThreadLocal.withInitial(Random::new);

	public static <T extends WeightAble> T random(List<T> list) {
		final int denominator = list.stream().mapToInt(T::getWeight).sum();
		int molecular = RANDOM.get().nextInt(denominator);
		T result = null;
		for (T cfg : list) {
			molecular = molecular - cfg.getWeight();
			if (molecular < 0) {
				result = cfg;
				break;
			}
		}

		return result;
	}
	
	public static <T> T randomWeightObject(List<T> objList) {
		List<T> rlist = randomWeightObject(objList, 1);
		if(!rlist.isEmpty()){
			return rlist.get(0);
		}
		return null;
	}

	/**
	 * 按照权重随机n个对象
	 * 
	 * @param objList
	 * @param objWeight
	 * @return
	 */
	public static <T> List<T> randomWeightObject(List<T> objList, int count) {
		if (objList == null || objList.isEmpty()) {
			return new LinkedList<>();
		}
		count = Math.min(count, objList.size());
		List<Integer> objWeight = new ArrayList<>(objList.size());
		for (int i = 0; i < objList.size(); i++) {
			T obj = objList.get(i);
			if(obj instanceof HawkRandObj){
				objWeight.add(((HawkRandObj) obj).getWeight());
			} else if (obj instanceof WeightAble) {
				objWeight.add(((WeightAble) obj).getWeight());
			} else {
				objWeight.add(1);
			}
		}

		return HawkRand.randomWeightObject(objList, objWeight, count);
	}
}
