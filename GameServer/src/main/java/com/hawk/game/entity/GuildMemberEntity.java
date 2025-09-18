package com.hawk.game.entity;

import org.hawk.annotation.IndexProp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.db.HawkDBEntity;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

/**
 * 联盟成员实体
 *
 * @author shadow
 *
 */
@Entity
@Table(name = "guild_member")
public class GuildMemberEntity extends HawkDBEntity {
	@Id
	@Column(name = "playerId", unique = true, nullable = false)
    @IndexProp(id = 1)
	private String playerId;

	@Column(name = "playerName", nullable = false)
    @IndexProp(id = 2)
	private String playerName;

	@Column(name = "power")
    @IndexProp(id = 3)
	private long power;

	@Column(name = "killCount")
    @IndexProp(id = 4)
	private long killCount;

	@Column(name = "guildId")
    @IndexProp(id = 5)
	private String guildId;

	@Column(name = "authority", nullable = false)
    @IndexProp(id = 6)
	private int authority;
	
	
	@Column(name = "officeId", nullable = false)
    @IndexProp(id = 7)
	private int officeId;

	@Column(name = "quitGuildTime")
    @IndexProp(id = 8)
	private long quitGuildTime = 0;

	@Column(name = "joinGuildTime")
    @IndexProp(id = 9)
	private long joinGuildTime = 0;

	/** 普通捐献次数 */
	@Column(name = "normalDonateTimes")
    @IndexProp(id = 10)
	private int normalDonateTimes = 0;

	/** 水晶捐献次数 */
	@Column(name = "crystalDonateTimes")
    @IndexProp(id = 11)
	private int crystalDonateTimes = 0;

	/** 普通捐献重置次数 */
	@Column(name = "donateResetTimes")
    @IndexProp(id = 12)
	private int donateResetTimes = 0;

	/** 下次普通捐献次数恢复时间 */
	@Column(name = "nextDonateAddTime")
    @IndexProp(id = 13)
	private long nextDonateAddTime = 0;

	/** 联盟捐献相关刷新日期 */
	@Column(name = "donateDayOfYear")
    @IndexProp(id = 14)
	private int donateDayOfYear;

	/** 联盟礼物刷新日期 */
	@Column(name = "lastRefrashBigGift")
    @IndexProp(id = 15)
	private long lastRefrashBigGift;
	
	/** 加入联盟次数*/
	@Column(name = "joinGuildTimes")
    @IndexProp(id = 16)
	private int joinGuildTimes;
	
	/** 领地解锁邮件发送次数*/
	@Column(name = "manorUnlockTimes")
    @IndexProp(id = 17)
	private int manorUnlockTimes;
	
	/** 已领取联盟任务id*/
	@Column(name = "rewaredTaskIds")
    @IndexProp(id = 18)
	private String rewaredTaskIds;
	
	/** 联盟任务重置时间*/
	@Column(name = "taskResetTime")
    @IndexProp(id = 19)
	private long taskResetTime;
	
	/** 已签到次数*/
	@Column(name = "signTimes")
    @IndexProp(id = 20)
	private int signTimes;
	
	/** 上次签到时间*/
	@Column(name = "lastSingTime")
    @IndexProp(id = 21)
	private long lastSingTime;
	
	@Column(name = "logoutTime", nullable = true)
    @IndexProp(id = 22)
	private long logoutTime;

	@Column(name = "createTime", nullable = false)
    @IndexProp(id = 23)
	protected long createTime = 0;

	@Column(name = "updateTime")
    @IndexProp(id = 24)
	protected long updateTime = 0;

	@Column(name = "invalid")
    @IndexProp(id = 25)
	protected boolean invalid;
	
	@Column(name = "noArmyPower")
    @IndexProp(id = 26)
	private long noArmyPower;
	
	@Column(name = "dragonAwardTime")
    @IndexProp(id = 27)
	private long dragonAwardTime;
	
	
	@Transient
	private List<Integer> rewardedTaskList;

	public GuildMemberEntity() {
	}

	public String getPlayerId() {
		return playerId;
	}

	protected void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public String getPlayerName() {
		return playerName;
	}

	protected void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public long getPower() {
		return power;
	}

	protected void setPower(long power) {
		this.power = power;
	}

	public long getKillCount() {
		return killCount;
	}

	protected void setKillCount(long killCount) {
		this.killCount = killCount;
	}

	public String getGuildId() {
		return guildId;
	}

	protected void setGuildId(String guildId) {
		this.guildId = guildId;
	}

	public int getAuthority() {
		return authority;
	}

	protected void setAuthority(int authority) {
		this.authority = authority;
	}

	public int getOfficeId() {
		return officeId;
	}

	public void setOfficeId(int officeId) {
		this.officeId = officeId;
	}

	public long getQuitGuildTime() {
		return quitGuildTime;
	}

	protected void setQuitGuildTime(long quitGuildTime) {
		this.quitGuildTime = quitGuildTime;
	}

	public long getJoinGuildTime() {
		return joinGuildTime;
	}

	public void setJoinGuildTime(long joinGuildTime) {
		this.joinGuildTime = joinGuildTime;
	}

	public int getNormalDonateTimes() {
		return normalDonateTimes;
	}

	public void setNormalDonateTimes(int normalDonateTimes) {
		this.normalDonateTimes = normalDonateTimes;
	}

	public int getCrystalDonateTimes() {
		return crystalDonateTimes;
	}

	public void setCrystalDonateTimes(int crystalDonateTimes) {
		this.crystalDonateTimes = crystalDonateTimes;
	}

	public int getDonateResetTimes() {
		return donateResetTimes;
	}

	public void setDonateResetTimes(int donateResetTimes) {
		this.donateResetTimes = donateResetTimes;
	}

	public long getNextDonateAddTime() {
		return nextDonateAddTime;
	}

	public void setNextDonateAddTime(long nextDonateAddTime) {
		this.nextDonateAddTime = nextDonateAddTime;
	}

	public int getDonateDayOfYear() {
		return donateDayOfYear;
	}

	public void setDonateDayOfYear(int donateDayOfYear) {
		this.donateDayOfYear = donateDayOfYear;
	}

	@Override
	public long getCreateTime() {
		return createTime;
	}

	@Override
	protected void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	@Override
	public long getUpdateTime() {
		return updateTime;
	}

	@Override
	protected void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

	@Override
	public boolean isInvalid() {
		return invalid;
	}

	@Override
	public void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}

	public long getLastRefrashBigGift() {
		return lastRefrashBigGift;
	}

	public void setLastRefrashBigGift(long lastRefrashBigGift) {
		this.lastRefrashBigGift = lastRefrashBigGift;
	}
	
	public int getJoinGuildTimes() {
		return joinGuildTimes;
	}

	public void setJoinGuildTimes(int joinGuildTimes) {
		this.joinGuildTimes = joinGuildTimes;
	}

	public int getManorUnlockTimes() {
		return manorUnlockTimes;
	}

	public void setManorUnlockTimes(int manorUnlockTimes) {
		this.manorUnlockTimes = manorUnlockTimes;
	}

	@Override
	public String getPrimaryKey() {
		return this.playerId;
	}

	public long getLogoutTime() {
		return logoutTime;
	}

	public void setLogoutTime(long logoutTime) {
		this.logoutTime = logoutTime;
	}
	
	public long getTaskResetTime() {
		return taskResetTime;
	}

	public void setTaskResetTime(long taskResetTime) {
		this.taskResetTime = taskResetTime;
	}

	public int getSignTimes() {
		return signTimes;
	}

	public void setSignTimes(int signTimes) {
		this.signTimes = signTimes;
	}

	public long getLastSingTime() {
		return lastSingTime;
	}

	public void setLastSingTime(long lastSingTime) {
		this.lastSingTime = lastSingTime;
	}

	public List<Integer> getRewardedTaskList() {
		return rewardedTaskList;
	}
	
	public void addRewardedTask(List<Integer> taskIds){
		rewardedTaskList.addAll(taskIds);
		notifyUpdate();
	}
	
	public void resetRewardedTask(){
		rewardedTaskList = new ArrayList<>();
		this.taskResetTime = HawkTime.getMillisecond();
		notifyUpdate();
	}

	public void setRewaredTaskIds(String rewaredTaskIds) {
		this.rewaredTaskIds = rewaredTaskIds;
	}

	@Override
	public void beforeWrite() {
		if (rewardedTaskList.isEmpty()) {
			this.rewaredTaskIds = "";
			return;
		}
		StringBuilder sb = new StringBuilder();
		for (Integer taskId : rewardedTaskList) {
			sb.append(",").append(taskId);
		}
		if(sb.length() > 0){
			sb.replace(0, 1, "");
		}
		this.rewaredTaskIds = sb.toString();
		super.beforeWrite();
	}

	@Override
	public void afterRead() {
		rewardedTaskList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(this.rewaredTaskIds)) {
			for (String info : this.rewaredTaskIds.split(",")) {
				rewardedTaskList.add(Integer.valueOf(info));
			}
		}
		
		super.afterRead();
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		throw new UnsupportedOperationException();
	}
	
	public String getOwnerKey() {
		return playerId;
	}
	
	public long getNoArmyPower() {
		return noArmyPower;
	}

	public void setNoArmyPower(long noArmyPower) {
		this.noArmyPower = noArmyPower;
	}

	public long getDragonAwardTime() {
		return dragonAwardTime;
	}
	
	public void setDragonAwardTime(long dragonAwardTime) {
		this.dragonAwardTime = dragonAwardTime;
	}
}
