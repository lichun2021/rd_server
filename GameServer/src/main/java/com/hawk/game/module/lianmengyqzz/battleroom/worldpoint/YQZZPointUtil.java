package com.hawk.game.module.lianmengyqzz.battleroom.worldpoint;

import java.util.HashSet;
import java.util.Set;

import com.hawk.game.util.GameUtil;

public class YQZZPointUtil {
	private YQZZPointUtil() {
	}

	public static Set<Integer> getOcuPointId(int centerX, int centerY, int radius) {
		Set<Integer> set = new HashSet<>();
		return getOcuPointId(centerX, centerY, radius, set);
	}
	
	public static Set<Integer> getOcuPointId(int centerX, int centerY, int radius,Set<Integer> set ) {
		int radiusX = radius;
		int radiusY = radius;

		set.add(GameUtil.combineXAndYCacheIndex(centerX, centerY));
		// 取x轴上的点
		for (int i = 1; i <= radiusX - 1; i++) {
			int x1 = centerX + i;
			int x2 = centerX - i;
			set.add(GameUtil.combineXAndYCacheIndex(x1, centerY));
			set.add(GameUtil.combineXAndYCacheIndex(x2, centerY));
		}

		// 取y轴上的点
		for (int i = 1; i <= radiusY - 1; i++) {
			int y1 = centerY + i;
			int y2 = centerY - i;
			set.add(GameUtil.combineXAndYCacheIndex(centerX, y1));
			set.add(GameUtil.combineXAndYCacheIndex(centerX, y2));
		}

		// 取其它点
		for (int i = 0; i <= radiusX - 1; i++) {
			for (int j = 0; j <= radiusY - 1 - i; j++) {
				// 不要中心点和坐标轴点
				if (i == 0 || j == 0) {
					continue;
				}

				int x1 = centerX + i;
				int x2 = centerX - i;
				int y1 = centerY + j;
				int y2 = centerY - j;

				set.add(GameUtil.combineXAndYCacheIndex(x1, y1));
				set.add(GameUtil.combineXAndYCacheIndex(x1, y2));
				set.add(GameUtil.combineXAndYCacheIndex(x2, y1));
				set.add(GameUtil.combineXAndYCacheIndex(x2, y2));
			}
		}
		return set;
	}

}
