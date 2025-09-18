package com.hawk.activity.type.impl.doubleGift.entity;

import java.util.List;
import org.hawk.app.HawkApp;
import org.hawk.os.HawkTime;
import com.hawk.game.protocol.Activity.DoubleGiftInfo;
import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

/**
 * 双享豪礼购买项数据
 */
public class DoubleGiftItem implements SplitEntity {
	
	//礼包id
	private int giftId = 0;
	//礼包档次类型
	private int rewardId = 0;
	// 购买时间 
	private long buyTime = 0;
	
	public DoubleGiftItem() {
		
	}
	
	public DoubleGiftItem(int giftId, int rewardId) {
		this.giftId = giftId;
		this.rewardId = rewardId;
	}
	
	
	public int getRewardId() {
		return rewardId;
	}

	public void setRewardId(int rewardId) {
		this.rewardId = rewardId;
	}

	public int getGiftId() {
		return giftId;
	}

	public void setGiftId(int giftId) {
		this.giftId = giftId;
	}

	public long getBuyTime() {
		return buyTime;
	}

	public void setBuyTime(long buyTime) {
		this.buyTime = buyTime;
	}

	@Override
	public SplitEntity newInstance() {
		return new DoubleGiftItem();
	}
	
	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(giftId);
		dataList.add(rewardId);
		dataList.add(buyTime);
	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(3);
		giftId = dataArray.getInt();
		rewardId = dataArray.getInt();
		buyTime = dataArray.getLong();
	}

	public DoubleGiftInfo.Builder toBuilder() {
		DoubleGiftInfo.Builder builder = DoubleGiftInfo.newBuilder();
		builder.setGiftId(giftId);
		builder.setRewardConfigId(rewardId);
		builder.setIsBuy(HawkTime.isSameDay(buyTime, HawkApp.getInstance().getCurrentTime()));
		return builder;
	}
	
	@Override
	public String toString() {
		return "[giftId=" + giftId + ", rewardId=" + rewardId + ", buyTime=" + buyTime + "]";
	}
}
