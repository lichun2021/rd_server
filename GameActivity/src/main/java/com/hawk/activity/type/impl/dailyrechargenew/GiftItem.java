package com.hawk.activity.type.impl.dailyrechargenew;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import com.hawk.game.protocol.Activity.RechargeBuyGiftInfoPB;
import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SerializeHelper;
import com.hawk.serialize.string.SplitEntity;

/**
 * 今日累充特价礼包数据
 * 
 * @author lating
 *
 */
public class GiftItem implements SplitEntity {
	
	/** 礼包id*/
	private int giftId;
	
	/** 购买时间 */
	private long purchaseTime;

	/** 奖励ID */
	private List<Integer> rewardIdList;
	
	public GiftItem() {
		rewardIdList = new ArrayList<Integer>();
	}
	
	public static GiftItem valueOf(int giftId) {
		GiftItem data = new GiftItem();
		data.giftId = giftId;
		return data;
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

	public void setRewardIdList(Collection<Integer> rewardIdList) {
		this.rewardIdList.clear();
		this.rewardIdList.addAll(rewardIdList);
	}

	public List<Integer> getRewardIdList() {
		return rewardIdList;
	}
	
	@Override
	public SplitEntity newInstance() {
		return new GiftItem();
	}
	
	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(giftId);
		dataList.add(purchaseTime);
		dataList.add(SerializeHelper.collectionToString(this.rewardIdList, SerializeHelper.BETWEEN_ITEMS));
	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(3);
		giftId = dataArray.getInt();
		purchaseTime = dataArray.getLong();
		String rewardIdStr = dataArray.getString();
		this.rewardIdList = SerializeHelper.stringToList(Integer.class, rewardIdStr, SerializeHelper.BETWEEN_ITEMS);
	}

	@Override
	public String toString() {
		return "[giftId=" + giftId + ", purchaseTime=" + purchaseTime + ", rewardIdList=" + rewardIdList + "]";
	}
	
	public RechargeBuyGiftInfoPB.Builder toBuilder() {
		RechargeBuyGiftInfoPB.Builder builder = RechargeBuyGiftInfoPB.newBuilder();
		builder.setGiftId(giftId);
		builder.addAllRewardId(rewardIdList);
		builder.setBought(purchaseTime > 0);
		return builder;
	}
	
}
