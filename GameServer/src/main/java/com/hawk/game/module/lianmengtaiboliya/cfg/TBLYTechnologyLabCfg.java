package com.hawk.game.module.lianmengtaiboliya.cfg;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuples;

import com.google.common.collect.ImmutableList;
import com.hawk.game.protocol.Const.EffType;

@HawkConfigManager.KVResource(file = "xml/tbly_technology_lab.xml")
public class TBLYTechnologyLabCfg extends HawkConfigBase {
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
	// - pointTime：累计控制触发悬赏的时间，单位秒
	private final int pointTime;
	// - pointBase：触发悬赏时的基础悬赏分
	private final double pointBase;
	// - pointSpeed：悬赏积分增加速度，单位秒
	private final double pointSpeed;
	// - pointMax：悬赏积分最大值
	private final double pointMax;

	private final int totalPoint;
	private final int battleOpen;
	private final String openTime;

	private ImmutableList<HawkTuple2<EffType, Integer>> controleBuffList;
	private ImmutableList<HawkTuple2<Integer, Integer>> refreshPointList;
	private ImmutableList<HawkTuple2<Integer, Integer>> openTimeList;

	public TBLYTechnologyLabCfg() {
		firstControlHonor = 0;
		firstControlGuildHonor = 0;
		honor = 0;
		guildHonor = 0;
		refreshPoint = "";
		buffList = "1427_0";
		controlCountDown = 0;
		protectTime = 0;
		collectArmyMin = 0;
		pointTime = 0;
		pointBase = 0;
		pointSpeed = 0;
		pointMax = 0;
		totalPoint = 0;
		battleOpen = 0;
		openTime = "";
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
			List<HawkTuple2<EffType, Integer>> lsit = new ArrayList<>();
			for (String xy : buffList.trim().split("\\,")) {
				String[] x_y = xy.split("_");
				int[] pos = new int[2];
				pos[0] = NumberUtils.toInt(x_y[0]);
				pos[1] = NumberUtils.toInt(x_y[1]);
				lsit.add(HawkTuples.tuple(EffType.valueOf(pos[0]), pos[1]));
			}
			controleBuffList = ImmutableList.copyOf(lsit);
		}

		{
			List<HawkTuple2<Integer, Integer>> lsit = new ArrayList<>();
			for (String xy : openTime.trim().split("\\,")) {
				String[] x_y = xy.split("_");
				int[] pos = new int[2];
				pos[0] = NumberUtils.toInt(x_y[0]);
				pos[1] = NumberUtils.toInt(x_y[1]);
				lsit.add(HawkTuples.tuple(pos[0], pos[1]));
			}
			openTimeList = ImmutableList.copyOf(lsit);
		}

		return super.assemble();
	}

	public ImmutableList<HawkTuple2<EffType, Integer>> getControlebuffList() {
		return controleBuffList;
	}

	public void setControlebuffList(ImmutableList<HawkTuple2<EffType, Integer>> controlebuffList) {
		this.controleBuffList = controlebuffList;
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

	public ImmutableList<HawkTuple2<EffType, Integer>> getControleBuffList() {
		return controleBuffList;
	}

	public void setControleBuffList(ImmutableList<HawkTuple2<EffType, Integer>> controleBuffList) {
		this.controleBuffList = controleBuffList;
	}

	public int getPointTime() {
		return pointTime;
	}

	public double getPointBase() {
		return pointBase;
	}

	public double getPointSpeed() {
		return pointSpeed;
	}

	public double getPointMax() {
		return pointMax;
	}

	public ImmutableList<HawkTuple2<Integer, Integer>> getOpenTimeList() {
		return openTimeList;
	}

	public void setOpenTimeList(ImmutableList<HawkTuple2<Integer, Integer>> openTimeList) {
		this.openTimeList = openTimeList;
	}

	public int getTotalPoint() {
		return totalPoint;
	}

	public int getBattleOpen() {
		return battleOpen;
	}

	public String getOpenTime() {
		return openTime;
	}

}
