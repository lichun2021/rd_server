package com.hawk.game.nation.mission;

import org.hawk.os.HawkTime;
import org.hawk.uuid.HawkUUIDGenerator;

/**
 * 国家任务
 * @author Golden
 *
 */
public class NationMissionTemp {

	/**
	 * 任务uuid
	 */
	public String uuid;

	/**
	 * 任务id
	 */
	public int missionId;

	/**
	 * 任务刷新出来的时间
	 */
	public long refreshTime;
	
	/**
	 * 接取任务次数
	 */
	public int pickUpTimes;
	
	public NationMissionTemp() {
		
	}
	
	/**
	 * 构造方法
	 * @param missionId
	 */
	public NationMissionTemp(int missionId) {
		this.uuid = HawkUUIDGenerator.genUUID();
		this.missionId = missionId;
		this.refreshTime = HawkTime.getMillisecond();
		this.pickUpTimes = 0;
	}

	public NationMissionTemp(String missionInfo) {
		String[] info = missionInfo.split("_");
		this.uuid = info[0];
		this.missionId = Integer.parseInt(info[1]);
		this.refreshTime = Long.parseLong(info[2]);
		this.pickUpTimes = Integer.parseInt(info[3]);
	}
	
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public int getMissionId() {
		return missionId;
	}

	public void setMissionId(int missionId) {
		this.missionId = missionId;
	}

	public long getRefreshTime() {
		return refreshTime;
	}

	public void setRefreshTime(long refreshTime) {
		this.refreshTime = refreshTime;
	}

	public int getPickUpTimes() {
		return pickUpTimes;
	}

	public void setPickUpTimes(int pickUpTimes) {
		this.pickUpTimes = pickUpTimes;
	}
	
	public void addPickUpTimes() {
		this.pickUpTimes++;
	}
	
	public String toString() {
		return 	uuid + "_" + missionId + "_" + refreshTime + "_" + pickUpTimes;
	}
}
