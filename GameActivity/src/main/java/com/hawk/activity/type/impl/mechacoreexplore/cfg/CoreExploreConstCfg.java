package com.hawk.activity.type.impl.mechacoreexplore.cfg;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRand;

import com.hawk.serialize.string.SerializeHelper;

@HawkConfigManager.KVResource(file = "activity/core_explore/core_explore_base.xml")
public class CoreExploreConstCfg extends HawkConfigBase {
	/**
	 * 服务器开服延时开启活动时间；单位：秒
	 */
	private final long serverDelay;
	/**
	 * 基地等级限制
	 */
	private final int baseLimit;
	/**
	 * VIP等级限制
	 */
	private final int vipLimit;
	/**
	 * 免费矿镐道具ID
	 */
	private final int freePickId;
	/**
	 * 付费矿镐道具ID
	 */
	private final int buyPickId;
	/**
	 * 免费矿镐数量上限
	 */
	private final int pickLimit;
	/**
	 * 每日0时赠与矿镐数量
	 */
	private final int dailyFreePick;
	/**
	 * 赠送矿镐间隔(秒）
	 */
	private final int freePickCd;
	/**
	 * 赠送矿镐数量
	 */
	private final int freePickNum;
	/**
	 * 矿镐购买消耗
	 */
	private final String pickPrice;
	/**
	 * 点击沙土消耗
	 */
	private final String sandCost;
	/**
	 * 点击石头消耗
	 */
	private final String stoneCost;
	/**
	 * 宝箱基础点击次数
	 */
	private final String boxClickTimes;
	/**
	 * 宝箱点击奖励id
	 */
	private final int boxClickAward;
	/**
	 * 每期购买矿镐次数上限
	 */
	private final int buyPickLimit;
	/**
	 * 炸弹道具ID
	 */
	private final int boomItemId;
	/**
	 * 钻机道具ID
	 */
	private final int rigItemId;
	/**
	 * 每期赠送炸弹数量
	 */
	private final int freeBoomNum;
	/** 
	 * 每期赠送钻机数量
	 */
	private final int freeRigNum;
	
	/** 矿石id */
	private final int mineralItemId;
	
	/** 连续*行强行阻断 */
	private final int block;
	
	/** 活动结束需要清除的道具id */
	private final String clearItemIds;
	
	
	private int[] boxClickMinMax = new int[2];
	private List<Integer> clearItemList = new ArrayList<>();
	
	private static CoreExploreConstCfg instance;
	
	public CoreExploreConstCfg(){
		this.serverDelay = 0;
		this.baseLimit = 0;
		this.vipLimit = 0;
		this.pickLimit = 0;
		this.dailyFreePick = 0;
		this.freePickCd = 0;
		this.freePickNum = 0;
		this.pickPrice = "";
		this.sandCost = "";
		this.stoneCost = "";
		this.boxClickTimes = "";
		this.boxClickAward = 0;
		this.buyPickLimit = 0;
		this.boomItemId = 0;
		this.rigItemId = 0;
		this.freePickId = 0;
		this.buyPickId = 0;
		this.freeBoomNum = 0;
		this.freeRigNum = 0;
		this.mineralItemId = 0;
		this.block = 10;
		this.clearItemIds = "";
	}
	
	public boolean assemble() {
		String[] arr = boxClickTimes.split(",");
		boxClickMinMax[0] = Integer.parseInt(arr[0]);
		boxClickMinMax[1] = Integer.parseInt(arr[1]);
		if (boxClickMinMax[0] > boxClickMinMax[1] || boxClickMinMax[0] < 0) {
			return false;
		}
		
		clearItemList = SerializeHelper.stringToList(Integer.class, clearItemIds, ",");
		instance = this;
		return true;
	}
	
	public static CoreExploreConstCfg getInstance() {
		return instance;
	}

	public long getServerDelay() {
		return serverDelay * 1000L;
	}

	public int getBaseLimit() {
		return baseLimit;
	}

	public int getVipLimit() {
		return vipLimit;
	}

	public int getPickLimit() {
		return pickLimit;
	}

	public int getDailyFreePick() {
		return dailyFreePick;
	}

	public int getFreePickCd() {
		return freePickCd;
	}

	public int getFreePickNum() {
		return freePickNum;
	}

	public String getPickPrice() {
		return pickPrice;
	}

	public String getSandCost() {
		return sandCost;
	}

	public String getStoneCost() {
		return stoneCost;
	}

	public String getBoxClickTimes() {
		return boxClickTimes;
	}
	
	public int getBoxClickRandTimes() {
		return HawkRand.randInt(boxClickMinMax[0], boxClickMinMax[1]);
	}

	public int getBoxClickAward() {
		return boxClickAward;
	}

	public int getBuyPickLimit() {
		return buyPickLimit;
	}

	public int getBoomItemId() {
		return boomItemId;
	}

	public int getRigItemId() {
		return rigItemId;
	}
	
	public int getFreePickId() {
		return freePickId;
	}

	public int getBuyPickId() {
		return buyPickId;
	}

	public int getFreeBoomNum() {
		return freeBoomNum;
	}

	public int getFreeRigNum() {
		return freeRigNum;
	}

	public int getMineralItemId() {
		return mineralItemId;
	}

	public int getBlockLineNum() {
		return block;
	}
	
	public List<Integer> getClearItemList() {
		return clearItemList;
	}
}
