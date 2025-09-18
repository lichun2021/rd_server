package com.hawk.activity.type.impl.plantweaponback.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.KVResource(file = "activity/plant_weapon_back/plant_weapon_back_const.xml")
public class PlantWeaponBackKVCfg extends HawkConfigBase {
	/**
	 * 服务器开服延时开启活动时间；单位：秒
	 */
	private final long serverDelay;
	/**
	 * 抽奖代币，三段式
	 */
	private final String luckToken;
	/**
	 * 商店代币，三段式
	 */
	private final String shopToken;
	/**
	 * 每日免费次数
	 */
	private final int dailyFreeTimes;
	/**
	 * 投放超武id
	 */
	private final int plantWeaponId;
	/**
	 * 代币单抽消耗，三段式
	 */
	private final String extractExpend;
	/**
	 * 转换超武碎片数量，三段式
	 */
	private final String plantWeaponValue;
	/**
	 * 转换商店代币碎片数量，三段式
	 */
	private final String shopTokenValue;
	/**
	 * 购买附赠城建礼包，三段式
	 */
	private final String buyGiveItem;
	/**
	 * 抽奖上限次数
	 */
	private final int maxLotteryTimes;
	/**
	 * 单抽价值金条
	 */
	private final String extractNeedGoldExpend;
	
	public PlantWeaponBackKVCfg(){
		this.serverDelay = 0;
		this.luckToken = "";
		this.shopToken = "";
		this.dailyFreeTimes = 0;
		this.plantWeaponId = 0;
		this.extractExpend = "";
		this.plantWeaponValue = "";
		this.shopTokenValue = "";
		this.buyGiveItem = "";
		this.maxLotteryTimes = 0;
		this.extractNeedGoldExpend = "";
	}
	
	public boolean assemble() {
		return true;
	}

	public long getServerDelay() {
		return serverDelay * 1000L;
	}

	public String getLuckToken() {
		return luckToken;
	}

	public String getShopToken() {
		return shopToken;
	}

	public int getDailyFreeTimes() {
		return dailyFreeTimes;
	}

	public int getPlantWeaponId() {
		return plantWeaponId;
	}

	public String getExtractExpend() {
		return extractExpend;
	}

	public String getPlantWeaponValue() {
		return plantWeaponValue;
	}

	public String getShopTokenValue() {
		return shopTokenValue;
	}

	public String getBuyGiveItem() {
		return buyGiveItem;
	}

	public int getMaxLotteryTimes() {
		return maxLotteryTimes;
	}

	public String getExtractNeedGoldExpend() {
		return extractNeedGoldExpend;
	}

}
