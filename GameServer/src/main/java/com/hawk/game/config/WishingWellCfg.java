package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 许愿池等级配置
 * @author PhilChen
 *
 */
@HawkConfigManager.XmlResource(file = "xml/wishing_well.xml")
public class WishingWellCfg extends HawkConfigBase {
	/** 许愿池等级*/
	@Id
	protected final int level;
	/** 每日免费次数*/
	protected final int freeCount;
	/** 每日最大次数*/
	protected final int maxCount;
	/** 兑换石油所需消耗*/
	protected final int oilCost;
	/** 石油数量*/
	protected final int oil;
	/** 兑换矿产所需消耗*/
	protected final int goldoreCost;
	/** 矿产数量*/
	protected final int goldore;
	/** 兑换钢铁所需消耗*/
	protected final int steelCost;
	/** 钢铁数量*/
	protected final int steel;
	/** 兑换合金所需消耗*/
	protected final int tombarthiteCost;
	/** 合金数量*/
	protected final int tombarthite;
	
	public WishingWellCfg() {
		this.level = 0;
		this.freeCount = 0;
		this.maxCount = 0;
		this.oilCost = 0;
		this.oil = 0;
		this.goldoreCost = 0;
		this.goldore = 0;
		this.steelCost = 0;
		this.steel = 0;
		this.tombarthiteCost = 0;
		this.tombarthite = 0;
	}
	
	@Override
	protected boolean assemble() {
		return true;
	}

	public int getLevel() {
		return level;
	}

	public int getFreeCount() {
		return freeCount;
	}

	public int getMaxCount() {
		return maxCount;
	}

	public int getOil() {
		return oil;
	}

	public int getGoldore() {
		return goldore;
	}

	public int getSteel() {
		return steel;
	}

	public int getTombarthite() {
		return tombarthite;
	}

	public int getOilCost() {
		return oilCost;
	}

	public int getGoldoreCost() {
		return goldoreCost;
	}

	public int getSteelCost() {
		return steelCost;
	}

	public int getTombarthiteCost() {
		return tombarthiteCost;
	}
	
}
