package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "xml/supersoldier_skillexp_exchange.xml")
public class SuperSoldierSkillexpExchangeCfg extends HawkConfigBase {
	@Id
	protected final int id;
	protected final int count;

	public SuperSoldierSkillexpExchangeCfg() {
		this.id = 0;
		this.count = 0;
	}

	public int getId() {
		return id;
	}

	public int getCount() {
		return count;
	}

}
