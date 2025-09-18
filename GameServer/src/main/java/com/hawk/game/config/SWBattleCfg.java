package com.hawk.game.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuples;

import com.google.common.collect.ImmutableMap;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.service.starwars.StarWarsConst.SWWarType;

@HawkConfigManager.KVResource(file = "xml/sw_battle.xml")
public class SWBattleCfg extends HawkConfigBase {

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
	private final String bornPoint; // 15_13,22_34,34_28,33_33,18_18,40_40,45_50,55_65,70_75,88_90,66_67,45_49
	private final int deadToWoundPer;
	private final int tickPeriod;
	private final int towerAtk;// = 3
	// # 连续占领获胜时间 (秒)
	private final int conControlWin;
	// # 累计占领获胜时间(秒)
	private final int accControlWin;
	protected final String buffList1;// ="110_3000,132_3000"
	protected final String buffList2;// ="110_3000,132_3000"
	protected final String buffList3;// ="110_3000,132_3000"
	protected final int hospitalMinLevel;
	
	private List<HawkTuple2<Integer, Integer>> refreshPointList = new ArrayList<>();

	public SWBattleCfg() {
		prepairTime = 30;
		fireSpeed = 10;
		playerMarchSpeedUp = 1.5;
		mapX = 140;
		mapY = 140;
		moveCityCd = 30;
		scoreForKill = 5000;
		scoreForDefense = 10000;
		cureSpeedUp = 1;
		bornPoint = "15_13,22_34,34_28,33_33,18_18,40_40,45_50,55_65,70_75,88_90,66_67,45_49";
		deadToWoundPer = 0;
		towerAtk = 3;
		tickPeriod = 30000;
		conControlWin = 180;
		accControlWin = 360;
		buffList1 = "110_3000,132_3000";
		buffList2 = "110_3000,132_3000";
		buffList3 = "110_3000,132_3000";
		hospitalMinLevel = 0;
	}

	@Override
	protected boolean assemble() {
		for (String xy : bornPoint.trim().split("\\,")) {
			String[] x_y = xy.split("_");
			int[] pos = new int[2];
			pos[0] = NumberUtils.toInt(x_y[0]);
			pos[1] = NumberUtils.toInt(x_y[1]);
			refreshPointList.add(HawkTuples.tuple(pos[0], pos[1]));
		}

		return super.assemble();
	}

	public List<int[]> copyOfRefreshPointList() {
		List<int[]> result = new LinkedList<int[]>();
		for (HawkTuple2<Integer, Integer> ht : refreshPointList) {
			result.add(new int[] { ht.first, ht.second });
		}
		return result;
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

	public int getDeadToWoundPer() {
		return deadToWoundPer;
	}

	public int getTowerAtk() {
		return towerAtk;
	}

	public int getTickPeriod() {
		return tickPeriod;
	}

	/**毫秒*/
	public int getConControlWin() {
		return conControlWin * 1000;
	}

	/**毫秒*/
	public int getAccControlWin() {
		return accControlWin * 1000;
	}

	public ImmutableMap<EffType, Integer> getEffectMap(SWWarType warType) {
		switch (warType) {
		case FIRST_WAR:
			return splitbuff(buffList1);
		case SECOND_WAR:
			return splitbuff(buffList2);
		case THIRD_WAR:
			return splitbuff(buffList3);

		default:
			break;
		}
		return ImmutableMap.of();
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
	
}
