package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 *
 * @author zhenyu.shang
 * @since 2017年11月13日
 */
@HawkConfigManager.XmlResource(file = "xml/mailCombatReminder.xml")
public class MailCombatReminderCfg extends HawkConfigBase {
	
	/** 哨塔Id */
	@Id
	protected final int id;
	
	/** 默认名称 */
	protected final String dictionary;
	
	/** 联盟人数限制 */
	protected final int defaultWeight;
	
	/** 联盟战力限制 */
	protected final int resourceWeight;
	
	/** 联盟科技限制 */
	protected final int baseWeight;
	
	public MailCombatReminderCfg() {
		this.id = 0;
		this.dictionary = "";
		this.defaultWeight = 0;
		this.resourceWeight = 0;
		this.baseWeight = 0;
	}

	public int getId() {
		return id;
	}

	public String getDictionary() {
		return dictionary;
	}

	public int getDefaultWeight() {
		return defaultWeight;
	}

	public int getResourceWeight() {
		return resourceWeight;
	}

	public int getBaseWeight() {
		return baseWeight;
	}
}
