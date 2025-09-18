package com.hawk.activity.type.impl.order.activityOrderTwo.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;


/**
 * 权限提升配置
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "activity/new_order/new_order_authority.xml")
public class OrderTwoAuthorityCfg extends HawkConfigBase {
	/** 直购礼包id */
	@Id
	private final int id;
	
	/** 经验值 */
	private final int exp;
	
	/** 战令等阶*/
	private final int order;
	
	/** 是否为补差价*/
	private final int supply;

	public OrderTwoAuthorityCfg() {
		id = 0;
		exp = 0;
		order = 0;
		supply = 0;
	}

	public int getId() {
		return id;
	}

	public int getExp() {
		return exp;
	}

	public int getOrder() {
		return order;
	}

	public boolean isSupply() {
		return supply == 1;
	}
	
}
