package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "xml/star_wars_gift.xml")
public class StarWarsGiftCfg extends HawkConfigBase {
	@Id
	protected final int id;
	/**
	 * 礼包发放个数
	 */
	protected final int totalNumber;
	/**
	 * 礼包配置
	 */
	protected final int awardId;
	/**
	 * 礼包的名字
	 */
	protected final String giftName;
	/**
	 * 第几个阶段的王.
	 */
	private final int level;
	
	public StarWarsGiftCfg() {
		this.id = 0;
		this.totalNumber = 0;
		this.awardId = 0;
		this.giftName = "";
		this.level = 0;
	}
	
	@Override
	protected boolean checkValid() {
		boolean awardIdCheckResult = AwardCfg.isExistAwardId(awardId);
		boolean numCheckResult = totalNumber > 0;
		return awardIdCheckResult && numCheckResult;
	}

	public int getId() {
		return id;
	}

	public int getTotalNumber() {
		return totalNumber;
	}

	public int getAwardId() {
		return awardId;
	}

	public String getGiftName() {
		return giftName;
	}
	
	public int getLevel() {
		return level;
	}
}
