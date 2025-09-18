package com.hawk.activity.type.impl.immgration.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "activity/migrant/migrant_item_use.xml")
public class ImmgrationItemUseCfg extends HawkConfigBase {
	private final int id;

	/**
	 * 排名
	 */
	@Id
	private final int rank;
	
	/**
	 * 本服名次道具消耗
	 */
	private final int localNumber;
	
	/**
	 * 目标服名次道具消耗
	 */
	private final int targetNumber;
	
	public ImmgrationItemUseCfg() {
		id = 0;
		rank = 0;
		localNumber = 0;
		targetNumber = 0;
	}

	public int getId() {
		return id;
	}

	public int getRank() {
		return rank;
	}

	public int getLocalNumber() {
		return localNumber;
	}

	public int getTargetNumber() {
		return targetNumber;
	}
}
