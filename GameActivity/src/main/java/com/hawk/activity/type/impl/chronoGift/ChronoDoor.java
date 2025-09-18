package com.hawk.activity.type.impl.chronoGift;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hawk.game.protocol.Activity.ChronoGiftState;
import com.hawk.game.protocol.Activity.PBChronoGift;
import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SerializeHelper;
import com.hawk.serialize.string.SplitEntity;

/**
 * 时空之门
 * 
 * @author che
 *
 */
public class ChronoDoor implements SplitEntity {
	
	/** 礼包id*/
	private int giftId;
	
	/** 免费礼品领取时间*/
	private long freeAwardTime;
	
	/** 直购礼包购买时间 */
	private long buyPackageTime;

	/** 直购礼包选择奖励ID */
	private List<Integer> rewardIdList;

	/** 开门时间*/
	private long openTime;
	
	
	public ChronoDoor() {
		rewardIdList = new ArrayList<Integer>();
	}
	
	public static ChronoDoor valueOf(int giftId) {
		ChronoDoor data = new ChronoDoor();
		data.giftId = giftId;
		return data;
	}
	
	
	

	public int getGiftId() {
		return giftId;
	}

	public void setGiftId(int giftId) {
		this.giftId = giftId;
	}

	public long getFreeAwardTime() {
		return freeAwardTime;
	}

	public void setFreeAwardTime(long freeAwardTime) {
		this.freeAwardTime = freeAwardTime;
	}

	

	public long getBuyPackageTime() {
		return buyPackageTime;
	}

	public void setBuyPackageTime(long buyPackageTime) {
		this.buyPackageTime = buyPackageTime;
	}
	


	public long getOpenTime() {
		return openTime;
	}

	public void setOpenTime(long openTime) {
		this.openTime = openTime;
	}

	
	public void AddRewardIdList(Collection<Integer> rewardIdList) {
		this.rewardIdList.clear();
		this.rewardIdList.addAll(rewardIdList);
	}

	public List<Integer> getRewardIdList() {
		return rewardIdList;
	}
	


	public ChronoGiftState getFreeAwardState(){
		if(this.freeAwardTime > 0){
			return ChronoGiftState.ACHIEVE;
		}
		return ChronoGiftState.NO_ACHIEVE;
	}
	
	
	public ChronoGiftState getBuyAwardState(){
		if(this.buyPackageTime > 0){
			return ChronoGiftState.ACHIEVE;
		}
		return ChronoGiftState.NO_ACHIEVE;
	}
	
	@Override
	public SplitEntity newInstance() {
		return new ChronoDoor();
	}
	
	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(this.giftId);
		dataList.add(this.openTime);
		dataList.add(this.freeAwardTime);
		dataList.add(this.buyPackageTime);
		dataList.add(SerializeHelper.collectionToString(this.rewardIdList, SerializeHelper.BETWEEN_ITEMS));
	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(5);
		this.giftId = dataArray.getInt();
		this.openTime = dataArray.getLong();
		this.freeAwardTime = dataArray.getLong();
		this.buyPackageTime = dataArray.getLong();
		String rewardIdStr = dataArray.getString();
		this.rewardIdList = SerializeHelper.stringToList(Integer.class, rewardIdStr, SerializeHelper.BETWEEN_ITEMS);
	}

	@Override
	public String toString() {
		return "[giftId=" + giftId + "freeAwardTime=" + freeAwardTime+ ", buyPackageTime=" + buyPackageTime + ", rewardIdList=" + rewardIdList + "]";
	}
	
	public PBChronoGift.Builder toBuilder() {
		PBChronoGift.Builder builder = PBChronoGift.newBuilder();
		builder.setGiftId(this.giftId);
		builder.setFreeAwardState(this.getFreeAwardState());
		builder.setBuyAwardState(this.getBuyAwardState());
		builder.addAllBuyAwards(this.rewardIdList);
		return builder;
	}
	
}
