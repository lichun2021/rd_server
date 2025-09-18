package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

/**
 * 装扮道具配置
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/world_dress.xml")
public class DressToolCfg extends HawkConfigBase {
	@Id
	protected final int id;
	
	/**
	 * 装扮id
	 */
	protected final int dressId;
	
	/**
	 * 持续时间 0 代表永久
	 */
	protected final int continueTime;

	protected final String exchangeItem;

	protected final int item;
	protected final String costItem;
	
	
	public DressToolCfg() {
		id = 0;
		dressId = 0;
		continueTime = 0;
		exchangeItem = "";
		item = 0;
		costItem = "";
	}


	public int getId() {
		return id;
	}

	public int getDressId() {
		return dressId;
	}

	public int getContinueTime() {
		return continueTime;
	}

	public String getExchangeItem() {
		return exchangeItem;
	}

	public boolean canExchange(){
		return !HawkOSOperator.isEmptyString(exchangeItem);
	}

	public boolean canBuy(){
		return !HawkOSOperator.isEmptyString(costItem);
	}

	public int getItem() {
		return item;
	}

	public String getCostItem() {
		return costItem;
	}
}
