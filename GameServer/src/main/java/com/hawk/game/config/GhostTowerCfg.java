package com.hawk.game.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.tuple.HawkTuple5;
import org.hawk.tuple.HawkTuples;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.hawk.game.protocol.Const.EffType;

@HawkConfigManager.XmlResource(file = "xml/ghost_tower.xml")
public class GhostTowerCfg extends HawkConfigBase {

	@Id
	protected final int id;
	
	protected final int level;
	
	protected final int floor;
	
	protected final int buildcfgId;
	
	protected final int productTime;
	
	protected final int productMaxCount;
	
	protected final String production;
	
	protected final int randomProduction;
	
	protected final int marchTime;
	
	protected final String killReward;
	
	protected final String areaRadius;
	
	protected final int ghostLiftTime;
	
	protected final String effectList;
	
	protected final String heroList;

	protected final String trapNumSec;

	protected final String trapList;

	protected final String soldierNumSec;

	protected final String soldierList;
	
	protected final String superSoldier; 
	
	protected final String armourList;
	
	

	private int areaRadiusMax;
	private int areaRadiusMin;
	private int superSoldierId;
	private int superSoldierLevel;
	private List<Integer> heroIds;
	private List<TowerArmyRandInfo> foggyTrapRandInfos;
	private List<TowerArmyRandInfo> foggySoldierRandInfos;
	private ImmutableMap<EffType, Integer> effectmap;
	
	private List<Integer> armours;

	public GhostTowerCfg() {
		this.id = 0;
		level = 0;
		floor = 0;
		buildcfgId = 0;
		productTime = 600;
		productMaxCount = 10;
		production = "";
		randomProduction = 0;
		killReward = "";
		areaRadius = "";
		ghostLiftTime = 300;
		effectList = "";
		superSoldierLevel = 0;
		this.heroList = "";
		this.trapNumSec = "";
		this.trapList = "";
		this.soldierNumSec = "";
		this.soldierList = "";
		superSoldier = "";
		armourList = "";
		marchTime = 0;
	}

	/** 获取随机陷阱数量
	 * 
	 * @return */
	public int getRandTrapNum() {
		String[] minMax = trapNumSec.split("_");
		return HawkRand.randInt(Integer.parseInt(minMax[0]), Integer.parseInt(minMax[1]));
	}

	/** 获取随机陷阱数量
	 * 
	 * @return */
	public int getRandSoldierNum() {
		String[] minMax = soldierNumSec.split("_");
		return HawkRand.randInt(Integer.parseInt(minMax[0]), Integer.parseInt(minMax[1]));
	}

	/** 获取陷阱信息
	 * 
	 * @return */
	public Map<Integer, Integer> getRandTrapInfo() {
		Map<Integer, Integer> trapInfo = new HashMap<Integer, Integer>();
		int totalWeight = 0;
		// 首先算出命中ID
		for (TowerArmyRandInfo foggyRandInfo : foggyTrapRandInfos) {
			if (HawkRand.randPercentRate(foggyRandInfo.hitRate)) {
				// 随机权重
				int weight = HawkRand.randInt(foggyRandInfo.minWeight, foggyRandInfo.maxWeight);
				totalWeight += weight;
				// 这里先放入权重，方便后面计算
				trapInfo.put(foggyRandInfo.id, weight);
			}
		}
		// 判断空
		if (trapInfo.isEmpty()) {
			// 重新随机一个
			TowerArmyRandInfo info = HawkRand.randomObject(foggyTrapRandInfos);
			int num = getRandTrapNum();
			if (num > 0) {
				trapInfo.put(info.id, num);
			}
		} else {
			// 计算数量
			long totalNum = getRandTrapNum();
			Iterator<Entry<Integer, Integer>> it = trapInfo.entrySet().iterator();
			while (it.hasNext()) {
				Entry<Integer, Integer> entry = it.next();
				Long num = totalNum * entry.getValue() / totalWeight;
				if (num > 0) {
					trapInfo.put(entry.getKey(), num.intValue());
				} else {
					it.remove();
				}
			}
		}
		return trapInfo;
	}

	/** 获取士兵信息
	 * 
	 * @return */
	public Map<Integer, Integer> getRandSoldierInfo() {
		Map<Integer, Integer> soldierInfo = new HashMap<Integer, Integer>();
		int totalWeight = 0;
		// 首先算出命中ID
		for (TowerArmyRandInfo foggyRandInfo : foggySoldierRandInfos) {
			if (HawkRand.randPercentRate(foggyRandInfo.hitRate)) {
				// 随机权重
				int weight = HawkRand.randInt(foggyRandInfo.minWeight, foggyRandInfo.maxWeight);
				totalWeight += weight;
				// 这里先放入权重，方便后面计算
				soldierInfo.put(foggyRandInfo.id, weight);
			}
		}
		// 判断空
		if (soldierInfo.isEmpty()) {
			// 重新随机一个
			TowerArmyRandInfo info = HawkRand.randomObject(foggySoldierRandInfos);
			int num = getRandSoldierNum();
			if (num > 0) {
				soldierInfo.put(info.id, num);
			}
		} else {
			// 计算数量
			long totalNum = getRandSoldierNum();
			Iterator<Entry<Integer, Integer>> it = soldierInfo.entrySet().iterator();
			while (it.hasNext()) {
				Entry<Integer, Integer> entry = it.next();
				Long num = totalNum * entry.getValue() / totalWeight;
				if (num > 0) {
					soldierInfo.put(entry.getKey(), num.intValue());
				} else {
					it.remove();
				}
			}
		}
		return soldierInfo;
	}

	public HawkTuple5<Boolean, Boolean, Boolean, Boolean, Boolean> randA_B_C_D_E(String a_b_c_d_e) {
		String[] x_y = a_b_c_d_e.split("_");
		int[] arr = new int[5];
		int sum = 0;
		for (int i = 0; i < x_y.length; i++) {
			arr[i] = NumberUtils.toInt(x_y[i]);
			sum += arr[i];
		}
		int rand = RandomUtils.nextInt(sum);
		for (int i = 0; i < arr.length; i++) {
			rand -= arr[i];
			if (rand <= 0) {
				arr[i] = -1;
				break;
			}
		}

		return HawkTuples.tuple(arr[0] < 0, arr[1] < 0, arr[2] < 0, arr[3] < 0, arr[4] < 0);
	}

	public int randNum(String a_b) {
		String[] x_y = a_b.split("_");
		int[] arr = new int[5];
		for (int i = 0; i < x_y.length; i++) {
			arr[i] = NumberUtils.toInt(x_y[i]);
		}
		return HawkRand.randInt(arr[0], arr[1]);
	}

	@Override
	protected boolean assemble() {
		if(!HawkOSOperator.isEmptyString(superSoldier)){
			String[] array = superSoldier.split("_");
			superSoldierId = Integer.parseInt(array[0]);
			superSoldierLevel = Integer.parseInt(array[1]);
		}
		if(!HawkOSOperator.isEmptyString(areaRadius)){
			String[] array = areaRadius.split("_");
			areaRadiusMin = Integer.parseInt(array[0]);
			areaRadiusMax = Integer.parseInt(array[1]);
		}
		
		heroIds = new ArrayList<Integer>();
		if (!HawkOSOperator.isEmptyString(heroList)) {
			String[] array = heroList.split("\\|");
			for (String val : array) {
				heroIds.add(NumberUtils.toInt(val));
			}
		}

		foggyTrapRandInfos = new ArrayList<TowerArmyRandInfo>();
		if (!HawkOSOperator.isEmptyString(trapList)) {
			String[] list = trapList.split("\\|");
			for (String val : list) {
				String[] info = val.split("_");
				TowerArmyRandInfo fInfo = new TowerArmyRandInfo();
				fInfo.id = Integer.parseInt(info[0]);
				fInfo.hitRate = Integer.parseInt(info[1]);
				fInfo.minWeight = Integer.parseInt(info[2]);
				fInfo.maxWeight = Integer.parseInt(info[3]);
				foggyTrapRandInfos.add(fInfo);
			}
		}

		foggySoldierRandInfos = new ArrayList<TowerArmyRandInfo>();
		if (!HawkOSOperator.isEmptyString(soldierList)) {
			String[] list = soldierList.split("\\|");
			for (String val : list) {
				String[] info = val.split("_");
				TowerArmyRandInfo fInfo = new TowerArmyRandInfo();
				fInfo.id = Integer.parseInt(info[0]);
				fInfo.hitRate = Integer.parseInt(info[1]);
				fInfo.minWeight = Integer.parseInt(info[2]);
				fInfo.maxWeight = Integer.parseInt(info[3]);
				foggySoldierRandInfos.add(fInfo);
			}
		}

		{
			armours = new ArrayList<Integer>();
			List<String> list = Splitter.on(",").omitEmptyStrings().splitToList(armourList);
			for (String armour : list) {
				armours.add(NumberUtils.toInt(armour));
			}
		}
		
		{
			Map<EffType, Integer> map = new HashMap<>();
			List<String> effkv = Splitter.on("|").omitEmptyStrings().splitToList(getEffectList());
			for (String kv : effkv) {
				String[] kvArr = kv.split("_");
				EffType key = EffType.valueOf(NumberUtils.toInt(kvArr[0]));
				int val = NumberUtils.toInt(kvArr[1]);
				map.put(key, val);
			}
			effectmap = ImmutableMap.copyOf(map);
		}
		return true;
	}

	
	@Override
	protected boolean checkValid() {
		if (!HawkOSOperator.isEmptyString(heroList)) {
			for (Integer heroId : heroIds) {
				FoggyHeroCfg cfg = HawkConfigManager.getInstance().getConfigByKey(FoggyHeroCfg.class, heroId);
				if (cfg == null) {
					return false;
				}
			}
		}

		if (!HawkOSOperator.isEmptyString(trapList)) {
			for (TowerArmyRandInfo info : foggyTrapRandInfos) {
				BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, info.id);
				if (cfg == null) {
					return false;
				}
			}
		}

		if (!HawkOSOperator.isEmptyString(soldierList)) {
			for (TowerArmyRandInfo info : foggySoldierRandInfos) {
				BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, info.id);
				if (cfg == null) {
					return false;
				}
			}
		}
		return super.checkValid();
	}

	
	
	/** 随机信息类 */
	public static class TowerArmyRandInfo {

		public int id;

		public int hitRate;

		public int minWeight;

		public int maxWeight;
	}

	

	public List<TowerArmyRandInfo> getFoggyTrapRandInfos() {
		return foggyTrapRandInfos;
	}

	public void setFoggyTrapRandInfos(List<TowerArmyRandInfo> foggyTrapRandInfos) {
		this.foggyTrapRandInfos = foggyTrapRandInfos;
	}

	public List<TowerArmyRandInfo> getFoggySoldierRandInfos() {
		return foggySoldierRandInfos;
	}

	public void setFoggySoldierRandInfos(List<TowerArmyRandInfo> foggySoldierRandInfos) {
		this.foggySoldierRandInfos = foggySoldierRandInfos;
	}

	public void setHeroIds(List<Integer> heroIds) {
		this.heroIds = heroIds;
	}


	public int getId() {
		return id;
	}
	
	

	public int getBuildcfgId() {
		return buildcfgId;
	}

	public String getHeroList() {
		return heroList;
	}

	public String getTrapNumSec() {
		return trapNumSec;
	}

	public String getTrapList() {
		return trapList;
	}

	public String getSoldierNumSec() {
		return soldierNumSec;
	}

	public String getSoldierList() {
		return soldierList;
	}

	public List<Integer> getHeroIds() {
		return new ArrayList<>(heroIds);
	}
	
	public List<Integer> getArmourIds() {
		return new ArrayList<>(armours);
	}

	public int getProductTime() {
		return productTime;
	}

	public int getProductMaxCount() {
		return productMaxCount;
	}

	public String getProduction() {
		return production;
	}

	public int getRandomProduction() {
		return randomProduction;
	}

	public int getGhostLiftTime() {
		return ghostLiftTime;
	}

	public String getKillReward() {
		return killReward;
	}

	

	public int getAreaRadiusMax() {
		return areaRadiusMax;
	}

	public int getAreaRadiusMin() {
		return areaRadiusMin;
	}

	public String getSuperSoldier() {
		return superSoldier;
	}

	

	public String getEffectList() {
		return effectList;
	}

	public int getFloor() {
		return floor;
	}

	public int getLevel() {
		return level;
	}

	public int getSuperSoldierId() {
		return superSoldierId;
	}

	
	public int getSuperSoldierLevel() {
		return superSoldierLevel;
	}

	public ImmutableMap<EffType, Integer> getEffectmap() {
		return effectmap;
	}

	public void setEffectmap(ImmutableMap<EffType, Integer> effectmap) {
		this.effectmap = effectmap;
	}

	public int getMarchTime() {
		return marchTime;
	}
	
	
	
	
	

	
}
