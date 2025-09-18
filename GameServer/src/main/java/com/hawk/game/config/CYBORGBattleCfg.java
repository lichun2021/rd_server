package com.hawk.game.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuples;

import com.google.common.collect.ImmutableMap;
import com.hawk.game.protocol.Const.SoldierType;

@HawkConfigManager.KVResource(file = "xml/cyborg_battle.xml")
public class CYBORGBattleCfg extends HawkConfigBase {

	protected final int prepairTime;
	protected final int fireSpeed;
	protected final String bornPointA;// ="24,25|26,27|22,22"
	protected final String bornPointB;// ="24,25|20,18|18,16"
	protected final String bornPointC;// ="24,25|26,27|22,22"
	protected final String bornPointD;// ="24,25|20,18|18,16"
	protected final double playerMarchSpeedUp;// ="1.5"
	protected final int mapX;// = 140;
	protected final int mapY;// = 140;
	protected final int moveCityCd;
	// # 副本建筑内击杀敌方战斗力获取1个人积分
	protected final int scoreForKill;// = 50000
	protected final double cureSpeedUp;
	// # 副本建筑内防御坦克损失战斗力获取1个人积分
	protected final String scoreForDefense;// = 10000
	protected final int hotBloodModel;
	protected final int monsterHerald;
	protected final int playerBuildBuff;
	private List<HawkTuple2<Integer, Integer>> bornPointAList = new ArrayList<>();
	private List<HawkTuple2<Integer, Integer>> bornPointBList = new ArrayList<>();
	private List<HawkTuple2<Integer, Integer>> bornPointCList = new ArrayList<>();
	private List<HawkTuple2<Integer, Integer>> bornPointDList = new ArrayList<>();
	private ImmutableMap<SoldierType, Integer> scoreForDefenseAdjustMap = ImmutableMap.of();
	public CYBORGBattleCfg() {
		this.bornPointA = "";
		this.bornPointB = "";
		this.bornPointC = "";
		this.bornPointD = "";
		prepairTime = 30;
		fireSpeed = 10;
		playerMarchSpeedUp = 1.5;
		mapX = 140;
		mapY = 140;
		moveCityCd = 30;
		scoreForKill = 5000;
		scoreForDefense = "";
		cureSpeedUp = 1;
		hotBloodModel = 600;
		monsterHerald = 60;
		playerBuildBuff = 0;
	}

	public int getScoreForDefense(SoldierType key) {
		return scoreForDefenseAdjustMap.getOrDefault(key, 0);
	}

	@Override
	protected boolean assemble() {
		fillBornPoint(getBornPointA(), bornPointAList);
		fillBornPoint(getBornPointB(), bornPointBList);
		fillBornPoint(getBornPointC(), bornPointCList);
		fillBornPoint(getBornPointD(), bornPointDList);

		if (bornPointAList.size() < 2 || bornPointBList.size() < 2) {
			throw new RuntimeException("BornPoint error " + bornPointA);
		}

		if (!StringUtils.isEmpty(scoreForDefense)) {
			String[] strs = scoreForDefense.trim().split(",");
			Map<SoldierType, Integer> map = new HashMap<>();
			for (String str : strs) {
				String[] arr = str.split("_");
				map.put(SoldierType.valueOf(Integer.parseInt(arr[0])), Integer.valueOf(arr[1]));
			}
			scoreForDefenseAdjustMap = ImmutableMap.copyOf(map);
		}
		
		return super.assemble();
	}

	private void fillBornPoint(String pointstr, List<HawkTuple2<Integer, Integer>> bornPointList) {
		for (String xy : pointstr.trim().split("\\,")) {
			String[] x_y = xy.split("_");
			if (x_y.length != 2) {
				continue;
			}
			int[] pos = new int[2];
			pos[0] = NumberUtils.toInt(x_y[0]);
			pos[1] = NumberUtils.toInt(x_y[1]);
			bornPointList.add(HawkTuples.tuple(pos[0], pos[1]));
		}
	}

	public List<int[]> copyOfbornPointAList() {
		return copyOfbornPointList(bornPointAList);
	}

	public List<int[]> copyOfbornPointBList() {
		return copyOfbornPointList(bornPointBList);
	}

	public List<int[]> copyOfbornPointCList() {
		return copyOfbornPointList(bornPointCList);
	}

	public List<int[]> copyOfbornPointDList() {
		return copyOfbornPointList(bornPointDList);
	}

	private List<int[]> copyOfbornPointList(List<HawkTuple2<Integer, Integer>> bornPointList) {
		List<int[]> result = new LinkedList<int[]>();
		for (HawkTuple2<Integer, Integer> ht : bornPointList) {
			result.add(new int[] { ht.first, ht.second });
		}
		return result;
	}

	public String getBornPointA() {
		return bornPointA;
	}

	public String getBornPointB() {
		return bornPointB;
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

	public String getScoreForDefense() {
		return scoreForDefense;
	}

	public double getCureSpeedUp() {
		return cureSpeedUp;
	}

	public int getHotBloodModel() {
		return hotBloodModel;
	}

	public String getBornPointC() {
		return bornPointC;
	}

	public String getBornPointD() {
		return bornPointD;
	}

	public int getMonsterHerald() {
		return monsterHerald;
	}

	public int getPlayerBuildBuff() {
		return playerBuildBuff;
	}

}
