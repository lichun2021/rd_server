package com.hawk.activity.type.impl.treasury.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "activity/treasury/%s/activity_treasury.xml", autoLoad=false, loadParams="44")
public class TreasuryCfg extends HawkConfigBase {
	/**
	 * 主键Id
	 */
	@Id
	private final int id;
	/**
	 * 存储时间,开始和结束都是在同一天
	 */
	private final int storageTime;
	/**
	 * 存储上限
	 */
	private final int storageMax;
	/**
	 * 可以领取的时间
	 */
	private final int receiveTime;
	
	public TreasuryCfg() {
		this.id = 0;
		this.storageTime = 0;
		this.storageMax = 0;
		this.receiveTime = 0;
	}
	public int getId() {
		return id;
	}
	public int getStorageTime() {
		return storageTime;
	}
	public int getStorageMax() {
		return storageMax;
	}
	public int getReceiveTime() {
		return receiveTime;
	}
	
}
