package com.hawk.activity.type.impl.giftzero.entity;

import java.util.List;

import org.hawk.os.HawkTime;

import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

/**
 * 0元礼包购买信息
 * 
 * @author lating
 *
 */
public class GiftZeroItem implements SplitEntity {
	/**
	 * 礼包ID
	 */
	private int giftId;
	/**
	 * 购买时间
	 */
	private long purchaseTime;
	/**
	 * 消耗返还时间
	 */
	private long consumeBackTime;
	
	public GiftZeroItem() {
		
	}
	
	public GiftZeroItem(int giftId) {
		this.giftId = giftId;
		this.purchaseTime = HawkTime.getMillisecond();
	}
	
	public int getGiftId() {
		return giftId;
	}

	public void setGiftId(int giftId) {
		this.giftId = giftId;
	}

	public long getPurchaseTime() {
		return purchaseTime;
	}

	public void setPurchaseTime(long purchaseTime) {
		this.purchaseTime = purchaseTime;
	}

	public long getConsumeBackTime() {
		return consumeBackTime;
	}

	public void setConsumeBackTime(long consumeBackTime) {
		this.consumeBackTime = consumeBackTime;
	}

	@Override
	public SplitEntity newInstance() {
		return new GiftZeroItem();
	}

	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(giftId);
		dataList.add(purchaseTime);
		dataList.add(consumeBackTime);
	}

	@Override
	public void fullData(DataArray dataArray) {
		giftId = dataArray.getInt();
		purchaseTime = dataArray.getLong();
		consumeBackTime = dataArray.getLong();
	}
}
