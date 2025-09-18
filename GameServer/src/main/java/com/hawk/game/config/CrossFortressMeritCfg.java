package com.hawk.game.config;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.item.ItemInfo;

/**
 * 要塞发奖
 * @author Golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/cross_fortress_merit.xml")
public class CrossFortressMeritCfg extends HawkConfigBase {
	
	@Id
	protected final int id;
	
	protected final String reward;
	
	public CrossFortressMeritCfg() {
		id = 0;
		reward = "";
	}

	public int getId() {
		return id;
	}

	public List<ItemInfo> getRewardList() {
		return ItemInfo.valueListOf(reward);
	}
}
