package com.hawk.robot.config;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

/**
 *
 * @author zhenyu.shang
 * @since 2017年7月4日
 */
@HawkConfigManager.XmlResource(file = "xml/guild_science_level.xml")
@HawkConfigBase.CombineId(fields = {"level", "scienceId"})
public class GuildScienceLevelCfg extends HawkConfigBase {
	/** 等级 */
	protected final int level;

	/** 科技Id */
	protected final int scienceId;

	/** 作用号 */
	protected final String effect;

	/** 研究时间 */
	protected final int costTime;

	/** 星级 */
	protected final String scienceVal;

	/** 作用号集合 */
	private List<int[]> effects;

	/** 升星科技值列表 */
	private List<Integer> starValList;

	/** 科研值上限 */
	private int fullDonate;

	public GuildScienceLevelCfg() {
		this.level = 0;
		this.scienceId = 0;
		this.effect = "";
		this.costTime = 0;
		this.scienceVal = "";
	}

	public int getLevel() {
		return level;
	}

	public int getScienceId() {
		return scienceId;
	}

	public String getEffect() {
		return effect;
	}

	public int getCostTime() {
		return costTime;
	}

	public List<int[]> getEffects() {
		return effects;
	}

	public List<Integer> getStarValList() {
		return starValList;
	}

	public int getFullDonate() {
		return fullDonate;
	}

	@Override
	protected boolean assemble() {
		effects = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(effect)) {
			String[] array = effect.split(",");
			for (String val : array) {
				String[] eff = val.split("_");
				if (eff == null || eff.length != 2) {
					logger.error("guild_science_level.xml error, scienceId : {}, level : {} error", scienceId, level);
					return false;
				}
				effects.add(new int[] { Integer.parseInt(eff[0]), Integer.parseInt(eff[1]) });
			}
		}
		fullDonate = 0;
		starValList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(scienceVal)) {
			String[] array = scienceVal.split(";");
			for (String val : array) {
				int intVal = Integer.valueOf(val);
				starValList.add(intVal);
				fullDonate += intVal;
			}
		}
		return true;
	}
}
