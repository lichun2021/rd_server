package com.hawk.activity.type.impl.destinyRevolver.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.google.common.base.Splitter;

/**
 * 命运左轮
 * @author golden
 *
 */
@HawkConfigManager.KVResource(file = "activity/destiny_revolver/destiny_revolver_cfg.xml")
public class DestinyRevolverCfg extends HawkConfigBase {

	/**
	 * 服务器开服延时开启活动时间；单位：秒
	 */
	private final int serverDelay;
	
	/**
	 * 倍数道具id
	 */
	private final String multipleItem;
	
	/**
	 * 外层五格转盘消耗
	 */
	private final String fiveCost;
	
	/**
	 * 内层九宫格消耗 首次免费配0
	 */
	private final String nineCost;
	
	/**
	 * 外层五格转盘 道具单价
	 */
	private final String fivePrice;
	
	/**
	 * 内层九宫格 道具单价
	 */
	private final String ninePrice;
	
	/**
	 * 购买1次获得固定奖励
	 */
	private final String extReward;
	
	/**
	 * 九格持续时间
	 */
	private final int nineCountdown;
	
	private final int fiveMaxjackpot;
	
	/**
	 * 翻牌消耗
	 */
	private List<String> nineCostList;
	
	/**
	 * 单例
	 */
	private static DestinyRevolverCfg instance = null;
	
	/**
	 * 构造
	 */
	public DestinyRevolverCfg() {
		instance = this;
		serverDelay = 0;
		multipleItem = "";
		fiveCost = "";
		nineCost = "";
		fivePrice = "";
		ninePrice = "";
		extReward = "";
		nineCountdown = 3600;
		fiveMaxjackpot = 0;
	}
	
	public static DestinyRevolverCfg getInstance() {
		return instance;
	}

	public long getServerDelay() {
		return serverDelay * 1000L;
	}

	public String getMultipleItem() {
		return multipleItem;
	}

	public String getFiveCost() {
		return fiveCost;
	}

	public String getNineCost() {
		return nineCost;
	}

	/**
	 * 翻牌消耗
	 */
	public String getTreasureCost(int index) {
		index = Math.min(index, nineCostList.size() - 1);
		return nineCostList.get(index);
	}
	
	public String getFivePrice() {
		return fivePrice;
	}

	public String getNinePrice() {
		return ninePrice;
	}

	public String getExtReward() {
		return extReward;
	}

	public long getNineCountdown() {
		return nineCountdown * 1000L;
	}

	public int getFiveMaxjackpot() {
		return fiveMaxjackpot;
	}

	@Override
	protected boolean assemble() {
		nineCostList = Splitter.on(";").omitEmptyStrings().splitToList(nineCost);
		return super.assemble();
	}

	/**
	 * 是否倍数道具 
	 */
	public boolean isMultipleItem(int itemId) {
		return multipleItem.contains(itemId + "");
	}
}
