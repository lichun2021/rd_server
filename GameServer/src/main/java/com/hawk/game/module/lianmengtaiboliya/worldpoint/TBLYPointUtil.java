package com.hawk.game.module.lianmengtaiboliya.worldpoint;

import java.util.HashSet;
import java.util.Set;

import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.util.GameUtil;

public class TBLYPointUtil {
	private TBLYPointUtil() {
	}

	public static int pointRedis(WorldPointType pt) {
//		TBLY_FUELBANK					= 26;	// tbly燃料库   2
//		TBLY_IRON_CRUTAIN_DIVICE		= 27;   // 铁幕装置   4
//		TBLY_NUCLEAR_MISSILE_SILO		= 29;   // 核弹发射井   4
//		TBLY_WEATHER_CONTROLLER			= 30;   // 天气控制器   4
//		TBLY_CHRONO_SPHERE				= 31;   // 超时空传送器   4
//		TBLY_COMMAND_CENTER				= 32;   // 指挥部   4
//		TBLY_MILITARY_BASE				= 33;   // 军事基地   3
//		TBLY_HEADQUARTERS				= 34;   // 司令部   7
		switch (pt) {
		case PLAYER:
			return 2;
		case RESOURCE:
			return 1;
		case TBLY_FUELBANK:
			return 2;
		case TBLY_IRON_CRUTAIN_DIVICE:
			return 4;
		case TBLY_NUCLEAR_MISSILE_SILO:
			return 4;
		case TBLY_WEATHER_CONTROLLER:
			return 4;
		case TBLY_CHRONO_SPHERE:
			return 4;
		case TBLY_COMMAND_CENTER:
			return 4;
		case TBLY_MILITARY_BASE:
			return 3;
		case TBLY_HEADQUARTERS:
			return 7;
		case NIAN:
			return 2;
		case MONSTER:
			return 1;
		case TBLY_TECHNOLOGY_LAB:
			return 3;

		default:
			break;
		}
		return 2;
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

	public static int[] xy(int x, int y, WorldPointType pt) {
		if ((x + y + pointRedis(pt)) % 2 == 0) {
			return new int[] { x + 1, y };
		}
		return new int[] { x, y };
	}
}
