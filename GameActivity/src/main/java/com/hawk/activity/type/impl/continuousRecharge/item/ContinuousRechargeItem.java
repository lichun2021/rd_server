package com.hawk.activity.type.impl.continuousRecharge.item;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hawk.os.HawkOSOperator;

import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

public class ContinuousRechargeItem implements SplitEntity {
	/**
	 * 天数
	 */
	private int day;
	/**
	 * 数量
	 */
	private int count;
	/**
	 * 已领取
	 */
	private StringBuilder received = new StringBuilder();
	/**
	 * 已领取奖励的充值档次
	 */
	private Set<Integer> receivedSet = new HashSet<>();
	
	public ContinuousRechargeItem() {
	}
	
	public ContinuousRechargeItem(int day, int count) {
		this.day = day;
		this.count = count;
	}
	
	public ContinuousRechargeItem(String item) {
		String[] split = item.split("_");
		this.day = Integer.parseInt(split[0]);
		this.count = Integer.parseInt(split[1]);
		if (split.length > 2 && !HawkOSOperator.isEmptyString(split[2])) {
			this.received.append(split[2]);
			String[] receiveList = split[2].split(",");
			for (String received : receiveList) {
				receivedSet.add(Integer.valueOf(received));
			}
		}
	}
	
	public int getDay() {
		return day;
	}
	
	public void setDay(int day) {
		this.day = day;
	}
	
	public int getCount() {
		return count;
	}
	
	public void setCount(int count) {
		this.count = count;
	}
	
	public String getReceived() {
		return received.toString();
	}
	
	public void setReceived(String received) {
		if (!HawkOSOperator.isEmptyString(received)) {
			if (this.received.length() > 0) {
				this.received = new StringBuilder();
				receivedSet.clear();
			}
			
			this.received.append(received);
			String[] receiveList =  received.split(",");
			for (String receivedGrade : receiveList) {
				receivedSet.add(Integer.valueOf(receivedGrade));
			}
		}
	}
	
	public void addCount(int add) {
		this.count = this.count + add;
	}
	
	public void addReceivedGrade(int rechargeGrade) {
		receivedSet.add(rechargeGrade);
		if (received.length() == 0) {
			received.append(rechargeGrade);
		} else {
			received.append(",").append(rechargeGrade);
		}
	}
	
	public Set<Integer> getReceivedGrade() {
		return Collections.unmodifiableSet(receivedSet);
	}
	
	public String toString() {
		return String.format("%d_%d_%s", day, count, received.toString());
	}

	@Override
	public SplitEntity newInstance() {
		return new ContinuousRechargeItem();
	}

	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(day);
		dataList.add(count);
		dataList.add(this.received.toString());
	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(3);
		day = dataArray.getInt();
		count = dataArray.getInt();
		String receivedStr = dataArray.getString(); 
		if (!HawkOSOperator.isEmptyString(receivedStr)) {
			if (this.received.length() > 0) {
				this.received = new StringBuilder();
				receivedSet.clear();
			}
			
			this.received.append(receivedStr);
			String[] receiveArray =  receivedStr.split(",");
			for (String received : receiveArray) {
				receivedSet.add(Integer.valueOf(received));
			}
		}
	}
}
