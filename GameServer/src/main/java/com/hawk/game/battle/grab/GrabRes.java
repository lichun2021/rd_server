package com.hawk.game.battle.grab;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigManager;

import com.google.common.collect.ImmutableMap;
import com.hawk.game.config.WorldMapConstProperty;
import com.hawk.game.config.WorldMarchConstProperty;
import com.hawk.game.config.WorldPlunderupLimitCfg;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.PlayerAttr;

public class GrabRes {
	private GrabRes() {
	}

	private String playerId;
	private int cityLv;
	private long maxAddWeight;
	private int res1007;
	private int res1008;
	private int res1010;
	private int res1009;

	private int resGrab1007;
	private int resGrab1008;
	private int resGrab1010;
	private int resGrab1009;

	private int res1007Weight;
	private int res1008Weight;
	private int res1010Weight;
	private int res1009Weight;

	private int res1007WeightMax;
	private int res1008WeightMax;
	private int res1010WeightMax;
	private int res1009WeightMax;

	private int weightMax;

	public static List<GrabRes> valueOfAll(List<Player> players, int[] weightAry) {
		if (players.size() != weightAry.length) {
			throw new RuntimeException("Fuck!");
		}

		List<GrabRes> result = new ArrayList<>(weightAry.length);
		int[] RES_LV = WorldMapConstProperty.getInstance().getResLv();
		for (int i = 0; i < weightAry.length; i++) {
			Player player = players.get(i);
			WorldPlunderupLimitCfg plunderCfg = HawkConfigManager.getInstance().getCombineConfig(WorldPlunderupLimitCfg.class, player.getCityLevel(), player.getVipLevel());
			ImmutableMap<Integer, Integer> dayGrabResWeight = LocalRedis.getInstance().grabResWeightDayCount(player.getId());

			GrabRes gra = new GrabRes();
			gra.cityLv = player.getCityLevel();
			gra.playerId = player.getId();
			gra.res1007Weight = WorldMarchConstProperty.getInstance().getResWeightByType(PlayerAttr.GOLDORE_UNSAFE_VALUE);
			gra.res1008Weight = WorldMarchConstProperty.getInstance().getResWeightByType(PlayerAttr.OIL_UNSAFE_VALUE);
			gra.res1010Weight = WorldMarchConstProperty.getInstance().getResWeightByType(PlayerAttr.TOMBARTHITE_UNSAFE_VALUE);
			gra.res1009Weight = WorldMarchConstProperty.getInstance().getResWeightByType(PlayerAttr.STEEL_UNSAFE_VALUE);

			gra.resGrab1007 = dayGrabResWeight.getOrDefault(PlayerAttr.GOLDORE_UNSAFE_VALUE, 0).intValue();
			gra.resGrab1008 = dayGrabResWeight.getOrDefault(PlayerAttr.OIL_UNSAFE_VALUE, 0).intValue();
			gra.resGrab1010 = dayGrabResWeight.getOrDefault(PlayerAttr.TOMBARTHITE_UNSAFE_VALUE, 0).intValue();
			gra.resGrab1009 = dayGrabResWeight.getOrDefault(PlayerAttr.STEEL_UNSAFE_VALUE, 0).intValue();

			gra.weightMax = weightAry[i];
			if (plunderCfg != null) {
				gra.res1007WeightMax = plunderCfg.getPlunderupLimit(PlayerAttr.GOLDORE_UNSAFE_VALUE)
						- gra.resGrab1007;
				gra.res1008WeightMax = plunderCfg.getPlunderupLimit(PlayerAttr.OIL_UNSAFE_VALUE)
						- gra.resGrab1008;
				
				if (gra.cityLv >= RES_LV[2]) {
					gra.res1010WeightMax = plunderCfg.getPlunderupLimit(PlayerAttr.TOMBARTHITE_UNSAFE_VALUE)
							- gra.resGrab1010;
				}
				if (gra.cityLv >= RES_LV[3]) {
					gra.res1009WeightMax = plunderCfg.getPlunderupLimit(PlayerAttr.STEEL_UNSAFE_VALUE)
							- gra.resGrab1009;
				}
			}
			
			if (gra.res1007WeightMax > 0) {
				gra.res1007WeightMax += gra.res1009Weight;
			}
			if (gra.res1008WeightMax > 0) {
				gra.res1008WeightMax += gra.res1009Weight;
			}
			if (gra.res1010WeightMax > 0) {
				gra.res1010WeightMax += gra.res1009Weight;
			}
			if (gra.res1009WeightMax > 0) {
				gra.res1009WeightMax += gra.res1009Weight;
			}
			result.add(gra);
		}
		return result;
	}

	public String getPlayerId() {
		return playerId;
	}

	public long[] grabResArr() {
		resGrab1007 += res1007 * res1007Weight;
		resGrab1008 += res1008 * res1008Weight;
		resGrab1010 += res1010 * res1010Weight;
		resGrab1009 += res1009 * res1009Weight;

		int[] RES_LV = WorldMapConstProperty.getInstance().getResLv();
		long plunderupLimit1007 = res1007 * res1007Weight >= res1007WeightMax ? 1 : 0;
		long plunderupLimit1008 = res1008 * res1008Weight >= res1008WeightMax ? 1 : 0;
		long plunderupLimit1010 = (cityLv >= RES_LV[2] && res1010 * res1010Weight >= res1010WeightMax) ? 1 : 0;
		long plunderupLimit1009 = (cityLv >= RES_LV[3] && res1009 * res1009Weight >= res1009WeightMax) ? 1 : 0;

		/** 一定要提前准备好数据, 妈B的, 发邮件和给奖励在不同线程.有时序问题 */
		return new long[] { res1007, res1008, res1010, res1009, // 抢到的
				plunderupLimit1007, plunderupLimit1008, plunderupLimit1010, plunderupLimit1009, // 到上限
				resGrab1007, resGrab1008, resGrab1010, resGrab1009 }; // 当日掠夺总量
	}

	public long incRes1007(long addCount) {
		if (res1007 * res1007Weight >= res1007WeightMax) {
			return 0;
		}
		long maxCount = maxAddWeight / res1007Weight;
		maxCount = Math.min(maxCount, addCount);
		res1007 += maxCount;
		return maxCount;
	}

	public long incRes1008(long addCount) {
		if (res1008 * res1008Weight >= res1008WeightMax) {
			return 0;
		}
		long maxCount = maxAddWeight / res1008Weight;
		maxCount = Math.min(maxCount, addCount);
		res1008 += maxCount;
		return maxCount;
	}

	public long incRes1010(long addCount) {
		if (res1010 * res1010Weight >= res1010WeightMax) {
			return 0;
		}
		long maxCount = maxAddWeight / res1010Weight;
		maxCount = Math.min(maxCount, addCount);
		res1010 += maxCount;
		return maxCount;
	}

	public long incRes1009(long addCount) {
		if (res1009 * res1009Weight >= res1009WeightMax) {
			return 0;
		}
		long maxCount = maxAddWeight / res1009Weight;
		maxCount = Math.min(maxCount, addCount);
		res1009 += maxCount;
		return maxCount;
	}

	public boolean overMax() {
		if (allResWeight() >= weightMax) {
			return true;
		}
		return false;
	}

	private int allResWeight() {
		return res1007 * res1007Weight + res1008 * res1008Weight + res1010 * res1010Weight + res1009 * res1009Weight;
	}

	public long nextSafeAddWeight() {
		if (overMax()) {
			return 0;
		}
		int typeCount = 0;
		long result = 1 << 25;
		if (res1007 * res1007Weight < res1007WeightMax) {
			result = Math.min(result, res1007WeightMax - res1007 * res1007Weight);
			typeCount++;
		}
		if (res1008 * res1008Weight < res1008WeightMax) {
			result = Math.min(result, res1008WeightMax - res1008 * res1008Weight);
			typeCount++;
		}
		if (res1010 * res1010Weight < res1010WeightMax) {
			result = Math.min(result, res1010WeightMax - res1010 * res1010Weight);
			typeCount++;
		}
		if (res1009 * res1009Weight < res1009WeightMax) {
			result = Math.min(result, res1009WeightMax - res1009 * res1009Weight);
			typeCount++;
		}
		if (typeCount == 0) {
			this.maxAddWeight = 0;
			return 0;
		}
		long left = weightMax - allResWeight();
		result = Math.min(result, left / typeCount);
		this.maxAddWeight = result + res1009Weight;
		return this.maxAddWeight;
	}

}
