package com.hawk.game.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "xml/world_treasure_hunt_res.xml")
public class TreasureHuntResCfg extends HawkConfigBase {
	
	@Id
	protected final int id;
	
	protected final int level;
	// 单次tick的时间(s)
	protected final int tickTime;
	// tick的次数
	protected final int tickNum;
	// 固定奖励
	protected final String fixedAward;
	// 随机奖励
	protected final String randomAward;
	// 生命周期
	protected final int lifeTime;
	// 体力消耗
	protected final int strongpointCost;
	
	private List<Integer> fixedAwards;
	private List<Integer> randomAwards;
	
	public TreasureHuntResCfg() {
		id = 0;
		tickTime = 0;
		tickNum = 0;
		fixedAward = "";
		randomAward = "";
		strongpointCost = 0;
		lifeTime = 0;
		level = 0;
	}

	public int getId() {
		return id;
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

	public int getLifeTime() {
		return lifeTime;
	}

	public List<Integer> getFixedAwards() {
		return new ArrayList<>(fixedAwards);
	}

	public List<Integer> getRandomAwards() {
		return new ArrayList<>(randomAwards);
	}
	
	@Override
	protected boolean assemble() {
		
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
		return true;
	}
}
