package com.hawk.game.module.spacemecha.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.tuple.HawkTuple2;
import com.google.common.base.Splitter;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.march.ArmyInfo;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 星甲召唤据点配置
 *
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "xml/space_machine_stronghold.xml")
public class SpaceMechaStrongholdCfg extends HawkConfigBase {
	@Id
	protected final int id;
	/**
	 * 对应舱体的等级
	 */
	protected final int level;
	/**
	 * 标识是否是特殊据点
	 */
	protected final int isSpecial;
	/**
	 * 占地格子数
	 */
	protected final int gridCnt;
	
	/**
	 * 据点防守英雄列表
	 */
	protected final String heroList;
	
	/**
	 * 据点防守部队
	 */
	protected final String soldierList;
	/**
	 * 血管数量
	 */
	protected final int hpNumber;
	
	/**
	 * 据点进攻部队兵力配置
	 */
	protected final String atkEnemy;
	/**
	 * 进攻波次
	 */
	protected final int atkWave;
	/**
	 * 进攻cd
	 */
	protected final int atkWaveCd;
	/**
	 * 据点每波出多少个enemy单位
	 */
	protected final int atkEnemyWaveNum;
	
	protected final String atkAward;
	
	// 克制关系的作用号
	protected final String curbBuff;
	
	protected final String effects;
	
	protected final String spWinEffects;
	
	protected final String spLoseEffects;
	
	protected final String soldierEffect;
	
	/**
	 * 据点刷出普通怪所包含的部队和权重
	 */
	private List<Integer> atkEnemyIdList = new ArrayList<>();
	private List<Integer> atkEnemyWeightList = new ArrayList<>();
	
	private List<HawkTuple2<Integer, Integer>> effectList = new ArrayList<>();
	private List<Integer> effectWeight = new ArrayList<>();
	
	private List<ArmyInfo> armyList;
	private List<ItemInfo> atkAwardItemList;
	
	private List<Integer> heroIdList;
	
	private Map<Integer, Integer> spWinEffectMap;
	private Map<Integer, Integer> spLoseEffectMap;
	private Map<Integer, Integer> soldierEffectMap;
	/**
	 * 克制buff
	 */
	Map<Integer, Integer> curbBuffMap;
	
	public SpaceMechaStrongholdCfg() {
		id = 0;
		level = 0;
		isSpecial = 0;
		gridCnt = 0;
		heroList = "";
		soldierList = "";
		hpNumber = 1;
		atkEnemy = "";
		atkWave = 0;
		atkWaveCd = 0;
		atkEnemyWaveNum = 0;
		atkAward = "";
		effects = "";
		spWinEffects = "";
		spLoseEffects = "";
		curbBuff = "";
		soldierEffect = "";
	}

	public int getId() {
		return id;
	}

	public int getLevel() {
		return level;
	}

	public int getIsSpecial() {
		return isSpecial;
	}

	public int getGridCnt() {
		return gridCnt;
	}

	public String getHeroList() {
		return heroList;
	}
	
	public String getSoldierList() {
		return soldierList;
	}

	public String getAtkEnemy() {
		return atkEnemy;
	}

	public int getAtkWave() {
		return atkWave;
	}

	public long getAtkWaveCd() {
		return atkWaveCd * 1000L;
	}

	public int getAtkEnemyWaveNum() {
		return atkEnemyWaveNum;
	}

	public String getAtkAward() {
		return atkAward;
	}

	public String getCurbBuff() {
		return curbBuff;
	}

	public String getEffects() {
		return effects;
	}
	
	public String getSpWinEffects() {
		return spWinEffects;
	}

	public String getSpLoseEffects() {
		return spLoseEffects;
	}

	public int getHpNumber() {
		return hpNumber;
	}
	
	public boolean assemble() {
		String[] enemyArray = atkEnemy.split(",");
		for (String enemy : enemyArray) {
			String[] idWeight = enemy.split("_");
			atkEnemyIdList.add(Integer.parseInt(idWeight[0]));
			atkEnemyWeightList.add(Integer.parseInt(idWeight[1]));
		}
		
		List<ArmyInfo> armyList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(soldierList)) {
			for (String army : Splitter.on("|").split(soldierList)) {
				String[] armyStrs = army.split("_");
				armyList.add(new ArmyInfo(Integer.parseInt(armyStrs[0]), Integer.parseInt(armyStrs[1])));
			}
		}
		this.armyList = armyList;
		
		atkAwardItemList = ItemInfo.valueListOf(atkAward);
		
		this.heroIdList = SerializeHelper.stringToList(Integer.class, heroList, "\\|");
		
		String[] effectStr = effects.split(",");
		for (String str : effectStr) {
			String[] effParams = str.split("_");
			HawkTuple2<Integer, Integer> tuple = new HawkTuple2<Integer, Integer>(Integer.parseInt(effParams[0]), Integer.parseInt(effParams[1]));
			effectList.add(tuple);
			effectWeight.add(Integer.parseInt(effParams[2]));
		}
		
		spWinEffectMap = SerializeHelper.stringToMap(spWinEffects, Integer.class, Integer.class, "_", ",");
		spLoseEffectMap = SerializeHelper.stringToMap(spLoseEffects, Integer.class, Integer.class, "_", ",");
		soldierEffectMap = SerializeHelper.stringToMap(soldierEffect, Integer.class, Integer.class, "_", ",");
		
		curbBuffMap = SerializeHelper.stringToMap(curbBuff, Integer.class, Integer.class, "_", ",");
		
		return true;
	}
	
	public int getCurbBuff(int effId) {
		return curbBuffMap.getOrDefault(effId, 0);
	}
	
	public HawkTuple2<Integer, Integer> randomEffect() {
		HawkTuple2<Integer, Integer> tuple = HawkRand.randomWeightObject(effectList, effectWeight);
		return tuple;
	}
	
	public Map<Integer, Integer> getSpWinEffectMap() {
		return spWinEffectMap;
	} 
	
	public Map<Integer, Integer> getSpLoseEffectMap() {
		return spLoseEffectMap;
	} 
	
	public Map<Integer, Integer> getSoldierEffectMap() {
		return soldierEffectMap;
	}
	
	public List<Integer> getAtkEnemyIdList() {
		return atkEnemyIdList;
	}
	
	public List<Integer> getAtkEnemyWeightList() {
		return atkEnemyWeightList;
	}

	public List<ArmyInfo> getArmyList() {
		return this.armyList.stream().map(e -> e.getCopy()).collect(Collectors.toList());
	}
	
	public List<ItemInfo> getAtkAwardItemList() {
		return atkAwardItemList.stream().map(e -> e.clone()).collect(Collectors.toList());
	}
	
	public List<Integer> getHeroIdList() {
		return heroIdList;
	}
	
	public int getBlood() {
		int blood = 0;
		for (ArmyInfo army : getArmyList()) {
			blood += army.getTotalCount();
		}
		
		return blood; 
	}
	
}
