package com.hawk.activity.type.impl.order.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;


/**
 * 权限提升配置
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "activity/order/order_authority.xml")
public class OrderAuthorityCfg extends HawkConfigBase {
	/** 直购礼包id */
	@Id
	private final int id;
	
	/** 经验值 */
	private final int exp;

	public OrderAuthorityCfg() {
		id = 0;
		exp = 0;
	}

	public int getId() {
		return id;
	}

	public int getExp() {
		return exp;
	}

}
