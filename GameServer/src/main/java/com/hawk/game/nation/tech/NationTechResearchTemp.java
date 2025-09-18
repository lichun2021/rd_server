package com.hawk.game.nation.tech;

import java.util.List;

import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

/**
 * 国家科技研究信息
 * @author Golden
 *
 */
public class NationTechResearchTemp implements SplitEntity {

	/**
	 * 科技id
	 */
	private int techCfgId;
	
	/**
	 * 研究结束时间
	 */
	private long endTime;
	
	/**
	 * 目标等级
	 */
	private int tarLevel;
	
	/**
	 * 日标记值
	 */
	private int dayMark;
	
	/**
	 * 帮助时间
	 */
	private long helpTime;
	
	public NationTechResearchTemp() {
		
	}
	
	public int getTechCfgId() {
		return techCfgId;
	}

	public void setTechCfgId(int techCfgId) {
		this.techCfgId = techCfgId;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public void reduceEndTime(long time) {
		this.endTime -= time;
	}
	
	public int getTarLevel() {
		return tarLevel;
	}

	public void setTarLevel(int tarLevel) {
		this.tarLevel = tarLevel;
	}

	public int getDayMark() {
		return dayMark;
	}

	public void setDayMark(int dayMark) {
		this.dayMark = dayMark;
	}

	public long getHelpTime() {
		return helpTime;
	}

	public void setHelpTime(long helpTime) {
		this.helpTime = helpTime;
	}

	public void addHelpTime(long helpTime) {
		this.helpTime += helpTime;
	}
	
	public NationTechResearchTemp(int techCfgId, long endTime, int tarLevel, int dayMark) {
		this.techCfgId = techCfgId;
		this.endTime = endTime;
		this.tarLevel = tarLevel;
		this.dayMark = dayMark;
		this.helpTime = 0L;
	}
	
	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(techCfgId);
		dataList.add(endTime);
		dataList.add(tarLevel);
		dataList.add(dayMark);
		dataList.add(helpTime);
	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(5);
		techCfgId = dataArray.getInt();
		endTime = dataArray.getLong();
		tarLevel = dataArray.getInt();
		dayMark = dataArray.getInt();
		helpTime = dataArray.getLong();
	}
	
	@Override
	public String toString() {
		return techCfgId + "_" + endTime + "_" + dayMark + "_" + tarLevel + "_" +helpTime;
	}

	@Override
	public SplitEntity newInstance() {
		return new NationTechResearchTemp(0, 0, 0, 0);
	}
}
