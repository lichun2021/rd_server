package com.hawk.game.module.college.entity;

import java.util.List;

import com.hawk.game.util.GsConst.MissionState;
import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

public class CollegeMissionEntityItem implements SplitEntity {

	/** 配置id*/
	protected int cfgId;

	/** 进度值*/
	protected long value;

	/** 状态 0 未完成 1已完成 2已领奖*/
	protected int state;

	protected int yearDay;
	protected int yearWeek;
	protected int month;

	public CollegeMissionEntityItem() {

	}

	public int getCfgId() {
		return cfgId;
	}

	public long getValue() {
		return value;
	}

	public void setValue(long value) {
		this.value = value;
	}

	public void addValue(int value) {
		this.value += value;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getYearDay() {
		return yearDay;
	}

	public void setYearDay(int yearDay) {
		this.yearDay = yearDay;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public void setCfgId(int cfgId) {
		this.cfgId = cfgId;
	}

	public int getYearWeek() {
		return yearWeek;
	}

	public void setYearWeek(int yearWeek) {
		this.yearWeek = yearWeek;
	}

	/**
	 * 任务是否完成
	 */
	public boolean isMissionComplete() {
		return this.state == MissionState.STATE_FINISH;
	}

	/**
	 * 任务是否领奖
	 */
	public boolean isMissionBonus() {
		return this.state == MissionState.STATE_BONUS;
	}

	@Override
	public CollegeMissionEntityItem newInstance() {
		return new CollegeMissionEntityItem();
	}

	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(cfgId);
		dataList.add(value);
		dataList.add(state);
		dataList.add(yearDay);
		dataList.add(yearWeek);
		dataList.add(month);

	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(5);
		cfgId = dataArray.getInt();
		value = dataArray.getInt();
		state = dataArray.getInt();
		yearDay = dataArray.getInt();
		yearWeek = dataArray.getInt();
		month = dataArray.getInt();

	}
}
