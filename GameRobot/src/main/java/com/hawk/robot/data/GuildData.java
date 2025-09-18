package com.hawk.robot.data;

import com.hawk.game.protocol.Const.GuildAuthority;
import com.hawk.game.protocol.GuildManager.GetGuildInfoResp;
import com.hawk.game.protocol.GuildManager.HPGuildInfoSync;

/**
 * 联盟相关数据
 * 
 * @author lating
 *
 */
public class GuildData {
	/**
	 * 机器人信息(上层数据)
	 */
	protected GameRobotData robotData;
	/**
	 * 联盟Id
	 */
	protected String guildId;
	/**
	 * 联盟信息
	 */
	protected GetGuildInfoResp guildInfo = null;
	/**
	 * 联盟同步信息
	 */
	protected HPGuildInfoSync guildInfoSync = null;
	
	private boolean moveSee = false;
	
	public GuildData(GameRobotData gameRobotData) {
		robotData = gameRobotData;
	}

	public GameRobotData getRobotData() {
		return robotData;
	}

	public String getGuildId() {
		return guildId;
	}
	
	public HPGuildInfoSync getGuildInfoSync() {
		return guildInfoSync;
	}
	
	/**
	 * 是否四阶以上成员
	 * @return
	 */
	public boolean isLeader(){
		if(guildInfoSync == null){
			return false;
		}
		return guildInfoSync.getGuildAuthority() >= GuildAuthority.L4_VALUE;
	}
	
	/**
	 * 刷新联盟信息
	 * @param guildInfo
	 */
	public void refreshGuildInfo(GetGuildInfoResp guildInfo) {
		this.guildInfo = guildInfo;
		if(guildInfo != null){
			guildId = guildInfo.getId();
		}
	}
	
	/**
	 * 刷新联盟同步信息
	 * @param guildInfoSync
	 */
	public void refreshGuildInfoSync(HPGuildInfoSync guildInfoSync){
		this.guildInfoSync = guildInfoSync;
		if(guildInfoSync!=null){
			guildId = guildInfoSync.getGuildId();
		}
	}
	
	public void clearGuildData(){
		this.guildId = null;
		this.guildInfo = null;
		this.guildInfoSync = null;
	}

	public boolean isMoveSee() {
		return moveSee;
	}

	public void setMoveSee(boolean moveSee) {
		this.moveSee = moveSee;
	}
	
}
