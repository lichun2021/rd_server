package com.hawk.game.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;

/**
 * 迷雾要塞配置表
 * @author zhenyu.shang
 * @since 2018年2月22日
 */
@HawkConfigManager.XmlResource(file = "xml/foggy_fortress.xml")
public class FoggyFortressCfg extends HawkConfigBase {
	
	@Id
	protected final int id;
	
	protected final int level;
	
	protected final String reward;
	
	protected final String heroList;
	
	protected final String trapNumSec;
	
	protected final String trapList;
	
	protected final String soldierNumSec;
	
	protected final String soldierList;
	
	protected final boolean antiSpyBool;
	/**
	 * 生命周期，单位秒
	 */
	protected final int lifeTime;
	
	/** 集结战胜幽灵基地时，给还有奖励次数的玩家发放的奖励 */
	protected final int assembleReward;
	
	/** 集结战胜幽灵基地时，给还有发动集结奖励次数的玩家发放的奖励 */
	protected final int startAssembleReward;
	
	/** 战胜幽灵基地时，集结队伍中存在有奖励次数的玩家发放的联盟礼 */
	protected final int allianceGift;
	
	
	private List<Integer> heroIds;
	
	private List<Integer> heroWeights;
	
	private List<FoggyRandInfo> foggyTrapRandInfos;
	
	private List<FoggyRandInfo> foggySoldierRandInfos;
	
	private List<Integer> awards;
	
	public FoggyFortressCfg() {
		this.id = 0;
		this.level = 0;
		this.reward = "";
		this.heroList = "";
		this.trapNumSec = "";
		this.trapList = "";
		this.soldierNumSec = "";
		this.soldierList = "";
		this.antiSpyBool = false;
		this.lifeTime = 0;
		this.assembleReward = 0;
		this.startAssembleReward = 0;
		this.allianceGift = 0;
	}

	public int getId() {
		return id;
	}

	public int getLevel() {
		return level;
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

	public boolean isAntiSpyBool() {
		return antiSpyBool;
	}
	
	public int getLifeTime() {
		return lifeTime;
	}

	public int getAssembleReward() {
		return assembleReward;
	}
	
	public int getStartAssembleReward() {
		return startAssembleReward;
	}

	public int getAllianceGift() {
		return allianceGift;
	}

	/**
	 * 获取随机英雄ID
	 * @return
	 */
	public List<Integer> getRandHeroId(int count){
		List<Integer> resHeroIds = new ArrayList<Integer>();
		if(heroIds.size() < count){
			count = heroIds.size();
		}
		BitSet bitSet = new BitSet();
		for (int i = 0; i < count; i++) {
			resHeroIds.add(HawkRand.randomWeightObject(heroIds, heroWeights, bitSet));
		}
		return resHeroIds;
	}
	
	/**
	 * 获取随机陷阱数量
	 * @return
	 */
	public int getRandTrapNum(){
		String[] minMax = trapNumSec.split("_");
		return HawkRand.randInt(Integer.parseInt(minMax[0]), Integer.parseInt(minMax[1]));
	}

	/**
	 * 获取随机陷阱数量
	 * @return
	 */
	public int getRandSoldierNum(){
		String[] minMax = soldierNumSec.split("_");
		return HawkRand.randInt(Integer.parseInt(minMax[0]), Integer.parseInt(minMax[1]));
	}
	
	/**
	 * 获取陷阱信息
	 * @return
	 */
	public Map<Integer, Integer> getRandTrapInfo(){
		Map<Integer, Integer> trapInfo = new HashMap<Integer, Integer>();
		int totalWeight = 0;
		//首先算出命中ID
		for (FoggyRandInfo foggyRandInfo : foggyTrapRandInfos) {
			if(HawkRand.randPercentRate(foggyRandInfo.hitRate)){
				//随机权重
				int weight = HawkRand.randInt(foggyRandInfo.minWeight, foggyRandInfo.maxWeight);
				totalWeight += weight;
				//这里先放入权重，方便后面计算
				trapInfo.put(foggyRandInfo.id, weight);
			}
		}
		//判断空
		if(trapInfo.isEmpty()){
			//重新随机一个
			FoggyRandInfo info = HawkRand.randomObject(foggyTrapRandInfos);
			int num = getRandTrapNum();
			if(num > 0){
				trapInfo.put(info.id, num);
			}
		} else {
			//计算数量
			long totalNum = getRandTrapNum();
			Iterator<Entry<Integer, Integer>> it = trapInfo.entrySet().iterator();
			while (it.hasNext()) {
				Entry<Integer, Integer> entry = it.next();
				Long num = totalNum * entry.getValue() / totalWeight;
				if(num > 0){
					trapInfo.put(entry.getKey(), num.intValue());
				} else {
					it.remove();
				}
			}
		}
		return trapInfo;
	}
	
	/**
	 * 获取士兵信息
	 * @return
	 */
	public Map<Integer, Integer> getRandSoldierInfo(){
		Map<Integer, Integer> soldierInfo = new HashMap<Integer, Integer>();
		int totalWeight = 0;
		//首先算出命中ID
		for (FoggyRandInfo foggyRandInfo : foggySoldierRandInfos) {
			if(HawkRand.randPercentRate(foggyRandInfo.hitRate)){
				//随机权重
				int weight = HawkRand.randInt(foggyRandInfo.minWeight, foggyRandInfo.maxWeight);
				totalWeight += weight;
				//这里先放入权重，方便后面计算
				soldierInfo.put(foggyRandInfo.id, weight);
			}
		}
		//判断空
		if(soldierInfo.isEmpty()){
			//重新随机一个
			FoggyRandInfo info = HawkRand.randomObject(foggySoldierRandInfos);
			int num = getRandSoldierNum();
			if(num > 0){
				soldierInfo.put(info.id, num);
			}
		} else {
			//计算数量
			long totalNum = getRandSoldierNum();			
			Iterator<Entry<Integer, Integer>> it = soldierInfo.entrySet().iterator();
			while (it.hasNext()) {
				Entry<Integer, Integer> entry = it.next();
				Long num = totalNum * entry.getValue() / totalWeight;
				if(num > 0){
					soldierInfo.put(entry.getKey(), num.intValue());
				} else {
					it.remove();
				}
			}
		}
		return soldierInfo;
	}
	
	@Override
	protected boolean assemble() {
		heroIds = new ArrayList<Integer>();
		heroWeights = new ArrayList<Integer>();
		if (!HawkOSOperator.isEmptyString(heroList)) {
			String[] array = heroList.split("\\|");
			for (String val : array) {
				String[] info = val.split("_");
				heroIds.add(Integer.parseInt(info[0]));
				heroWeights.add(Integer.parseInt(info[1]));
			}
		}
		
		foggyTrapRandInfos = new ArrayList<FoggyRandInfo>();
		if(!HawkOSOperator.isEmptyString(trapList)){
			String[] list = trapList.split("\\|");
			for (String val : list) {
				String[] info = val.split("_");
				FoggyRandInfo fInfo = new FoggyRandInfo();
				fInfo.id = Integer.parseInt(info[0]);
				fInfo.hitRate = Integer.parseInt(info[1]);
				fInfo.minWeight = Integer.parseInt(info[2]);
				fInfo.maxWeight = Integer.parseInt(info[3]);
				foggyTrapRandInfos.add(fInfo);
			}
		}
		
		foggySoldierRandInfos = new ArrayList<FoggyRandInfo>();
		if(!HawkOSOperator.isEmptyString(soldierList)){
			String[] list = soldierList.split("\\|");
			for (String val : list) {
				String[] info = val.split("_");
				FoggyRandInfo fInfo = new FoggyRandInfo();
				fInfo.id = Integer.parseInt(info[0]);
				fInfo.hitRate = Integer.parseInt(info[1]);
				fInfo.minWeight = Integer.parseInt(info[2]);
				fInfo.maxWeight = Integer.parseInt(info[3]);
				foggySoldierRandInfos.add(fInfo);
			}
		}
		
		
		awards = new ArrayList<Integer>();
		if (!HawkOSOperator.isEmptyString(reward)) {
			Arrays.asList(reward.split(";")).forEach(award -> {
				awards.add(Integer.parseInt(award));
			});
		}
		return true;
	}
	
	@Override
	protected boolean checkValid() {
		if(!HawkOSOperator.isEmptyString(heroList)){
			for (Integer heroId : heroIds) {
				FoggyHeroCfg cfg = HawkConfigManager.getInstance().getConfigByKey(FoggyHeroCfg.class, heroId);
				if(cfg == null){
					return false;
				}
			}
		}
		
		if(!HawkOSOperator.isEmptyString(trapList)){
			for (FoggyRandInfo info : foggyTrapRandInfos) {
				BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, info.id);
				if(cfg == null){
					return false;
				}
			}
		}
		
		if(!HawkOSOperator.isEmptyString(soldierList)){
			for (FoggyRandInfo info : foggySoldierRandInfos) {
				BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, info.id);
				if(cfg == null){
					return false;
				}
			}
		}
		
		for (int awardId : awards) {
			AwardCfg cfg = HawkConfigManager.getInstance().getConfigByKey(AwardCfg.class, awardId);
			if (cfg == null) {
				return false;
			}
		}
		return super.checkValid();
	}
	
	/**
	 * 随机信息类
	 * @author zhenyu.shang
	 * @since 2018年2月22日
	 */
	public static class FoggyRandInfo{
		
		public int id;
		
		public int hitRate;
		
		public int minWeight;
		
		public int maxWeight;
	}

	public List<Integer> getAwards() {
		return awards;
	}
}
