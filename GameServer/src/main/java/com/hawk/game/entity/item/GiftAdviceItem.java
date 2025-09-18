package com.hawk.game.entity.item;

import java.util.List;

import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

/**
 * 礼包推荐
 * @author Golden
 *
 */
public class GiftAdviceItem implements SplitEntity {

	/**
	 * 礼包id
	 */
	private int giftGroupId;
	
	/**
	 * 今日推荐次数
	 */
	private int dayAdviceCount;
	
	/**
	 * 总推荐次数
	 */
	private int allAdviceCount;
	
	/**
	 * 上次推荐时间
	 */
	private long lastAdviceTime;

	/**
	 * 购买次数
	 */
	private int buyTimes;
	
	@Override
	public SplitEntity newInstance() {
		return new GiftAdviceItem();
	}

	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(giftGroupId);
		dataList.add(dayAdviceCount);
		dataList.add(allAdviceCount);
		dataList.add(lastAdviceTime);
		dataList.add(buyTimes);
	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(5);
		giftGroupId = dataArray.getInt();
		dayAdviceCount = dataArray.getInt();
		allAdviceCount = dataArray.getInt();
		lastAdviceTime = dataArray.getLong();
		buyTimes = dataArray.getInt();
	}

	public int getGiftGroupId() {
		return giftGroupId;
	}

	public void setGiftGroupId(int giftGroupId) {
		this.giftGroupId = giftGroupId;
	}

	public int getDayAdviceCount() {
		return dayAdviceCount;
	}

	public void addDayAdviceCount() {
		this.dayAdviceCount += 1;
	}
	
	public void clearDayAdvice() {
		this.dayAdviceCount = 0;
	}
	
	public int getAllAdviceCount() {
		return allAdviceCount;
	}

	public void addAllAdviceCount() {
		this.allAdviceCount += 1;
	}
	
	public long getLastAdviceTime() {
		return lastAdviceTime;
	}

	public void setLastAdviceTime(long lastAdviceTime) {
		this.lastAdviceTime = lastAdviceTime;
	}

	public int getBuyTimes() {
		return buyTimes;
	}

	public void addBuyTimes() {
		this.buyTimes += 1;
	}
	
}
