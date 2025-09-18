package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 国王战礼包基础配置
 *
 * @author Nannan.Gao
 * @date 2016-10-11 12:08:56
 */
@HawkConfigManager.XmlResource(file = "xml/president_gift.xml")
public class PresidentGiftCfg extends HawkConfigBase {
	@Id
	protected final int id;
	/**
	 * 礼包名称
	 */
	protected final String giftName;
	/**
	 * 礼包发放个数
	 */
	protected final int totalNumber;
	/**
	 * 礼包配置
	 */
	protected final int awardId;

	public PresidentGiftCfg() {
		id = 0;
		giftName = "";
		totalNumber = 0;
		awardId = 0;
	}
	
	public int getId() {
		return id;
	}

	public String getGiftName() {
		return giftName;
	}

	public int getAwardId() {
		return awardId;
	}

	public int getTotalNumber() {
		return totalNumber;
	}

	@Override
	protected boolean assemble() {
		return true;
	}
	
	@Override
	protected boolean checkValid() {
		boolean awardIdCheckResult = AwardCfg.isExistAwardId(awardId);
		boolean numCheckResult = totalNumber > 0;
		return awardIdCheckResult && numCheckResult;
	}
}
