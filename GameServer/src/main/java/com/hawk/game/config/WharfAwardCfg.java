package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 码头奖励配置
 * @author PhilChen
 *
 */
@HawkConfigManager.XmlResource(file = "xml/wharf_award.xml")
public class WharfAwardCfg extends HawkConfigBase {
	/** id*/
	@Id
	protected final int id;
	/** 最小大本等级*/
	protected final int factoryMinLevel;
	/** 最大大本等级*/
	protected final int factoryMaxLevel;
	/** 奖励刷新最小间隔时间*/
	protected final int awardMinTime;
	/** 奖励刷新最大间隔时间*/
	protected final int awardMaxTime;
	/** 领取顺序*/
	protected final int awardOrder;
	/** 下一个序号*/
	protected final int nextOrder;
	/** 奖励内容*/
	protected final int awardId;
	
	public WharfAwardCfg() {
		this.id = 0;
		this.factoryMinLevel = 0;
		this.factoryMaxLevel = 0;
		this.awardMinTime = 0;
		this.awardMaxTime = 0;
		this.awardOrder = 0;
		this.nextOrder = 0;
		this.awardId = 0;
	}
	
	public int getId() {
		return id;
	}

	public int getFactoryMinLevel() {
		return factoryMinLevel;
	}

	public int getFactoryMaxLevel() {
		return factoryMaxLevel;
	}

	public int getAwardMinTime() {
		return awardMinTime;
	}

	public int getAwardMaxTime() {
		return awardMaxTime;
	}

	public int getAwardOrder() {
		return awardOrder;
	}

	public int getNextOrder() {
		return nextOrder;
	}
	
	public int getAwardId() {
		return awardId;
	}
}
