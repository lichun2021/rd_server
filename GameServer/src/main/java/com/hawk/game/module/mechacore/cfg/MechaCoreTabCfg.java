package com.hawk.game.module.mechacore.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "xml/mecha_core_tab.xml")
public class MechaCoreTabCfg extends HawkConfigBase {

	@Id
	protected final int id;

	/**
	 * 解锁槽位消耗
	 */
	protected final String unlockTabNeedItem;
	
	public MechaCoreTabCfg() {
		id = 0;
		unlockTabNeedItem = "";
	}

	public int getId() {
		return id;
	}

	public String getUnlockTabNeedItem() {
		return unlockTabNeedItem;
	}

}
