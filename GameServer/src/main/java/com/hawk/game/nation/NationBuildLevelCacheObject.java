package com.hawk.game.nation;

import org.hawk.os.HawkTime;

/**
 * 国家建筑等级缓存信息
 * 
 * @author lating
 *
 */
public class NationBuildLevelCacheObject {
	/**
	 * 建筑类型ID
	 */
	private int buildId;
	/**
	 * 建筑等级
	 */
	private int level;
	/**
	 * 所属区服
	 */
	private String serverId;
	/**
	 * 刷新的时间
	 */
	private long refreshTime;

	public NationBuildLevelCacheObject(int buildId, int level) {
		this.buildId = buildId;
		this.level = level;
		this.refreshTime = HawkTime.getMillisecond();
	}

	public int getBuildId() {
		return buildId;
	}

	public void setBuildId(int buildId) {
		this.buildId = buildId;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public long getRefreshTime() {
		return refreshTime;
	}

	public void setRefreshTime(long refreshTime) {
		this.refreshTime = refreshTime;
	}
	
}
