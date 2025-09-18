package com.hawk.game.lianmengcyb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CYBORGRandPointSeed {
	public static final int SEED = 16; // 六六大顺
	private List<int[]> list = new ArrayList<>();
	private int index;

	public CYBORGRandPointSeed() {
		for (int x = -SEED; x < SEED; x++) {
			for (int y = -SEED; y < SEED; y++) {
				list.add(new int[] { x, y });
			}
		}
		Collections.shuffle(list);
	}

	public int[] nextPoint() {
		if (index >= list.size()) {
			index = 0;
		}
		int[] result = list.get(index);
		index++;
		return result;
	}
	
	public int size(){
		return list.size();
	}
}
