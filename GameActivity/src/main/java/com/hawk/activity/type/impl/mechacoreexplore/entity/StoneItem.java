package com.hawk.activity.type.impl.mechacoreexplore.entity;

import java.util.List;

import com.hawk.game.protocol.Activity.CEZoneStonePB;
import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

/**
 * 石头数据
 * @author lating
 *
 */
public class StoneItem implements SplitEntity {
	
	/** 行 */
	private int line;
	
	/** 列  */
	private int column;
	
	/** 已点击次数 */
	private int clickTimes;

	public StoneItem() {
	}
	
	public static StoneItem valueOf(int line, int column) {
		StoneItem data = new StoneItem();
		data.line = line;
		data.column = column;
		data.clickTimes = 2;
		return data;
	}
	
	@Override
	public StoneItem newInstance() {
		return new StoneItem();
	}
	
	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(line);
		dataList.add(column);
		dataList.add(clickTimes);
	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(3);
		line = dataArray.getInt();
		column = dataArray.getInt();
		clickTimes = dataArray.getInt();
	}

	@Override
	public String toString() {
		return "(" + line + "," + column + "), " + clickTimes;
	}
	
	public CEZoneStonePB.Builder toBuilder() {
		CEZoneStonePB.Builder builder = CEZoneStonePB.newBuilder();
		builder.setLine(line);
		builder.setColumn(column);
		builder.setClickTimes(2 - clickTimes);
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

	public int getClickTimes() {
		return clickTimes;
	}

	public void setClickTimes(int clickTimes) {
		this.clickTimes = clickTimes;
	}

}
