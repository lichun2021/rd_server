package com.hawk.activity.type.impl.order.activityNewOrder.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;


/**
 * 权限提升配置
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "activity/order_two/%s/order_two_authority.xml", autoLoad=false, loadParams="211")
public class NewOrderAuthorityCfg extends HawkConfigBase {
	/** 直购礼包id */
	@Id
	private final int id;
	
	/** 经验值 */
	private final int exp;

	public NewOrderAuthorityCfg() {
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
