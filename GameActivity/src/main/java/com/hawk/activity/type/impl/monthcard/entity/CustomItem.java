package com.hawk.activity.type.impl.monthcard.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SerializeHelper;
import com.hawk.serialize.string.SplitEntity;

/**
 * 定制物品
 * 
 * @author lating
 *
 */
public class CustomItem implements SplitEntity {
	
	/** 月卡id*/
	private int cardId;

	/** 奖励ID */
	private List<Integer> rewardIdList;
	
	public CustomItem() {
		rewardIdList = new ArrayList<Integer>();
	}
	
	public static CustomItem valueOf(int cardId) {
		CustomItem data = new CustomItem();
		data.cardId = cardId;
		return data;
	}
	
	public int getCardId() {
		return cardId;
	}

	public void setCardId(int cardId) {
		this.cardId = cardId;
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
		return new CustomItem();
	}
	
	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(cardId);
		dataList.add(SerializeHelper.collectionToString(this.rewardIdList, SerializeHelper.BETWEEN_ITEMS));
	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(2);
		cardId = dataArray.getInt();
		String rewardIdStr = dataArray.getString();
		this.rewardIdList = SerializeHelper.stringToList(Integer.class, rewardIdStr, SerializeHelper.BETWEEN_ITEMS);
	}

	@Override
	public String toString() {
		return "[cardId=" + cardId + ", rewardIdList=" + rewardIdList + "]";
	}
	
}
