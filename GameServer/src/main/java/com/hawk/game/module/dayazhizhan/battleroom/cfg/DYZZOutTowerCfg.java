package com.hawk.game.module.dayazhizhan.battleroom.cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuples;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.hawk.game.protocol.Const.EffType;

@HawkConfigManager.KVResource(file = "xml/dyzz_out_tower.xml")
public class DYZZOutTowerCfg extends HawkConfigBase {

	private final int redis;
	// # 刷新点 x_y, 半径为偶数 x,y相加必须为奇数
	private final String refreshPointA;// = 43_42,58_59,60_61,75_50
	private final String refreshPointB;// = 43_42,58_59,60_61,75_50
	// # 采集阶段我方部队, 塔内战斗获得buff加成
	private final String collectBuffList;// = 100_5000,136_5000
	// # 占领倒计时 /秒
	private final int controlCountDown;// = 30
	private final int destroyCountDown;

	// # 攻击间隔
	private final int atkCd;// = 30
	// # 对基地伤害
	private final int atkVal;// = 50
	// #能源井攻击
	private final int wellAtkVal;// = 3
	// #商人买
	private final int orderAtkVal;// = 100
	private ImmutableList<HawkTuple2<Integer, Integer>> refreshPointAList;
	private ImmutableList<HawkTuple2<Integer, Integer>> refreshPointBList;
	private ImmutableMap<EffType, Integer> collectBuffMap;

	public DYZZOutTowerCfg() {
		redis = 3;
		refreshPointA = "43_42,58_59";
		refreshPointB = "43_42,58_59";
		controlCountDown = 30;
		destroyCountDown = 30;
		collectBuffList = "100_5000,136_5000";
		atkCd = 30;
		atkVal = 50;
		wellAtkVal = 0;
		orderAtkVal = 0;
	}

	@Override
	protected boolean assemble() {
		{
			List<HawkTuple2<Integer, Integer>> lsit = new ArrayList<>();
			for (String xy : refreshPointA.trim().split("\\,")) {
				String[] x_y = xy.split("_");
				int[] pos = new int[2];
				pos[0] = NumberUtils.toInt(x_y[0]);
				pos[1] = NumberUtils.toInt(x_y[1]);
				lsit.add(HawkTuples.tuple(pos[0], pos[1]));
			}
			refreshPointAList = ImmutableList.copyOf(lsit);
		}
		{
			List<HawkTuple2<Integer, Integer>> lsit = new ArrayList<>();
			for (String xy : refreshPointB.trim().split("\\,")) {
				String[] x_y = xy.split("_");
				int[] pos = new int[2];
				pos[0] = NumberUtils.toInt(x_y[0]);
				pos[1] = NumberUtils.toInt(x_y[1]);
				lsit.add(HawkTuples.tuple(pos[0], pos[1]));
			}
			refreshPointBList = ImmutableList.copyOf(lsit);
		}

		{
			Map<EffType, Integer> lsit = new HashMap<>();
			for (String xy : Splitter.on(",").omitEmptyStrings().splitToList(collectBuffList)) {
				String[] x_y = xy.split("_");
				int[] pos = new int[2];
				pos[0] = NumberUtils.toInt(x_y[0]);
				pos[1] = NumberUtils.toInt(x_y[1]);
				lsit.put(EffType.valueOf(pos[0]), pos[1]);
			}
			collectBuffMap = ImmutableMap.copyOf(lsit);
		}

		return super.assemble();
	}

	public int getControlCountDown() {
		return controlCountDown;
	}

	public int getRedis() {
		return redis;
	}

	public ImmutableList<HawkTuple2<Integer, Integer>> getRefreshPointAList() {
		return refreshPointAList;
	}

	public void setRefreshPointAList(ImmutableList<HawkTuple2<Integer, Integer>> refreshPointAList) {
		this.refreshPointAList = refreshPointAList;
	}

	public ImmutableList<HawkTuple2<Integer, Integer>> getRefreshPointBList() {
		return refreshPointBList;
	}

	public void setRefreshPointBList(ImmutableList<HawkTuple2<Integer, Integer>> refreshPointBList) {
		this.refreshPointBList = refreshPointBList;
	}

	public ImmutableMap<EffType, Integer> getCollectBuffMap() {
		return collectBuffMap;
	}

	public void setCollectBuffMap(ImmutableMap<EffType, Integer> collectBuffMap) {
		this.collectBuffMap = collectBuffMap;
	}

	public String getRefreshPointA() {
		return refreshPointA;
	}

	public String getRefreshPointB() {
		return refreshPointB;
	}

	public String getCollectBuffList() {
		return collectBuffList;
	}

	public int getDestroyCountDown() {
		return destroyCountDown;
	}

	public int getAtkCd() {
		return atkCd;
	}

	public int getAtkVal() {
		return atkVal;
	}

	public int getWellAtkVal() {
		return wellAtkVal;
	}

	public int getOrderAtkVal() {
		return orderAtkVal;
	}

}
