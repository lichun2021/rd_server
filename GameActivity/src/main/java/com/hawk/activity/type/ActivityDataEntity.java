package com.hawk.activity.type;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hawk.db.HawkDBEntity;
import org.hawk.os.HawkTime;

import com.google.common.collect.ImmutableList;
import com.hawk.serialize.string.SerializeHelper;

public abstract class ActivityDataEntity extends HawkDBEntity implements IActivityDataEntity {

	private List<Integer> loginDaysList = new ArrayList<>();

	/**
	 * @param val <code>HawkTime.getYyyyMMddIntVal()</code>.
	 */
	public void recordLoginDay(int val) {
		if (StringUtils.isNotEmpty(getLoginDaysStr()) && loginDaysList.isEmpty()) {
			stringToLoginDaysList();
		}
		
		if (loginDaysList.contains(val)) {
			return;
		}
		
		loginDaysList.add(val);
		setLoginDaysStr(SerializeHelper.collectionToString(loginDaysList));
	}
	
	/**
	 * 记录登录天, 可重复添加.
	 * 如果想集成在框架中, 需要在 ActivityBase::onPlayerLogind , onEvent(ContinueLoginEvent event), 以及onOpen时调用.
	 */
	public void recordLoginDay() {
		int val = HawkTime.getYyyyMMddIntVal();
		recordLoginDay(val);
	}

	protected void stringToLoginDaysList(){
		SerializeHelper.stringToList(Integer.class, getLoginDaysStr(), loginDaysList);
	}

	public abstract void setLoginDaysStr(String loginDays);

	public abstract String getLoginDaysStr();
	
	/** 获取活动累计登录天*/
	public int getLoginDaysCount(){
		return loginDaysList.size();
	}

	
	/**
	 * @param star <code>HawkTime.getYyyyMMddIntVal()</code>.
	 */
	public int getLoginDaysCount(int star) {
		return (int) loginDaysList.stream().filter(day -> day >= star).count();
	}

	public List<Integer> getLoginDaysList() {
		if (loginDaysList.isEmpty() && StringUtils.isNotEmpty(getLoginDaysStr())) {
			stringToLoginDaysList();
		}
		return ImmutableList.copyOf(loginDaysList);
	}


}
