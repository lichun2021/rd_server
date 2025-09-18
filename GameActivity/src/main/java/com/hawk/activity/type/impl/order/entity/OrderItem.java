package com.hawk.activity.type.impl.order.entity;

import java.util.List;

import com.hawk.game.protocol.Activity.OrderItemPB;
import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

/**
 * 战令数据
 * @author Jesse
 *
 */
public class OrderItem implements SplitEntity {
	
	/** 成就id*/
	private int orderId;
	
	/** 完成次数*/
	private int finishTimes;
	
	/** 任务值*/
	private long value;
	
	public OrderItem() {
	}
	
	public static OrderItem valueOf(int orderId) {
		OrderItem data = new OrderItem();
		data.orderId = orderId;
		return data;
	}
	
	public int getOrderId() {
		return orderId;
	}

	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}

	public int getFinishTimes() {
		return finishTimes;
	}

	public void setFinishTimes(int finishTimes) {
		this.finishTimes = finishTimes;
	}

	public long getValue() {
		return value;
	}

	public void setValue(long value) {
		this.value = value;
	}


	@Override
	public SplitEntity newInstance() {
		return new OrderItem();
	}
	
	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(orderId);
		dataList.add(finishTimes);
		dataList.add(value);
	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(3);
		orderId = dataArray.getInt();
		finishTimes = dataArray.getInt();
		value = dataArray.getInt();
	}

	/**
	 * 重置数据
	 */
	public void reset() {
		this.finishTimes = 0;
		this.value = 0;
	}

	@Override
	public String toString() {
		return "OrderItem [orderId=" + orderId + ", finishTimes=" + finishTimes + ", value=" + value + "]";
	}
	
	public OrderItem getCopy(){
		OrderItem copy = new OrderItem();
		copy.setOrderId(this.orderId);
		copy.setFinishTimes(this.finishTimes);
		copy.setValue(this.value);
		return copy;
	}
	
	/**
	 * 构建战令PB数据
	 * @return
	 */
	public OrderItemPB build(){
		OrderItemPB.Builder builder = OrderItemPB.newBuilder();
		builder.setId(this.orderId);
		builder.setTimes(this.finishTimes);
		builder.setValue(this.value);
		return builder.build();
	}

}
