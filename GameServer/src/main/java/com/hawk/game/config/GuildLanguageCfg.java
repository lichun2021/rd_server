package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 联盟语言配置
 * 
 * @author shadow
 *
 */
@HawkConfigManager.XmlResource(file = "xml/alliance_language.xml")
public class GuildLanguageCfg extends HawkConfigBase {
	@Id
	protected final String id;

	protected final String language_name;

	protected final int order;

	public GuildLanguageCfg() {
		this.id = null;
		this.language_name = null;
		order = 0;
	}

	public String getId() {
		return id;
	}

	public String getLanguage_name() {
		return language_name;
	}
}
