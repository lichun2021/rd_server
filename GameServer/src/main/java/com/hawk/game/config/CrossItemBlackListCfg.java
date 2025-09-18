package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "xml/cross_item_blackList.xml")
public class CrossItemBlackListCfg extends HawkConfigBase {
	
	protected final int id;
	
	@Id
	protected final int itemID;
	
	
	public CrossItemBlackListCfg() {
		id = 0;
		itemID = 0;
	}

	public int getId() {
		return id;
	}
	
	
	public int getItemID() {
		return itemID;
	}

}
