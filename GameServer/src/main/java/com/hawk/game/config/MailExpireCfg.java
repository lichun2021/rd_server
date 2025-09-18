package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "xml/mail_expire.xml")
public class MailExpireCfg extends HawkConfigBase {
	@Id
	private final int NewPageType;// ="0"
	private final int maxCount;
	private final int expireDay;

	public MailExpireCfg() {
		NewPageType = 0;
		maxCount = 500;
		expireDay = 7;
	}

	public int getNewPageType() {
		return NewPageType;
	}

	public int getMaxCount() {
		return maxCount;
	}

	public int getExpireDay() {
		return expireDay;
	}

}
