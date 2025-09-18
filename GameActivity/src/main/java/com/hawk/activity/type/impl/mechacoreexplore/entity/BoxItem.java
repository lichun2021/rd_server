package com.hawk.activity.type.impl.mechacoreexplore.entity;

import java.util.List;

import com.hawk.game.protocol.Activity.CEZoneBoxPB;
import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

/**
 * 宝箱数据
 * @author lating
 *
 */
public class BoxItem implements SplitEntity {
	
	/** 行 */
	private int line;
	
	/** 列  */
	private int column;
	
	/** 已领取次数 */
	private int recieveTimes;

	/** 可领取次数 */
	private int totalTimes;
	
	public BoxItem() {
	}
	
	public static BoxItem valueOf(int line, int column, int totalTimes) {
		BoxItem data = new BoxItem();
		data.line = line;
		data.column = column;
		data.totalTimes = totalTimes;
		return data;
	}
	
	@Override
	public BoxItem newInstance() {
		return new BoxItem();
	}
	
	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(line);
		dataList.add(column);
		dataList.add(recieveTimes);
		dataList.add(totalTimes);
	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(4);
		line = dataArray.getInt();
		column = dataArray.getInt();
		recieveTimes = dataArray.getInt();
		totalTimes = dataArray.getInt();
	}

	@Override
	public String toString() {
		return "(" + line + "," + column + "), " + recieveTimes + "," + totalTimes;
	}
	
	public CEZoneBoxPB.Builder toBuilder() {
		CEZoneBoxPB.Builder builder = CEZoneBoxPB.newBuilder();
		builder.setLine(line);
		builder.setColumn(column);
		builder.setReceiveTimes(recieveTimes);
		builder.setTotalTimes(totalTimes);
		return builder;
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}

	public int getColumn() {
		return column;
	}

	public void setColumn(int column) {
		this.column = column;
	}

	public int getRecieveTimes() {
		return recieveTimes;
	}

	public void setRecieveTimes(int recieveTimes) {
		this.recieveTimes = recieveTimes;
	}

	public int getTotalTimes() {
		return totalTimes;
	}

	public void setTotalTimes(int totalTimes) {
		this.totalTimes = totalTimes;
	}
	
	public boolean recieveEnd() {
		return this.recieveTimes >= this.totalTimes;
	}
	
}
