package com.hawk.game.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.annotation.IndexProp;
import org.hawk.db.HawkDBEntity;

import com.hawk.game.item.NationMissionItem;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 国家任务
 * @author Golden
 *
 */
@Entity
@Table(name = "nation_mission")
public class NationMissionEntity extends HawkDBEntity {

	@Id
	@Column(name = "playerId")
    @IndexProp(id = 1)
	private String playerId;
	
	/**
	 * 任务
	 */
	@Column(name = "missionStr", nullable = false)
    @IndexProp(id = 2)
	private String missionStr;
	
	/**
	 * 类型 0国家版 1个人版
	 */
	@Column(name = "type", nullable = false)
    @IndexProp(id = 3)
	private int type;
	
	/**
	 * 剩余可以完成次数
	 */
	@Column(name = "remainTimes", nullable = false)
    @IndexProp(id = 4)
	private int remainTimes;
	
	/**
	 * 时间标记(用于跨天刷新)
	 */
	@Column(name = "timeMark", nullable = false)
    @IndexProp(id = 5)
	private long timeMark;
	
	/**
	 * 建筑等级标记(用于感知建筑升级了)
	 */
	@Column(name = "constructionLevelMark", nullable = false)
    @IndexProp(id = 6)
	private int constructionLevelMark;
	
	
	@Column(name = "weekMark", nullable = false)
    @IndexProp(id = 7)
	private int weekMark;
	
	@Column(name = "tech", nullable = false)
    @IndexProp(id = 8)
	private int tech;
	
	@Column(name = "giveupTime", nullable = false)
    @IndexProp(id = 9)
	private long giveupTime;
	
	@Column(name = "createTime")
    @IndexProp(id = 10)
	private long createTime;

	@Column(name = "updateTime")
    @IndexProp(id = 11)
	private long updateTime;

	@Column(name = "invalid")
    @IndexProp(id = 12)
	private boolean invalid;

	@Transient
	private NationMissionItem mission;
	
	@Override
	public void beforeWrite() {
		if (mission == null) {
			missionStr = "";
		} else {
			missionStr = SerializeHelper.toSerializeString(mission, SerializeHelper.ATTRIBUTE_SPLIT);
		}
	}

	@Override
	public void afterRead() {
		mission = SerializeHelper.getValue(NationMissionItem.class, missionStr, SerializeHelper.ATTRIBUTE_SPLIT);
	}

	@Override
	public String getPrimaryKey() {
		return this.playerId;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		throw new UnsupportedOperationException();
	}

	public String getOwnerKey() {
		return playerId;
	}
	
	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public String getMissionStr() {
		return missionStr;
	}

	public void setMissionStr(String missionStr) {
		this.missionStr = missionStr;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getRemainTimes() {
		return remainTimes;
	}

	public void setRemainTimes(int remainTimes) {
		this.remainTimes = remainTimes;
	}

	public void addRemainTimes(int times) {
		this.remainTimes += times;
		notifyUpdate();
	}
	
	public void reduceRemainTimes() {
		this.remainTimes = Math.max(0, this.remainTimes - 1);
		notifyUpdate();
	}
	
	public long getTimeMark() {
		return timeMark;
	}

	public void setTimeMark(long timeMark) {
		this.timeMark = timeMark;
	}

	public int getConstructionLevelMark() {
		return constructionLevelMark;
	}

	public void setConstructionLevelMark(int constructionLevelMark) {
		this.constructionLevelMark = constructionLevelMark;
	}

	public long getGiveupTime() {
		return giveupTime;
	}

	public void setGiveupTime(long giveupTime) {
		this.giveupTime = giveupTime;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

	public boolean isInvalid() {
		return invalid;
	}

	public void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}

	public NationMissionItem getMission() {
		return mission;
	}

	public void setMission(NationMissionItem mission) {
		this.mission = mission;
	}

	public int getWeekMark() {
		return weekMark;
	}

	public void setWeekMark(int weekMark) {
		this.weekMark = weekMark;
	}

	public int getTech() {
		return tech;
	}

	public void setTech(int tech) {
		this.tech = tech;
	}

	public void addTech(int add) {
		this.tech += add;
	}
}
