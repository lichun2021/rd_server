package com.hawk.game.module.college.entity;

import java.util.List;
import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;
import com.hawk.game.protocol.MilitaryCollege.CollegeShopItem;

/**
 * 兑换商店的商品数据
 */
public class CollegeMemberShopEntity implements SplitEntity {
	/**
	 * 商品id
	 */
	private int id;
	/**
	 * 已购买数量
	 */
	private int buyCount;
	/**
	 * 
	 */
	private int tip;
	
	
	private long refreshTime;
	
	public static CollegeMemberShopEntity valueOf(int shopId, long refreshTime) {
		CollegeMemberShopEntity data = new CollegeMemberShopEntity();
		data.id = shopId;
		data.refreshTime = refreshTime;
		data.tip = 1;
		return data;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public int getBuyCount() {
		return buyCount;
	}

	public void setBuyCount(int buyCount) {
		this.buyCount = buyCount;
	}
	
	public int getTip() {
		return tip;
	}
	
	public void setTip(int tip) {
		this.tip = tip;
	}
	
	public void setRefreshTime(long refreshTime) {
		this.refreshTime = refreshTime;
	}
	
	
	public long getRefreshTime() {
		return refreshTime;
	}

	public CollegeShopItem.Builder toBuilder() {
		CollegeShopItem.Builder builder = CollegeShopItem.newBuilder();
		builder.setShopId(id);
		builder.setExchangeCount(buyCount);
		builder.setTip(tip);
		return builder;
	}
	
	@Override
	public SplitEntity newInstance() {
		return new CollegeMemberShopEntity();
	}

	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(id);
		dataList.add(buyCount);
		dataList.add(tip);
		dataList.add(this.refreshTime);
	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(4);
		id = dataArray.getInt();
		buyCount = dataArray.getInt();
		tip = dataArray.getInt();
		refreshTime = dataArray.getLong();
	}
}
