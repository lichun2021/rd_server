package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "xml/mailsys.xml")
public class MailsysCfg extends HawkConfigBase {
	@Id
	private final int id;// ="2010001"
	private final int NewPageType;// ="0"
	private final String PushNews;

	public MailsysCfg() {
		id = 0;
		NewPageType = 0;
		PushNews = null;
	}

	public int getId() {
		return id;
	}

	public int getNewPageType() {
		return NewPageType;
	}

	public String getPushNews() {
		return PushNews;
	}

}
