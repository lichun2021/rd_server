package com.hawk.activity.type.impl.redrecharge.entity;

import java.util.List;

import org.hawk.os.HawkTime;

import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

public class HappyRedRechargeItem implements SplitEntity {
	
	/** 配置项ID */
	private int cfgId;
	
	/** 已购买次数 */
	private int buyCount;
	
	/** 当天已购买次数 */
	private int buyCountToday;

	/** 最近一次购买时间 */
	private long latestBuyTime;
	
	public HappyRedRechargeItem() {
	}
	
	public static HappyRedRechargeItem valueOf(int cfgId) {
		HappyRedRechargeItem data = new HappyRedRechargeItem();
		data.cfgId = cfgId;
		data.buyCount = 1;
		data.buyCountToday = 1;
		data.latestBuyTime = HawkTime.getMillisecond();
		return data;
	}

	public int getCfgId() {
		return cfgId;
	}

	public void setCfgId(int cfgId) {
		this.cfgId = cfgId;
	}

	public int getBuyCount() {
		return buyCount;
	}

	public void setBuyCount(int buyCount) {
		this.buyCount = buyCount;
	}

	public long getLatestBuyTime() {
		return latestBuyTime;
	}

	public void setLatestBuyTime(long latestBuyTime) {
		this.latestBuyTime = latestBuyTime;
	}

	@Override
	public SplitEntity newInstance() {
		return new HappyRedRechargeItem();
	}

	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(cfgId);
		dataList.add(buyCount);
		dataList.add(buyCountToday);
		dataList.add(latestBuyTime);
	}

	@Override
	public void fullData(DataArray dataArray) {
		cfgId = dataArray.getInt();
		buyCount = dataArray.getInt();
		buyCountToday = dataArray.getInt();
		latestBuyTime = dataArray.getLong();
	}

	public int getBuyCountToday() {
		return buyCountToday;
	}

	public void setBuyCountToday(int buyCountToday) {
		this.buyCountToday = buyCountToday;
	}
	
}
