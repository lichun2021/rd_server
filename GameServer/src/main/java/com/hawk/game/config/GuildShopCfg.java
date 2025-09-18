package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 联盟商城配置
 *
 * @author david
 *
 */
@HawkConfigManager.XmlResource(file = "xml/alliance_store.xml")
public class GuildShopCfg extends HawkConfigBase implements Comparable<GuildShopCfg>{
	@Id
	protected final int id;

	// 价格
	protected final int price;
	
	// 排序参数 从小到大排序
	protected final int priority;

	public GuildShopCfg() {
		id = 0;
		price = 0;
		priority = 0;
	}

	public int getId() {
		return id;
	}

	public int getPrice() {
		return price;
	}
	
	public int getPriority() {
		return priority;
	}

	@Override
	protected boolean checkValid() {
		ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, this.id);
		if (itemCfg == null) {
			logger.error("alliance_store.xml error, itemId: {} not exist", this.id);
			return false;
		}
		return true;
	}

	@Override
	protected boolean assemble() {
		return true;
	}

	@Override
	public int compareTo(GuildShopCfg arg0) {
		return this.priority - arg0.getPriority();
	}
}
