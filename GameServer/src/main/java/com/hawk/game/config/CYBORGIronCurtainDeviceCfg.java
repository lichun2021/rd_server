package com.hawk.game.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuples;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.hawk.game.protocol.Const.EffType;

@HawkConfigManager.KVResource(file = "xml/cyborg_iron_crutain_divice.xml")
public class CYBORGIronCurtainDeviceCfg extends HawkConfigBase {
	// # 首次占领个人 积分
	private final double firstControlHonor;// = 1000

	// # 首次占领联盟 积分
	private final double firstControlGuildHonor;// = 1000

	// # 刷新点 x_y, 半径为偶数 x,y相加必须为奇数
	private final String refreshPoint;// = 43_42,58_59,60_61,75_50

	// # 完全控制个人积分增长/秒
	private final double honor;// = 5

	// # 完全控制联盟积分增长/秒
	private final double guildHonor; // = 2

	// # 完全控制获得buff bufId_val
	private final String buffList;// = 1427_5000,1428_5000,1429_8000
	// # 占领倒计时 /秒
	private final int controlCountDown;// = 30
	private final int collectArmyMin;// = 50000
	// # 保护时间
	private final int protectTime;
	private ImmutableMap<EffType, Integer> controleBuffMap;
	private ImmutableList<HawkTuple2<Integer, Integer>> refreshPointList;

	public CYBORGIronCurtainDeviceCfg() {
		firstControlHonor = 8;
		firstControlGuildHonor = 500;
		honor = 150;
		guildHonor = 10;
		refreshPoint = "43_42,58_59,60_61,75_50";
		buffList = "1427_5000,1428_5000,1429_8000";
		controlCountDown = 30;
		protectTime = 60;
		collectArmyMin = 50000;
	}

	@Override
	protected boolean assemble() {
		{
			List<HawkTuple2<Integer, Integer>> lsit = new ArrayList<>();
			for (String xy : refreshPoint.trim().split("\\,")) {
				String[] x_y = xy.split("_");
				int[] pos = new int[2];
				pos[0] = NumberUtils.toInt(x_y[0]);
				pos[1] = NumberUtils.toInt(x_y[1]);
				lsit.add(HawkTuples.tuple(pos[0], pos[1]));
			}
			refreshPointList = ImmutableList.copyOf(lsit);
		}

		{
			Map<EffType, Integer> lsit = new HashMap<>();
			for (String xy : buffList.trim().split("\\,")) {
				String[] x_y = xy.split("_");
				int[] pos = new int[2];
				pos[0] = NumberUtils.toInt(x_y[0]);
				pos[1] = NumberUtils.toInt(x_y[1]);
				lsit.put(EffType.valueOf(pos[0]), pos[1]);
			}
			controleBuffMap = ImmutableMap.copyOf(lsit);
		}

		return super.assemble();
	}

	public ImmutableMap<EffType, Integer> getControleBuffMap() {
		return controleBuffMap;
	}

	public void setControleBuffMap(ImmutableMap<EffType, Integer> controleBuffMap) {
		this.controleBuffMap = controleBuffMap;
	}

	public ImmutableList<HawkTuple2<Integer, Integer>> getRefreshPointList() {
		return refreshPointList;
	}

	public void setRefreshPointList(ImmutableList<HawkTuple2<Integer, Integer>> refreshPointList) {
		this.refreshPointList = refreshPointList;
	}

	public String getBuffList() {
		return buffList;
	}

	public double getFirstControlHonor() {
		return firstControlHonor;
	}

	public double getFirstControlGuildHonor() {
		return firstControlGuildHonor;
	}

	public double getHonor() {
		return honor;
	}

	public double getGuildHonor() {
		return guildHonor;
	}

	public String getRefreshPoint() {
		return refreshPoint;
	}

	public int getControlCountDown() {
		return controlCountDown;
	}

	public int getProtectTime() {
		return protectTime;
	}

	public int getCollectArmyMin() {
		return collectArmyMin;
	}

}
