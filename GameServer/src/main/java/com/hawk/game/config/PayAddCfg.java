package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 累计充值配置
 *
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "xml/pay_add.xml")
public class PayAddCfg extends HawkConfigBase {
	// 累计充值等级
	@Id
	protected final int level;
	// 累计充值数量
	protected final int addGold;
	// 奖励
	protected final int awardItems;

	private static int maxLevel = 0;

	public PayAddCfg() {
		level = 0;
		addGold = 0;
		awardItems = 0;
	}

	public int getLevel() {
		return level;
	}

	public int getAddGold() {
		return addGold;
	}

	public int getAwardItems() {
		return awardItems;
	}

	@Override
	protected boolean assemble() {
		if (level > maxLevel) {
			maxLevel = level;
		}
		return true;
	}

	public static int getMaxLevel() {
		return maxLevel;
	}
}
