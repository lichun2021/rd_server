package com.hawk.game.module.dayazhizhan.battleroom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DYZZBornPointRing {
	private List<int[]> list = new ArrayList<>();
	private int index;

	public DYZZBornPointRing(List<int[]> bornlist) {
		this.list = bornlist;
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

	public int size() {
		return list.size();
	}
}
