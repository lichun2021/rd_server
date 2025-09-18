package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "xml/res_gift_group.xml")
public class ResGiftGroupCfg extends HawkConfigBase {
	/**
	 *资源类型标示
	 */
	@Id
	private final int resType;

	/**
	 *是否出售
	 */
	private final int onSale;

	/**
	 *礼包类型重置时间
	 */
	private final int resetTime;

	public ResGiftGroupCfg() {
		this.resType = 0;
		this.resetTime = 0;
		this.onSale = 0;
	}

	public int getResType() {
		return resType;
	}

	public int getOnSale() {
		return onSale;
	}

	public int getResetTime() {
		return resetTime;
	}

}