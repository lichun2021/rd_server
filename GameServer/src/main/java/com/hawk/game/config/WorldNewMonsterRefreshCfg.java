package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;

import com.hawk.game.item.RandomItem;
import com.hawk.game.util.RandomUtil;

/**
 * 区域野怪刷新配置
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/world_newMonster_refresh.xml")
public class WorldNewMonsterRefreshCfg extends HawkConfigBase {
	/**
	 * 区域等级
	 */
	protected final int resAreaLevel;
	
	protected final int openServiceTimeLowerLimit;
	
	protected final int openServiceTimeUpLimit;
	
	/**
	 * 野怪等级权重配置
	 */
	protected final String monsterLevelWeight;
	
	/**
	 * 迷雾要塞等级权重配置
	 */
	protected final String foggyFortressIdWeight;
	
	/**
	 * 野怪随机列表
	 */
	private List<RandomItem> monsterRandom;
	
	/**
	 * 最大可刷新出的新版野怪等级
	 */
	private int maxCanRandomNewMonsterLvl;
	
	private List<Integer> foggyIds = new ArrayList<Integer>();
	private List<Integer> foggyWeights = new ArrayList<Integer>();
	
	public WorldNewMonsterRefreshCfg() {
		resAreaLevel = 0;
		monsterLevelWeight = "";
		foggyFortressIdWeight = "";
		openServiceTimeLowerLimit = 0;
		openServiceTimeUpLimit = 0;
	}

	public int getResAreaLevel() {
		return resAreaLevel;
	}

	public String getMonsterLevelWeight() {
		return monsterLevelWeight;
	}
	
	public int getMonsterRandomLevel() {
		return RandomUtil.random(monsterRandom).getType();
	}
	
	public int getOpenServiceTimeLowerLimit() {
		return openServiceTimeLowerLimit;
	}

	public int getOpenServiceTimeUpLimit() {
		return openServiceTimeUpLimit;
	}

	public String getFoggyFortressIdWeight() {
		return foggyFortressIdWeight;
	}

	public int getMaxCanRandomNewMonsterLvl() {
		return maxCanRandomNewMonsterLvl;
	}

	@Override
	protected boolean assemble() {
		monsterRandom = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(monsterLevelWeight)) {
			String[] split = monsterLevelWeight.split(";");
			for (int i = 0; i < split.length; i++) {
				int level = Integer.parseInt(split[i].split("_")[0]);
				int weight = Integer.parseInt(split[i].split("_")[1]);
				monsterRandom.add(new RandomItem(level, weight));
				
				if (level > maxCanRandomNewMonsterLvl) {
					maxCanRandomNewMonsterLvl = level;
				}
			}
		}
		
		if (!HawkOSOperator.isEmptyString(foggyFortressIdWeight)) {
			String[] items = foggyFortressIdWeight.split(";");
			for (int i = 0; i < items.length; i++) {
				String[] keyWeight = items[i].split("_");
				foggyIds.add(Integer.parseInt(keyWeight[0]));
				foggyWeights.add(Integer.parseInt(keyWeight[1]));
			}
		}
		
		return true;
	}
	
	/**
	 * 获取随机迷雾ID
	 * @return
	 */
	public int getRandFoggyId(){
		return HawkRand.randomWeightObject(foggyIds, foggyWeights);
	}
	
	@Override
	protected boolean checkValid() {
		if (foggyIds != null) {
			for (Integer foggyId : foggyIds) {
				FoggyFortressCfg pointCfg = HawkConfigManager.getInstance().getConfigByKey(FoggyFortressCfg.class, foggyId);
				if (pointCfg == null) {
					return false;
				}
			}
		}
		return super.checkValid();
	}

	public List<Integer> getFoggyIds() {
		return foggyIds;
	}
}
