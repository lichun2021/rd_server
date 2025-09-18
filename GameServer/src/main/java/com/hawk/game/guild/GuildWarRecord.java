package com.hawk.game.guild;

import java.util.ArrayList;
import java.util.List;

import org.hawk.os.HawkTime;

import com.hawk.game.protocol.GuildWar.GuildWarMarchType;

/**
 * 联盟战争记录
 * 
 * @author lating
 * @reviewer hawk
 *
 */
public class GuildWarRecord implements Cloneable {
	/**
	 * 攻击方信息
	 */
	private String attPlayerId;
	private String attPlayerName;
	private String attGuildId;
	private String attGuildName;
	private GuildWarMarchType atkMarchType;
	private List<String> atkPlayerIds;
	
	/**
	 * 防守方信息
	 */
	private String defPlayerId;
	private String defPlayerName;
	private String defGuildId;
	private String defGuildName;
	private GuildWarMarchType defMarchType;
	private List<String> defPlayerIds;
	
	/**
	 * 进攻方胜利次数
	 */
	private int winTimes;
	/**
	 * 战争结束时间
	 */
	private long warTime;

	/**
	 * 默认构造
	 */
	public GuildWarRecord() {
		this.warTime = HawkTime.getMillisecond();
	}

	/**
	 * 克隆
	 */
	public GuildWarRecord clone() {
		GuildWarRecord guildWar = new GuildWarRecord();

		guildWar.setAttPlayerId(attPlayerId);
		guildWar.setAttPlayerName(attPlayerName);
		guildWar.setAttGuildId(attGuildId);
		guildWar.setAttGuildName(attGuildName);
		guildWar.setAtkMarchType(atkMarchType);
		List<String> atkPlayerIds = new ArrayList<>();
		defPlayerIds.addAll(this.atkPlayerIds);
		guildWar.setAtkPlayerIds(atkPlayerIds);
		
		guildWar.setDefPlayerId(defPlayerId);
		guildWar.setDefPlayerName(defPlayerName);
		guildWar.setDefGuildId(defGuildId);
		guildWar.setDefGuildName(defGuildName);
		guildWar.setDefMarchType(defMarchType);
		List<String> defPlayerIds = new ArrayList<>();
		defPlayerIds.addAll(this.defPlayerIds);
		guildWar.setDefPlayerIds(defPlayerIds);
		
		guildWar.setWinTimes(winTimes);
		guildWar.setWarTime(HawkTime.getMillisecond());

		return guildWar;
	}

	public boolean isContinuous(GuildWarRecord guildWar) {
		if (!attPlayerId.equals(guildWar.getAttPlayerId()) || 
			!defPlayerId.equals(guildWar.getDefPlayerId()) ||
			
			!attPlayerName.equals(guildWar.getAttPlayerName()) ||
			!defPlayerName.equals(guildWar.getDefPlayerName()) ||
			
			!attGuildName.equals(guildWar.getAttGuildName()) || 
			!defGuildName.equals(guildWar.getDefGuildName()) ||
			
			!attGuildId.equals(guildWar.getAttGuildId()) || 
			!defGuildId.equals(guildWar.getDefGuildId()) ||
			
			atkPlayerIds.size() != guildWar.getAtkPlayerIds().size() ||
			defPlayerIds.size() != guildWar.getDefPlayerIds().size() ||
			
			(winTimes * guildWar.getWinTimes() <= 0)) {
			return false;
		}
		
		for (String atkPlayerId : atkPlayerIds) {
			if (!guildWar.getAtkPlayerIds().contains(atkPlayerId)) {
				return false;
			}
		}
		
		for (String defPlayerId : defPlayerIds) {
			if (!guildWar.getDefPlayerIds().contains(defPlayerId)) {
				return false;
			}
		}
		
		return true;
	}

	public String getAttPlayerId() {
		return attPlayerId;
	}

	public void setAttPlayerId(String attPlayerId) {
		this.attPlayerId = attPlayerId;
	}

	public String getAttPlayerName() {
		return attPlayerName;
	}

	public void setAttPlayerName(String attPlayerName) {
		this.attPlayerName = attPlayerName;
	}

	public String getAttGuildId() {
		return attGuildId;
	}

	public void setAttGuildId(String attGuildId) {
		this.attGuildId = attGuildId;
	}

	public String getAttGuildName() {
		return attGuildName;
	}

	public void setAttGuildName(String attGuildName) {
		this.attGuildName = attGuildName;
	}

	public String getDefPlayerId() {
		return defPlayerId;
	}

	public void setDefPlayerId(String defPlayerId) {
		this.defPlayerId = defPlayerId;
	}

	public String getDefPlayerName() {
		return defPlayerName;
	}

	public void setDefPlayerName(String defPlayerName) {
		this.defPlayerName = defPlayerName;
	}

	public String getDefGuildId() {
		return defGuildId;
	}

	public void setDefGuildId(String defGuildId) {
		this.defGuildId = defGuildId;
	}

	public String getDefGuildName() {
		return defGuildName;
	}

	public void setDefGuildName(String defGuildName) {
		this.defGuildName = defGuildName;
	}

	public int getWinTimes() {
		return winTimes;
	}

	public void setWinTimes(int winTimes) {
		this.winTimes = winTimes;
	}

	public long getWarTime() {
		return warTime;
	}

	public void setWarTime(long warTime) {
		this.warTime = warTime;
	}

	public void addWinTimes(int count) {
		if (winTimes < 0) {
			winTimes -= count;
		} else {
			winTimes += count;
		}
	}
	
	public GuildWarMarchType getAtkMarchType() {
		return atkMarchType;
	}

	public void setAtkMarchType(GuildWarMarchType atkMarchType) {
		this.atkMarchType = atkMarchType;
	}

	public GuildWarMarchType getDefMarchType() {
		return defMarchType;
	}

	public void setDefMarchType(GuildWarMarchType defMarchType) {
		this.defMarchType = defMarchType;
	}

	public List<String> getAtkPlayerIds() {
		return atkPlayerIds;
	}

	public void setAtkPlayerIds(List<String> atkPlayerIds) {
		this.atkPlayerIds = atkPlayerIds;
	}

	public List<String> getDefPlayerIds() {
		return defPlayerIds;
	}

	public void setDefPlayerIds(List<String> defPlayerIds) {
		this.defPlayerIds = defPlayerIds;
	}
}
