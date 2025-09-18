package com.hawk.game.module.lianmengyqzz.battleroom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class YQZZBornPointRing {
	private List<int[]> list = new ArrayList<>();
	private int index;

	public YQZZBornPointRing(int[] p1, int[] p2) {
		int minX = Math.min(p1[0], p2[0]);
		int maxX = Math.max(p1[0], p2[0]);
		int minY = Math.min(p1[1], p2[1]);
		int maxY = Math.max(p1[1], p2[1]);

		for (int x = minX + 1; x < maxX; x++) {
			for (int y = minY + 1; y < maxY; y++) {
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

	public int size() {
		return list.size();
	}
}
