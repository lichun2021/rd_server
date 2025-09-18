package com.hawk.game.module.lianmengyqzz.battleroom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.hawk.game.util.GameUtil;

public class YQZZRandPointSeed {
	private List<int[]> list = new ArrayList<>();
	private int index;

	public int[] nextPoint() {
		if (index >= list.size()) {
			index = 0;
		}
		int[] result = list.get(index);
		index++;
		return result;
	}

	public int size() {
		return list.size();
	}

	public void add(int val) {
		list.add(GameUtil.splitXAndY(val));
	}

	public void shuffle() {
		Collections.shuffle(list);
	}
}
