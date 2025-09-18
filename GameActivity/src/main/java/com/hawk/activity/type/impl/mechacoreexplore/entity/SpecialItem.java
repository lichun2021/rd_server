package com.hawk.activity.type.impl.mechacoreexplore.entity;

import java.util.List;

import com.hawk.game.protocol.Activity.CEZoneItemPB;
import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

/**
 * 沙土附带特殊道具
 * @author lating
 *
 */
public class SpecialItem implements SplitEntity {
	
	/** 行 */
	private int line;
	
	/** 列  */
	private int column;
	
	/** 道具三段是 */
	private String item;

	public SpecialItem() {
	}
	
	public static SpecialItem valueOf(int line, int column, String item) {
		SpecialItem data = new SpecialItem();
		data.line = line;
		data.column = column;
		data.item = item;
		return data;
	}
	
	@Override
	public SpecialItem newInstance() {
		return new SpecialItem();
	}
	
	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(line);
		dataList.add(column);
		dataList.add(item);
	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(3);
		line = dataArray.getInt();
		column = dataArray.getInt();
		item = dataArray.getString();
	}

	@Override
	public String toString() {
		return "(" + line + "," + column + "), " + item;
	}
	
	public CEZoneItemPB.Builder toBuilder() {
		CEZoneItemPB.Builder builder = CEZoneItemPB.newBuilder();
		builder.setLine(line);
		builder.setColumn(column);
		builder.setItem(item);
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

	public String getItem() {
		return item;
	}

	public void setItem(String item) {
		this.item = item;
	}

}
