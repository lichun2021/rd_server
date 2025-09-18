package com.hawk.game.module.lianmengyqzz.battleroom.cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.hawk.game.protocol.Const.EffType;

@HawkConfigManager.KVResource(file = "xml/moon_war_battle.xml")
public class YQZZBattleCfg extends HawkConfigBase {

	protected final int prepairTime;
	protected final int fireSpeed;
	protected final double playerMarchSpeedUp;// ="1.5"
	protected final int mapX;// = 140;
	protected final int mapY;// = 140;
	protected final int moveCityCd;
	// # 副本建筑内击杀敌方战斗力获取1个人积分
	protected final int scoreForKill;// = 50000
	protected final double cureSpeedUp;
	// # 副本建筑内防御坦克损失战斗力获取1个人积分
	protected final int scoreForDefense;// = 10000
	protected final int hospitalMinLevel;
	protected final int nationMilitaryMax;
	// # 初始
	protected final int declareWarOrder;// = 5
	// #最高
	protected final int declareWarOrderMax;// = 5
	// #+1 秒
	protected final int declareWarOrderSpeed;// = 1200

	protected final int hospitalCapacity;

	protected final int idelKick;
	protected final int orderVal;
	protected final int foggyLimit;
	protected final int monsterLimit;
	protected final int pylonLimit;
	protected final int foggyAssembleLimit;
	protected final int foggyStartAssembleLimit;
	
	protected final int giveupBuildCD;
	// # 升级盟军航天中心，有以下效果：1. 能减少宣战令的恢复时间，单位分钟。
	protected final String declareWarOrderSpeedAdd;// = 1,2,3,4,5,6,7,8,9,10

	// # 升级盟军航天中心，有以下效果：2. 能增加副本内医院容量（百分比，与moon_war_battle表的hospitalCapacity的增加值呈线性叠加关系）
	protected final String hospitalCapacityAdd;// = 30,60,90,120,150,180,210,240,270,300
	protected final String playerRankRate;
	// # 击杀也怪+技能令
	protected final int killMonsterOrderVal;// = 1

	private ImmutableList<Integer> declareWarOrderSpeedAddList;
	private ImmutableList<Integer> hospitalCapacityAddList;
	private ImmutableList<Double> playerRankRateList;

	public YQZZBattleCfg() {
		monsterLimit = 0;
		pylonLimit = 0;
		killMonsterOrderVal = 1;
		giveupBuildCD = 0;
		playerRankRate = "";
		foggyLimit = 0;
		prepairTime = 30;
		fireSpeed = 10;
		playerMarchSpeedUp = 1.5;
		mapX = 140;
		mapY = 140;
		moveCityCd = 30;
		scoreForKill = 5000;
		scoreForDefense = 10000;
		cureSpeedUp = 1;
		hospitalMinLevel = 0;
		nationMilitaryMax = 0;
		declareWarOrder = 5;
		declareWarOrderMax = 5;
		declareWarOrderSpeed = 1200;
		hospitalCapacity = 0;
		declareWarOrderSpeedAdd = "";
		hospitalCapacityAdd = "";
		idelKick = 30 * 60;
		orderVal = 666;
		foggyAssembleLimit = 0;
		foggyStartAssembleLimit = 0;
	}

	public double playerRankRate(int rank) {
		try {
			int size = playerRankRateList.size();
			if (size == 0 || rank == 0) {
				return 0;
			}
			return playerRankRateList.get(Math.min(rank, size) - 1);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}

	public int declareWarOrderSpeedAdd(int level) {
		try {
			int size = declareWarOrderSpeedAddList.size();
			if (size == 0 || level == 0) {
				return 0;
			}
			return declareWarOrderSpeedAddList.get(Math.min(level, size) - 1);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}

	public int hospitalCapacityAdd(int level) {
		try {
			int size = hospitalCapacityAddList.size();
			if (size == 0 || level == 0) {
				return 0;
			}
			return hospitalCapacityAddList.get(Math.min(level, size) - 1);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}

	@Override
	protected boolean assemble() {
		{
			List<Integer> list = new ArrayList<>();
			Splitter.on(",").omitEmptyStrings().trimResults().split(declareWarOrderSpeedAdd).forEach(str -> {
				list.add(NumberUtils.toInt(str));
			});
			this.declareWarOrderSpeedAddList = ImmutableList.copyOf(list);
		}
		{
			List<Integer> list = new ArrayList<>();
			Splitter.on(",").omitEmptyStrings().trimResults().split(hospitalCapacityAdd).forEach(str -> {
				list.add(NumberUtils.toInt(str));
			});
			this.hospitalCapacityAddList = ImmutableList.copyOf(list);
		}
		{
			List<Double> list = new ArrayList<>();
			Splitter.on(",").omitEmptyStrings().trimResults().split(playerRankRate).forEach(str -> {
				list.add(NumberUtils.toDouble(str));
			});
			this.playerRankRateList = ImmutableList.copyOf(list);
		}
		return super.assemble();
	}

	public int getPrepairTime() {
		return prepairTime;
	}

	public int getFireSpeed() {
		return fireSpeed;
	}

	public double getPlayerMarchSpeedUp() {
		return playerMarchSpeedUp;
	}

	public int getMapX() {
		return mapX;
	}

	public int getMapY() {
		return mapY;
	}

	public int getMoveCityCd() {
		return moveCityCd;
	}

	public int getScoreForKill() {
		return scoreForKill;
	}

	public int getScoreForDefense() {
		return scoreForDefense;
	}

	public double getCureSpeedUp() {
		return cureSpeedUp;
	}

	private ImmutableMap<EffType, Integer> splitbuff(String buffList) {
		Map<EffType, Integer> tampMap = new HashMap<>();
		if (!HawkOSOperator.isEmptyString(buffList)) {
			String[] array = buffList.split(",");
			for (String val : array) {
				String[] info = val.split("_");
				tampMap.put(EffType.valueOf(Integer.parseInt(info[0])), Integer.parseInt(info[1]));
			}
		}

		return ImmutableMap.copyOf(tampMap);
	}

	public int getHospitalMinLevel() {
		return hospitalMinLevel;
	}

	public int getNationMilitaryMax() {
		return nationMilitaryMax;
	}

	public int getDeclareWarOrder() {
		return declareWarOrder;
	}

	public int getDeclareWarOrderMax() {
		return declareWarOrderMax;
	}

	public int getDeclareWarOrderSpeed() {
		return declareWarOrderSpeed;
	}

	public int getHospitalCapacity() {
		return hospitalCapacity;
	}

	public String getDeclareWarOrderSpeedAdd() {
		return declareWarOrderSpeedAdd;
	}

	public String getHospitalCapacityAdd() {
		return hospitalCapacityAdd;
	}

	public int getIdelKick() {
		return idelKick;
	}

	public int getOrderVal() {
		return orderVal;
	}

	public int getFoggyLimit() {
		return foggyLimit;
	}

	public int getGiveupBuildCD() {
		return giveupBuildCD;
	}

	public int getKillMonsterOrderVal() {
		return killMonsterOrderVal;
	}

	public int getMonsterLimit() {
		return monsterLimit;
	}

	public int getPylonLimit() {
		return pylonLimit;
	}

	public int getFoggyAssembleLimit() {
		return foggyAssembleLimit;
	}

	public int getFoggyStartAssembleLimit() {
		return foggyStartAssembleLimit;
	}

}
