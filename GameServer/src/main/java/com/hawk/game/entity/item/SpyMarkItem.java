package com.hawk.game.entity.item;

import java.util.List;

import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

/**
 * 侦查标记
 * @author golden
 *
 */
public class SpyMarkItem implements SplitEntity {

	private int pointId;
	private int startTime;
	private String mailId;
	private String marchId;
	
	public SpyMarkItem() {
		
	}
	
	public SpyMarkItem(int pointId, int startTime, String mailId, String marchId) {
		this.pointId = pointId;
		this.startTime = startTime;
		this.mailId = mailId;
		this.marchId = marchId;
	}
	
	@Override
	public SplitEntity newInstance() {
		return new DressItem();
	}

	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(pointId);
		dataList.add(startTime);
		dataList.add(mailId);
		dataList.add(marchId);
	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(4);
		pointId = dataArray.getInt();
		startTime = dataArray.getInt();
		mailId = dataArray.getString();
		marchId = dataArray.getString();
	}

	public int getPointId() {
		return pointId;
	}

	public void setPointId(int pointId) {
		this.pointId = pointId;
	}

	public int getStartTime() {
		return startTime;
	}

	public void setStartTime(int startTime) {
		this.startTime = startTime;
	}

	public String getMailId() {
		return mailId;
	}

	public void setMailId(String mailId) {
		this.mailId = mailId;
	}

	public String getMarchId() {
		return marchId;
	}

	public void setMarchId(String marchId) {
		this.marchId = marchId;
	}
}
