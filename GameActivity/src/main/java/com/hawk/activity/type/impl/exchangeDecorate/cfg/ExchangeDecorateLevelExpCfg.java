package com.hawk.activity.type.impl.exchangeDecorate.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;

@HawkConfigManager.XmlResource(file = "activity/freeavatar/freeavatar_level_cfg.xml")
public class ExchangeDecorateLevelExpCfg extends HawkConfigBase {
	/**
	 * 最大等级
	 */
	private static int maxLevel = 0;
	/**
	 * 等级
	 */
	@Id
	protected final int level;
	/**
	 * 所需经验
	 */
	protected final int levelUpExp;
	/**
	 * 等级奖励
	 */
	protected final String normalReward;

	private List<RewardItem.Builder> normalItemList;

	public ExchangeDecorateLevelExpCfg() {
		this.level = 0;
		this.levelUpExp = 0;
		this.normalReward = "";
	}


	public List<RewardItem.Builder> getNormalItemList() {
		return normalItemList;
	}


	public void setNormalItemList(List<RewardItem.Builder> normalItemList) {
		this.normalItemList = normalItemList;
	}


	public int getLevel() {
		return level;
	}

	public int getLevelUpExp() {
		return levelUpExp;
	}


	public String getNormalReward() {
		return normalReward;
	}

	/**
	 * 获取配置的最大等级
	 *
	 * @return
	 */
	public static int getMaxLevel() {
		return maxLevel;
	}

	@Override
	protected boolean assemble() {
		if (maxLevel < level) {
			maxLevel = level;
		}
		normalItemList = RewardHelper.toRewardItemImmutableList(normalReward);
		return true;
	}

	@Override
	protected boolean checkValid() {
		return ConfigChecker.getDefaultChecker().checkAwardsValid(normalReward);
	}
}