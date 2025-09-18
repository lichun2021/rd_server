package com.hawk.game.module.nationMilitary.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 国家军衔等级配置
 * @author lating
 * @since 2022年4月24日
 */
@HawkConfigManager.XmlResource(file = "xml/nation_military.xml")
public class NationMilitaryCfg extends HawkConfigBase {

	@Id
	protected final int level;

	protected final int type;

	protected final int merit;
	protected final int num;
	protected final int advanced;
	protected final int resetMerit;
	protected final String dailyReward;
	protected final String resetReward;

	public static int defaultLevel = 101;

	public NationMilitaryCfg() {
		this.level = 101;
		this.type = 0;
		this.merit = 0;
		advanced = 0;
		num = 0;
		resetMerit = 0;
		dailyReward = "";
		resetReward = "";
	}

	public int getLevel() {
		return level;
	}

	public int getMerit() {
		return merit;
	}

	public int getType() {
		return type;
	}

	@Override
	protected boolean assemble() {
		if (merit == 0) {
			defaultLevel = level;
		}
		return true;
	}

	public int getAdvanced() {
		return advanced;
	}

	public static int getDefaultLevel() {
		return defaultLevel;
	}

	public int getNum() {
		return num;
	}

	public int getResetMerit() {
		return resetMerit;
	}

	public String getDailyReward() {
		return dailyReward;
	}

	public static void setDefaultLevel(int defaultLevel) {
		NationMilitaryCfg.defaultLevel = defaultLevel;
	}

	public String getResetReward() {
		return resetReward;
	}

}
