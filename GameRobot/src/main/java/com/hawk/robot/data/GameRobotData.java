package com.hawk.robot.data;

public class GameRobotData {
	/**
	 * 基础数据
	 */
	protected BasicData robotBasicData;
	/**
	 * 城内数据
	 */
	protected CityData robotCityData;
	/**
	 * 任务、活动数据
	 */
	protected ActivityData robotActivityData;
	/**
	 * 联盟数据
	 */
	protected GuildData guildData;
	/**
	 * 世界数据
	 */
	protected WorldData worldData;
	
	protected AnchorData anchorData;
	
	public GameRobotData() {
		robotBasicData = new BasicData(this);
		robotCityData = new CityData(this);
		robotActivityData = new ActivityData(this);
		guildData = new GuildData(this);
		worldData = new WorldData(this);
		anchorData = new AnchorData(this);
	}
	
	public BasicData getBasicData() {
		return robotBasicData;
	}

	public CityData getCityData() {
		return robotCityData;
	}

	public ActivityData getActivityData() {
		return robotActivityData;
	}

	public GuildData getGuildData() {
		return guildData;
	}

	public WorldData getWorldData() {
		return worldData;
	}

	public AnchorData getAnchorData() {
		return anchorData;
	}
	
}
