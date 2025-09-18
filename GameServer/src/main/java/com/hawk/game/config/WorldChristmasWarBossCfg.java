package com.hawk.game.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.google.common.base.Splitter;
import com.hawk.game.march.ArmyInfo;

/**
 * 圣诞boss
 * @author 圣诞boss
 *
 */
@HawkConfigManager.XmlResource(file = "xml/world_christmas_war_boss.xml")
public class WorldChristmasWarBossCfg extends HawkConfigBase {

	@Id
	protected final int id;

	/**
	 * 士兵(士兵总数就是总血量)
	 */
	protected final String soldier;

	/**
	 * 攻击奖励
	 */
	protected final String atkAward;

	/**
	 * 致命一击奖励
	 */
	protected final String deadlyAward;

	/**
	 * 最终击杀奖励
	 */
	protected final String killAward;

	/**
	 * 血条数量
	 */
	protected final int hpNumber;

	/**
	 * 致命一击集结奖励
	 */
	protected final String deadlyMassAward;

	/**
	 * 致命一击掉落
	 */
	protected final String onceKillDrop;
	
	/**
	 * 击杀掉落
	 */
	protected final String killDrop;
	
	/**
	 * 击杀集结奖励
	 */
	protected final String killMassAward;

	private List<ArmyInfo> armyList;

	private List<Integer> atkAwards;

	private List<Integer> deadlyAwards;

	private List<Integer> killAwards;

	private List<Integer> deadlyMassAwards;

	private List<Integer> killMassAwards;

	private Map<Integer, Integer> onceKillDropMap;
	
	private Map<Integer, Integer> killDropMap;
	
	public WorldChristmasWarBossCfg() {
		id = 0;
		soldier = "";
		atkAward = "";
		deadlyAward = "";
		killAward = "";
		hpNumber = 1;
		deadlyMassAward = "";
		killMassAward = "";
		onceKillDrop = "";
		killDrop = "";
	}

	public int getId() {
		return id;
	}

	public int getHpNumber() {
		return hpNumber;
	}

	public List<ArmyInfo> getArmyList() {
		List<ArmyInfo> list = new ArrayList<>();
		armyList.forEach(e -> list.add(e.getCopy()));
		return list;
	}

	public List<Integer> getKillAwards() {
		return new ArrayList<>(killAwards);
	}

	public List<Integer> getDeadlyAwards() {
		return new ArrayList<>(deadlyAwards);
	}

	public List<Integer> getAtkAwards() {
		return new ArrayList<>(atkAwards);
	}

	public List<Integer> getDeadlyMassAwards() {
		return deadlyMassAwards;
	}

	public List<Integer> getKillMassAwards() {
		return killMassAwards;
	}

	public Map<Integer, Integer> getOnceKillDropMap() {
		return onceKillDropMap;
	}

	public Map<Integer, Integer> getKillDropMap() {
		return killDropMap;
	}

	@Override
	protected boolean assemble() {

		List<ArmyInfo> armyList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(soldier)) {
			for (String army : Splitter.on("|").split(soldier)) {
				String[] armyStrs = army.split("_");
				armyList.add(new ArmyInfo(Integer.parseInt(armyStrs[0]), Integer.parseInt(armyStrs[1])));
			}
		}
		this.armyList = armyList;

		List<Integer> killAawards = new ArrayList<Integer>();
		if (!HawkOSOperator.isEmptyString(killAward)) {
			for (String award : Splitter.on(";").split(killAward)) {
				killAawards.add(Integer.parseInt(award));
			}
		}
		this.killAwards = killAawards;

		List<Integer> atkAwards = new ArrayList<Integer>();
		if (!HawkOSOperator.isEmptyString(atkAward)) {
			for (String award : Splitter.on(";").split(atkAward)) {
				atkAwards.add(Integer.parseInt(award));
			}
		}
		this.atkAwards = atkAwards;

		List<Integer> deadlyAwards = new ArrayList<Integer>();
		if (!HawkOSOperator.isEmptyString(deadlyAward)) {
			for (String award : Splitter.on(";").split(deadlyAward)) {
				deadlyAwards.add(Integer.parseInt(award));
			}
		}
		this.deadlyAwards = deadlyAwards;

		List<Integer> deadlyMassAwards = new ArrayList<Integer>();
		if (!HawkOSOperator.isEmptyString(deadlyMassAward)) {
			for (String award : Splitter.on(";").split(deadlyMassAward)) {
				deadlyMassAwards.add(Integer.parseInt(award));
			}
		}
		this.deadlyMassAwards = deadlyMassAwards;

		List<Integer> killMassAwards = new ArrayList<Integer>();
		if (!HawkOSOperator.isEmptyString(killMassAward)) {
			for (String award : Splitter.on(";").split(killMassAward)) {
				killMassAwards.add(Integer.parseInt(award));
			}
		}
		this.killMassAwards = killMassAwards;

		Map<Integer, Integer> onceKillDropMap = new HashMap<>();
		if (!HawkOSOperator.isEmptyString(onceKillDrop)) {
			for (String drop : Splitter.on(",").split(onceKillDrop)) {
				String[] dropInfo = drop.split("_");
				onceKillDropMap.put(Integer.valueOf(dropInfo[0]), Integer.valueOf(dropInfo[1]));
			}
		}
		this.onceKillDropMap = onceKillDropMap; 
		
		Map<Integer, Integer> killDropMap = new HashMap<>();
		if (!HawkOSOperator.isEmptyString(killDrop)) {
			for (String drop : Splitter.on(",").split(killDrop)) {
				String[] dropInfo = drop.split("_");
				killDropMap.put(Integer.valueOf(dropInfo[0]), Integer.valueOf(dropInfo[1]));
			}
		}
		this.killDropMap = killDropMap;
		
		return true;
	}
}