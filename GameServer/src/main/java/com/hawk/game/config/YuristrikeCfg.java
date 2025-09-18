package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.march.ArmyInfo;

@HawkConfigManager.XmlResource(file = "xml/yuri_Strikes.xml")
public class YuristrikeCfg extends HawkConfigBase {
	@Id
	protected final int id;
	protected final int triggerPlot;
	protected final int triggerCityLevel;// ="5"
	protected final int triggerPlayerLevel;
	protected final int triggerPower;// ="5"
	protected final int triggerMilitaryRank;
	protected final int scanningTime;
	protected final String enemyArmy; // "100202_242|100402_232|100602_277|100802_234"
	protected final String rewardArmy; // "100101_500|100201_1000|100301_666"
	protected final int arrivalTime; // ="360"
	protected final int purifyTime; // ="300"
	protected final String purifyCost; // ="10000_1000_1000"
	protected final String rewards; //

	private static final TreeMap<Integer, Boolean> TMap = new TreeMap<>();

	public YuristrikeCfg() {
		id = 0;
		triggerPlot = 0;
		triggerPlayerLevel = 0;
		triggerPower = 1;
		triggerCityLevel = 1;
		enemyArmy = "";
		rewardArmy = "";
		triggerMilitaryRank = 0;
		scanningTime = 0;
		purifyTime = 0;
		purifyCost = "";
		arrivalTime = 0;
		rewards = "";
	}

	public static Integer higherCfgId(int cfgId) {
		return TMap.higherKey(cfgId);
	}

	@Override
	protected boolean checkValid() {
		this.getEnemyList();
		this.getRewardArmyList();
		return super.checkValid();
	}

	public int getId() {
		return id;
	}

	public int getTriggerCityLevel() {
		return triggerCityLevel;
	}

	public int getTriggerPower() {
		return triggerPower;
	}

	public int getTriggerPlot() {
		return triggerPlot;
	}

	public int getTriggerPlayerLevel() {
		return triggerPlayerLevel;
	}

	public int getTriggerMilitaryRank() {
		return triggerMilitaryRank;
	}

	public int getScanningTime() {
		return scanningTime;
	}

	public String getEnemyArmy() {
		return enemyArmy;
	}

	public String getRewardArmy() {
		return rewardArmy;
	}

	public List<ArmyInfo> getEnemyList() {
		List<ArmyInfo> enemyList = getArmyList(enemyArmy);
		return enemyList;
	}

	public List<ArmyInfo> getRewardArmyList() {
		List<ArmyInfo> rewardArmyList = getArmyList(rewardArmy);
		return rewardArmyList;
	}

	private List<ArmyInfo> getArmyList(String armyStr) {
		List<ArmyInfo> enemyList = new ArrayList<>();
		String[] marr = armyStr.split(",");
		for (String monsterStr : marr) {
			String[] monsters = monsterStr.split("_");
			enemyList.add(new ArmyInfo(Integer.parseInt(monsters[0]), Integer.parseInt(monsters[1])));
		}
		return enemyList;
	}

	public int getArrivalTime() {
		return arrivalTime;
	}

	public int getPurifyTime() {
		return purifyTime;
	}

	public String getPurifyCost() {
		return purifyCost;
	}

	public String getRewards() {
		return rewards;
	}

	@Override
	protected boolean assemble() {
		TMap.put(id, Boolean.TRUE);
		return true;
	}
}
