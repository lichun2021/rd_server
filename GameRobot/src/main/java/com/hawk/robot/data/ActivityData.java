package com.hawk.robot.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.hawk.game.protocol.Activity.AchieveItemPB;
import com.hawk.game.protocol.Activity.ActivityType;
import com.hawk.game.protocol.BattleMission.BattleMissionPage;
import com.hawk.game.protocol.DailyMission.RewardState;
import com.hawk.game.protocol.Mission.MissionPB;
import com.hawk.game.protocol.StoryMission.StoryMissionPage;

public class ActivityData {
	/**
	 * 机器人信息(上层数据)
	 */
	private GameRobotData robotData;
	/**
	 * 任务对象列表
	 */
	protected Map<String, MissionPB> missionObjects = new ConcurrentHashMap<String, MissionPB>();
	/**
	 * 章节任务信息
	 */
	private StoryMissionPage storyMission;
	/**
	 * 军衔任务信息
	 */
	private BattleMissionPage battleMission;
	/**
	 * 日常任务奖励状态信息
	 */
	private List<RewardState> dailyMissionRewardStates = new CopyOnWriteArrayList<>();
	/**
	 * 
	 */
	private Map<ActivityType, Object> activityData = new HashMap<>();
	/**
	 * 
	 */
	private  List<AchieveItemPB> achieveInfo = new CopyOnWriteArrayList<>();
	
	////////////////////////////////////////////////////////////////////////
	
	public ActivityData(GameRobotData gameRobotData) {
		robotData = gameRobotData;
	}

	public GameRobotData getRobotData() {
		return robotData;
	}

	public Map<String, MissionPB> getMissionObjects() {
		return missionObjects;
	}
	
	/**
	 * 刷新任务数据
	 * @param missionList
	 */
	public void refreshMissionData(List<MissionPB> missionList) {
		if(missionList != null && missionList.size() > 0) {
			for(MissionPB mission : missionList) {
				missionObjects.put(mission.getMissionId(), mission);
			}
		}
	}
	
	/**
	 * 删除任务
	 * @param missionId
	 */
	public void deleteMission(String missionId) {
		missionObjects.remove(missionId);
	}
	
	public void saveStoryMission(StoryMissionPage storyMission) {
		this.storyMission = storyMission;
		
	}

	public StoryMissionPage getStoryMission() {
		return storyMission;
	}
	
	public BattleMissionPage getBattleMission() {
		return battleMission;
	}

	public void saveBattleMission(BattleMissionPage battleMission) {
		this.battleMission = battleMission;
	}
	
	public void resetDailyMissionRewardState(List<RewardState> rewardStates) {
		dailyMissionRewardStates.clear();
		dailyMissionRewardStates.addAll(rewardStates);
	}
	
	public List<RewardState> getDailyMissionRewardStateList() {
		return dailyMissionRewardStates;
	}

	public void setActivityData(ActivityType activityType, Object data) {
		activityData.put(activityType, data);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getActivityData(ActivityType activityType) {
		Object data = activityData.get(activityType);
		if (data == null) {
			return null;
		}
		return (T) data;
	}

	public void addAchiveData(List<AchieveItemPB> achieveInfo) {
		this.achieveInfo.addAll(achieveInfo);
	}
	
	public List<AchieveItemPB> getAchieveInfo() {
		return achieveInfo;
	}

}
