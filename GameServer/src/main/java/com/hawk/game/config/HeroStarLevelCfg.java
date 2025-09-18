package com.hawk.game.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;

@HawkConfigManager.XmlResource(file = "xml/hero_star_level.xml")
@HawkConfigBase.CombineId(fields = { "heroId", "starLevel", "stepLevel" })
public class HeroStarLevelCfg extends HawkConfigBase {
	protected final int id;// ="100110"
	protected final int heroId;// ="11"
	protected final int starLevel;// ="1"
	protected final int stepLevel;// ="0"
	protected final int maxLevel;// ="40"
	protected final String piecesForNextLevel;// ="30000_1000011_10"
	protected final double starPower;// ="50"
	protected final String starAttrValue;// ="101_0|102_0|103_0|104_0"
	protected final int maxSkillLevel;// ="10"
	private ImmutableMap<Integer, Double> starAttrMap;

	public HeroStarLevelCfg() {
		this.id = 0;
		this.heroId = 1;
		this.starLevel = 1;
		this.stepLevel = 1;
		this.maxLevel = 1;
		this.piecesForNextLevel = "";
		this.starPower = 1;
		this.starAttrValue = "";
		this.maxSkillLevel = 0;
	}

	@Override
	protected boolean assemble() {
		{
			Map<Integer, Double> result = new HashMap<>();

			List<String> attrs = Splitter.on("|").omitEmptyStrings().splitToList(starAttrValue);
			for (String str : attrs) {
				String[] arr = Splitter.on("_").omitEmptyStrings().splitToList(str).toArray(new String[2]);
				result.put(NumberUtils.toInt(arr[0]), NumberUtils.toDouble(arr[1]));
			}
			starAttrMap = ImmutableMap.copyOf(result);
		}

		return super.assemble();
	}

	public int getHeroId() {
		return heroId;
	}

	public int getStarLevel() {
		return starLevel;
	}

	public int getMaxLevel() {
		return maxLevel;
	}

	public String getPiecesForNextLevel() {
		return piecesForNextLevel;
	}

	public double getStarPower() {
		return starPower;
	}

	public String getStarAttrValue() {
		return starAttrValue;
	}

	public ImmutableMap<Integer, Double> getStarAttrMap() {
		return starAttrMap;
	}

	public void setStarAttrMap(ImmutableMap<Integer, Double> starAttrMap) {
		throw new UnsupportedOperationException();
	}

	public int getMaxSkillLevel() {
		return maxSkillLevel;
	}

	public int getId() {
		return id;
	}

	public int getStepLevel() {
		return stepLevel;
	}

}
