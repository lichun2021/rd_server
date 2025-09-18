package com.hawk.activity.type.impl.mechacoreexplore.entity;

import java.util.List;

import com.hawk.game.protocol.Activity.CEFreePickPB;
import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

/**
 * （免费）矿镐数据
 * 
 * - 免费矿镐有上限
     - 上限读取 core_explore_base 表 pickLimit 字段
   - 每日0时，免费赠与玩家n个免费矿镐。如果赠与后超过了上限则只赠与到上限数量
     - 赠与数量n读取 core_explore_base 表 dailyFreePick 字段
   - 每隔x秒赠与玩家n个矿镐
     - 每隔x秒检测一次，如果玩家免费矿镐数量未达到上限，则额外赠与玩家n个免费矿镐；如果达到了上限，则不赠与
     - 检测间隔x读取 core_explore_base 表 freePickCd 字段，赠送数量n读取 freePickNum 字段

 * @author lating
 *
 */
public class PickItem implements SplitEntity {
	
	/** 每日（0点）赠与矿镐是否已发放：1是0否 */
	private int dailySendYet;
	
	/** 当日赠与矿镐数总量  */
	private int sendPickDaily;
	
	private int sendPickTotal;
	
	/** 上一次赠与矿镐的时间 */
	private long lastSendTime;
	
	public PickItem() {
	}
	
	public static PickItem valueOf() {
		PickItem data = new PickItem();
		return data;
	}
	
	@Override
	public PickItem newInstance() {
		return new PickItem();
	}
	
	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(dailySendYet);
		dataList.add(sendPickDaily);
		dataList.add(sendPickTotal);
		dataList.add(lastSendTime);
	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(4);
		dailySendYet = dataArray.getInt();
		sendPickDaily = dataArray.getInt();
		sendPickTotal = dataArray.getInt();
		lastSendTime = dataArray.getLong();
	}

	@Override
	public String toString() {
		return dailySendYet + ", " + sendPickDaily + ", " + sendPickTotal + ", " + lastSendTime;
	}
	
	public CEFreePickPB.Builder toBuilder(int freePickCount) {
		CEFreePickPB.Builder builder = CEFreePickPB.newBuilder();
		builder.setLastPickTime(lastSendTime);
		builder.setPickTotalToday(sendPickDaily);
		builder.setFreePickCount(freePickCount);
		return builder;
	}

	public int getDailySendYet() {
		return dailySendYet;
	}

	public void setDailySendYet(int dailySendYet) {
		this.dailySendYet = dailySendYet;
	}

	public int getSendPickDaily() {
		return sendPickDaily;
	}

	public void setSendPickDaily(int sendPickDaily) {
		this.sendPickDaily = sendPickDaily;
	}

	public int getSendPickTotal() {
		return sendPickTotal;
	}

	public void setSendPickTotal(int sendPickTotal) {
		this.sendPickTotal = sendPickTotal;
	}

	public long getLastSendTime() {
		return lastSendTime;
	}

	public void setLastSendTime(long lastSendTime) {
		this.lastSendTime = lastSendTime;
	}
	
}
