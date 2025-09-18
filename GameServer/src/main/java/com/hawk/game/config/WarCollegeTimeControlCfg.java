package com.hawk.game.config;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.JSONArray;
import com.google.common.base.Splitter;
import com.hawk.game.GsConfig;
import com.hawk.serialize.string.SerializeHelper;

@HawkConfigManager.KVResource(file = "xml/war_college_time_control.xml")
public class WarCollegeTimeControlCfg extends HawkConfigBase {
	/** 周几,可配置多个 */
	private final String days;
	/** 开始时间 时分秒 */
	private final String startTime;
	/** 结束时间 时分秒 */
	private final String endTime;

	// #联盟副本开关，如果为false，表示不开,需要读取白名单是否包含本服，如果包含就开；如果为true，表示开，则不需要理会白名单；
	protected final boolean warCollegeSwitch;
	
	//#带新奖励次数
	private final int dailyTeacherReward;

	// #联盟副本白名单
	protected final String warCollegeWhiteList;
	protected final int warCollegeQuitSheld;
	protected final String warCollegeQuitCd;
	
	private final String serverDaysOpen;
	private final int normalDays;
	
	
	
	
	
	private int[] daysArray;
	private int[] startTimeArray;
	private int[] endTimeArray;
	private int[] warCollegeQuitCdArray;
	private List<Integer> serverDaysOpenList = new ArrayList<>();

	public WarCollegeTimeControlCfg() {
		this.dailyTeacherReward = 0;
		this.days = "";
		this.startTime = "";
		this.endTime = "";
		warCollegeSwitch = true;
		warCollegeWhiteList = "[]";
		warCollegeQuitCd = "15";
		warCollegeQuitSheld = 1;
		serverDaysOpen= "";
		normalDays= 0;
	}
	
	

	public boolean isWarCollegeSwitch() {
		return warCollegeSwitch;
	}

	public String getWarCollegeWhiteList() {
		return warCollegeWhiteList;
	}

	public boolean openWarCollege() {
		if (warCollegeSwitch) {
			return true;
		}
		if (warCollegeWhiteList != null && warCollegeWhiteList.startsWith("[") && warCollegeWhiteList.endsWith("]")) {
			JSONArray array = JSONArray.parseArray(warCollegeWhiteList);
			for (int i = 0; i < array.size(); i++) {
				if (array.getString(i).trim().equals(GsConfig.getInstance().getServerId())) {
					return true;
				}
			}
		}
		return false;
	}

	public String getDays() {
		return days;
	}

	public String getStartTime() {
		return startTime;
	}

	public int[] getDaysArray() {
		return daysArray;
	}

	public int[] getStartTimeArray() {
		return startTimeArray;
	}

	@Override
	public boolean assemble() {
		daysArray = SerializeHelper.string2IntArray(days);
		String[] timeArray = startTime.split(":");
		startTimeArray = new int[3];
		for (int i = 0; i < timeArray.length; i++) {
			startTimeArray[i] = Integer.parseInt(timeArray[i]);
		}
		String[] tmpEndTimeArray = endTime.split(":");
		endTimeArray = new int[3];
		for (int i = 0; i < tmpEndTimeArray.length; i++) {
			endTimeArray[i] = Integer.parseInt(tmpEndTimeArray[i]);
		}
		warCollegeQuitCdArray = Splitter.on(",").splitToList(warCollegeQuitCd).stream().mapToInt(Integer::valueOf).toArray();
		
		List<Integer> serverDaysOpenListTemp = new ArrayList<>();
		if(!HawkOSOperator.isEmptyString(this.serverDaysOpen)){
			String[] arr = this.serverDaysOpen.split(SerializeHelper.ATTRIBUTE_SPLIT);
			for(String str : arr){
				serverDaysOpenListTemp.add(Integer.parseInt(str));
			}
		}
		this.serverDaysOpenList = serverDaysOpenListTemp;
		return true;
	}

	@Override
	public boolean checkValid() {
		for (int day : daysArray) {
			if (day < 1 || day > 7) {
				throw new InvalidParameterException(String.format("WarCollegeTimeControlCfg 星期只能配 1-7"));
			}
		}

		if (startTimeArray[0] < 0 || startTimeArray[0] > 23) {
			throw new InvalidParameterException(String.format("WarCollegeTimeControlCfg 小时只能配 0-23 "));
		}

		if (startTimeArray[1] < 0 || startTimeArray[1] > 59) {
			throw new InvalidParameterException(String.format("WarCollegeTimeControlCfg 分钟只能配置 0-59 "));
		}

		if (startTimeArray[2] < 0 || startTimeArray[2] > 59) {
			throw new InvalidParameterException(String.format("WarCollegeTimeControlCfg 秒只能配置 0-59 "));
		}

		if (endTimeArray[0] < 0 || endTimeArray[0] > 23) {
			throw new InvalidParameterException(String.format("WarCollegeTimeControlCfg 小时只能配 0-23 "));
		}

		if (endTimeArray[1] < 0 || endTimeArray[1] > 59) {
			throw new InvalidParameterException(String.format("WarCollegeTimeControlCfg 分钟只能配置 0-59 "));
		}

		if (endTimeArray[2] < 0 || endTimeArray[2] > 59) {
			throw new InvalidParameterException(String.format("WarCollegeTimeControlCfg 秒只能配置 0-59 "));
		}

		return true;
	}

	public int[] getEndTimeArray() {
		return endTimeArray;
	}

	public String getEndTime() {
		return endTime;
	}

	public int[] getWarCollegeQuitCdArray() {
		return warCollegeQuitCdArray;
	}

	public void setWarCollegeQuitCdArray(int[] warCollegeQuitCdArray) {
		this.warCollegeQuitCdArray = warCollegeQuitCdArray;
	}

	public int getWarCollegeQuitSheld() {
		return warCollegeQuitSheld;
	}

	public String getWarCollegeQuitCd() {
		return warCollegeQuitCd;
	}

	public void setDaysArray(int[] daysArray) {
		this.daysArray = daysArray;
	}

	public void setStartTimeArray(int[] startTimeArray) {
		this.startTimeArray = startTimeArray;
	}

	public void setEndTimeArray(int[] endTimeArray) {
		this.endTimeArray = endTimeArray;
	}
	
	
	public int getDailyTeacherReward() {
		return dailyTeacherReward;
	}
	
	public List<Integer> getServerDaysOpenList() {
		return serverDaysOpenList;
	}
	
	
	public int getNormalDays() {
		return normalDays;
	}
}
