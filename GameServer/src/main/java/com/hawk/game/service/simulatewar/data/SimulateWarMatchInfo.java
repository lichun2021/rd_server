package com.hawk.game.service.simulatewar.data;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * 
 * @author jm
 *
 */
public class SimulateWarMatchInfo {
	/**
	 * 主区服ID
	 */
	String mainServerId;
	/**
	 * 主公会ID
	 */
	String mainGuildId;
	/**
	 * 从区服ID
	 */
	String slaveServerId;
	/**
	 * 从公会ID
	 */
	String slaveGuildId;
	
	static final int BATTLE_FLAG = 0x1;
	static final int REWARD_FLAG = 0x2;
	private int state;
	
	public String getMainServerId() {
		return mainServerId;
	}
	public void setMainServerId(String mainServerId) {
		this.mainServerId = mainServerId;
	}
	public String getMainGuildId() {
		return mainGuildId;
	}
	public void setMainGuildId(String mainGuildId) {
		this.mainGuildId = mainGuildId;
	}
	public String getSlaveServerId() {
		return slaveServerId;
	}
	public void setSlaveServerId(String slaveServerId) {
		this.slaveServerId = slaveServerId;
	}
	public String getSlaveGuildId() {
		return slaveGuildId;
	}
	public void setSlaveGuildId(String slaveGuildId) {
		this.slaveGuildId = slaveGuildId;
	}
	
	public String getEnemyGuildId(String guildId) {
		if (mainGuildId.equals(guildId)) {
			return slaveGuildId;
		} else {
			return mainGuildId;
		}
	}
	
	/**
	 * 获取区服ID
	 * @param guildId
	 * @return
	 */
	public String getServerId(String guildId) {
		if (mainGuildId.equals(guildId)) {
			return mainServerId;
		} else {
			return slaveServerId;
		}
	}
	
	/**
	 * 是否是主
	 * @param guildId
	 * @return
	 */
	public boolean isMain(String guildId) {
		return mainGuildId.equals(guildId);
	}
	
	@JSONField(serialize = false)
	public boolean isBattleFinish() {
		return (state & BATTLE_FLAG) > 0;
	}
	
	public void finishBattle() {
		this.state = this.state | BATTLE_FLAG;
	}
	
	@JSONField(serialize = false)
	public boolean isReward() {
		return (state & REWARD_FLAG) > 0;
	}
	
	public void finishReward() {
		this.state = this.state | REWARD_FLAG;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	} 
}
