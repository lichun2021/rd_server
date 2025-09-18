package com.hawk.game.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.google.common.base.Splitter;
import com.hawk.game.march.ArmyInfo;

/**
 * 世界地图怪物刷新配置
 * 
 * @author julia
 *
 */
@HawkConfigManager.XmlResource(file = "xml/world_strongpoint.xml")
public class WorldStrongpointCfg extends HawkConfigBase {
	
	@Id
	protected final int id;
	// 等级
	protected final int level;
	// 单次tick的时间(s)
	protected final int tickTime;
	// tick的次数
	protected final int tickNum;
	// 击杀奖励
	protected final String killAward;
	// 固定奖励
	protected final String fixedAward;
	// 随机奖励
	protected final String randomAward;
	// 生命周期
	protected final int lifeTime;
	// 体力消耗
	protected final int strongpointCost;
	// 怪物
	protected final String monster;
	/**
	 * 英雄
	 */
	protected final String heroList;
	
	private List<Integer> killAwards;
	private List<Integer> fixedAwards;
	private List<Integer> randomAwards;
	// 据点部队列表
	private List<ArmyInfo> armyList;
	/**
	 * 英雄id列表
	 */
	private List<Integer> heroIdList;
	
	public WorldStrongpointCfg() {
		id = 0;
		level = 0;
		tickTime = 0;
		tickNum = 0;
		killAward = "";
		fixedAward = "";
		randomAward = "";
		strongpointCost = 0;
		monster = "";
		lifeTime = 0;
		heroList = "";
	}

	public int getId() {
		return id;
	}

	public int getLevel() {
		return level;
	}

	public int getTickTime() {
		return tickTime;
	}

	public int getTickNum() {
		return tickNum;
	}

	public int getStrongpointCost() {
		return strongpointCost;
	}

	public String getMonster() {
		return monster;
	}
	
	public int getLifeTime() {
		return lifeTime;
	}

	public List<Integer> getKillAwards() {
		return new ArrayList<>(killAwards);
	}

	public List<Integer> getFixedAwards() {
		return new ArrayList<>(fixedAwards);
	}

	public List<Integer> getRandomAwards() {
		return new ArrayList<>(randomAwards);
	}
	
	public List<Integer> getHeroIdList() {
		return new ArrayList<>(heroIdList);
	}
	
	public List<ArmyInfo> getArmyList() {
		List<ArmyInfo> copy = new ArrayList<>();
		armyList.forEach(e -> copy.add(e.getCopy()));
		return copy;
	}

	@Override
	protected boolean assemble() {
		List<Integer> killAawards = new ArrayList<Integer>();
		Arrays.asList(killAward.split(";")).forEach(award -> {
			killAawards.add(Integer.parseInt(award));
		});
		this.killAwards = killAawards;
		
		List<Integer> fixedAwards = new ArrayList<Integer>();
		Arrays.asList(fixedAward.split(";")).forEach(award -> {
			fixedAwards.add(Integer.parseInt(award));
		});
		this.fixedAwards = fixedAwards;
		
		List<Integer> randomAwards = new ArrayList<Integer>();
		Arrays.asList(randomAward.split(";")).forEach(award -> {
			randomAwards.add(Integer.parseInt(award));
		});
		this.randomAwards = randomAwards;
		
		armyList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(monster)) {
			for (String army : Splitter.on("|").split(monster)) {
				String[] armyStrs = army.split("_");
				armyList.add(new ArmyInfo(Integer.parseInt(armyStrs[0]), Integer.parseInt(armyStrs[1])));
			}
		}
		
		List<Integer> heroIdList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(heroList)) {
			String[] heros = heroList.split("\\|");
			for (int i = 0; i < heros.length; i++) {
				heroIdList.add(Integer.valueOf(heros[i]));
			}
		}
		this.heroIdList = heroIdList;
		return true;
	}
}
