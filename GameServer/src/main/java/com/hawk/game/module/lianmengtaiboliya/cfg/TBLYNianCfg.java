package com.hawk.game.module.lianmengtaiboliya.cfg;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRand;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuples;

import com.google.common.collect.ImmutableList;
import com.hawk.game.protocol.Const.EffType;

@HawkConfigManager.KVResource(file = "xml/tbly_nian.xml")
public class TBLYNianCfg extends HawkConfigBase {
	// # 刷新时间点 开战后. /秒
	private final String refreshTime;// = 60,180,300
	// # 每次刷新数
	private final int refreshCount;// = 10
	// # 每次击杀获得buff 累加
	private final String buffList1;// = 100_2000,136_2000;
	private final String buffList2;// = 100_2000,136_2000;
	private final String buffList3;// = 100_2000,136_2000;
	private final String refreshPoint; // 15_13,22_34,34_28

	// #每点血量折算联盟积分
	private final double perHPGuildHonor;// = 1
	private final double perHPPlayerHonor;// = 0.01

	// #致命一击积分
	private final int onceKillGuildHonor;// = 1000
	private final int onceKillPlayerHonor;// = 100
	// # 击杀联盟积分
	private final int killGuildHonor;// = 1000
	private final int killPlayerHonor;// = 100

	// # 伤害加成倍数(万分比)
	private final int hurtRate;// = 10000

	// # 集结扣血上限(万分比)
	private final int massKillLimit;// = 80000
	private final int signKillLimit;// = 20000
	
//	# 攻击行军每战力扣血(万分比) 
	private final int perPowerHurt;// = 50;

	//# 设进入副本时双方战力 : X , 机甲血量 = X / hpMutiplePower * 配置血量 
	private final long hpMutiplePower;//=50000000
	//#哥，再给我一个参数吧，就是血量倍数上限10:48:00就是X/mutiple的上限不然可能会算出一个很凶的数值控制一下变化上限
	private final int hpMutipleMax;//=5
	private ImmutableList<HawkTuple2<EffType, Integer>> killBuffList1;
	private ImmutableList<HawkTuple2<EffType, Integer>> killBuffList2;
	private ImmutableList<HawkTuple2<EffType, Integer>> killBuffList3;
	private List<Integer> refreshTimeList = new ArrayList<>();
	private List<HawkTuple2<Integer, Integer>> refreshPointList = new ArrayList<>();

	public TBLYNianCfg() {
		refreshTime = "60,180,300";
		refreshCount = 10;
		buffList1 = "100_2000,136_2000";
		buffList2 = "100_2000,136_2000";
		buffList3 = "100_2000,136_2000";
		refreshPoint = "75_150,75_151";

		perHPGuildHonor = 1;
		perHPPlayerHonor = 0.01;

		onceKillGuildHonor = 1000;
		onceKillPlayerHonor = 100;
		killGuildHonor = 1000;
		killPlayerHonor = 100;

		hurtRate = 10000;

		massKillLimit = 80000;
		signKillLimit = 20000;
		
		perPowerHurt = 50;
		hpMutiplePower = 50000000;
		hpMutipleMax = 5;
	}

	public int[] randomBoinPoint() {
		int index = HawkRand.randInt(0, 100) % refreshPointList.size();
		HawkTuple2<Integer, Integer> p = refreshPointList.get(index);
		return new int[] { p.first, p.second };
	}

	public int getKillBuff(EffType effType, int nianKillCount) {
		int result = 0;
		if (nianKillCount > 0) {
			for (HawkTuple2<EffType, Integer> ev : killBuffList1) {
				if (ev.first == effType) {
					result += ev.second;
				}
			}
		}
		if (nianKillCount > 1) {
			for (HawkTuple2<EffType, Integer> ev : killBuffList2) {
				if (ev.first == effType) {
					result += ev.second;
				}
			}
		}
		if (nianKillCount > 2) {
			for (HawkTuple2<EffType, Integer> ev : killBuffList3) {
				if (ev.first == effType) {
					result += ev.second;
				}
			}
		}
		return result;
	}

	@Override
	protected boolean assemble() {
		for (String xy : refreshPoint.trim().split("\\,")) {
			String[] x_y = xy.split("_");
			int[] pos = new int[2];
			pos[0] = NumberUtils.toInt(x_y[0]);
			pos[1] = NumberUtils.toInt(x_y[1]);
			refreshPointList.add(HawkTuples.tuple(pos[0], pos[1]));
		}

		for (String xy : refreshTime.trim().split("\\,")) {
			refreshTimeList.add(NumberUtils.toInt(xy));
		}

		{
			killBuffList1 = ImmutableList.copyOf(splitBuff(buffList1));
			killBuffList2 = ImmutableList.copyOf(splitBuff(buffList2));
			killBuffList3 = ImmutableList.copyOf(splitBuff(buffList3));
		}

		return super.assemble();
	}

	private List<HawkTuple2<EffType, Integer>> splitBuff(String buffList) {
		List<HawkTuple2<EffType, Integer>> lsit = new ArrayList<>();
		for (String xy : buffList.trim().split("\\,")) {
			String[] x_y = xy.split("_");
			int[] pos = new int[2];
			pos[0] = NumberUtils.toInt(x_y[0]);
			pos[1] = NumberUtils.toInt(x_y[1]);
			lsit.add(HawkTuples.tuple(EffType.valueOf(pos[0]), pos[1]));
		}
		return lsit;
	}

	public List<Integer> getRefreshTimeList() {
		return refreshTimeList;
	}

	public String getRefreshTime() {
		return refreshTime;
	}

	public int getRefreshCount() {
		return refreshCount;
	}

	public int getHurtRate() {
		return hurtRate;
	}

	public int getKillGuildHonor() {
		return killGuildHonor;
	}

	public ImmutableList<HawkTuple2<EffType, Integer>> getKillBuffList1() {
		return killBuffList1;
	}

	public void setKillBuffList1(ImmutableList<HawkTuple2<EffType, Integer>> killBuffList1) {
		this.killBuffList1 = killBuffList1;
	}

	public ImmutableList<HawkTuple2<EffType, Integer>> getKillBuffList2() {
		return killBuffList2;
	}

	public void setKillBuffList2(ImmutableList<HawkTuple2<EffType, Integer>> killBuffList2) {
		this.killBuffList2 = killBuffList2;
	}

	public ImmutableList<HawkTuple2<EffType, Integer>> getKillBuffList3() {
		return killBuffList3;
	}

	public void setKillBuffList3(ImmutableList<HawkTuple2<EffType, Integer>> killBuffList3) {
		this.killBuffList3 = killBuffList3;
	}

	public List<HawkTuple2<Integer, Integer>> getRefreshPointList() {
		return refreshPointList;
	}

	public void setRefreshPointList(List<HawkTuple2<Integer, Integer>> refreshPointList) {
		this.refreshPointList = refreshPointList;
	}

	public String getBuffList1() {
		return buffList1;
	}

	public String getBuffList2() {
		return buffList2;
	}

	public String getBuffList3() {
		return buffList3;
	}

	public String getRefreshPoint() {
		return refreshPoint;
	}

	public double getPerHPGuildHonor() {
		return perHPGuildHonor;
	}

	public double getPerHPPlayerHonor() {
		return perHPPlayerHonor;
	}

	public int getOnceKillGuildHonor() {
		return onceKillGuildHonor;
	}

	public int getOnceKillPlayerHonor() {
		return onceKillPlayerHonor;
	}

	public int getKillPlayerHonor() {
		return killPlayerHonor;
	}

	public int getMassKillLimit() {
		return massKillLimit;
	}

	public int getSignKillLimit() {
		return signKillLimit;
	}

	public void setRefreshTimeList(List<Integer> refreshTimeList) {
		this.refreshTimeList = refreshTimeList;
	}

	public int getPerPowerHurt() {
		return perPowerHurt;
	}

	public long getHpMutiplePower() {
		return hpMutiplePower;
	}

	public int getHpMutipleMax() {
		return hpMutipleMax;
	}
	
}
