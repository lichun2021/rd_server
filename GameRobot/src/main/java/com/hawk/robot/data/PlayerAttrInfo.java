package com.hawk.robot.data;

import org.hawk.os.HawkOSOperator;

import com.hawk.game.protocol.Consume.SyncAttrInfo;
import com.hawk.game.protocol.Player.PlayerInfo;
import com.hawk.game.protocol.Reward.HPPlayerReward;
import com.hawk.game.protocol.Reward.RewardInfo;
import com.hawk.robot.RobotLog;

public class PlayerAttrInfo {
	
	private String playerId;
	
	private String name;
	
	private int vit;
	
	private int level;
	
	private int vipLevel;
	
	private int militaryLevel;
	
	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getVit() {
		return vit;
	}

	public void setVit(int vit) {
		this.vit = vit;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getVipLevel() {
		return vipLevel;
	}

	public void setVipLevel(int vipLevel) {
		this.vipLevel = vipLevel;
	}
	
	public int getMilitaryLevel() {
		return militaryLevel;
	}

	public void setMilitaryLevel(int militaryLevel) {
		this.militaryLevel = militaryLevel;
	}
	
	public void reset(HPPlayerReward rewardInfo) {
		RewardInfo rewards = rewardInfo.getRewards();
		if (rewards.getVit() > 0) {
			this.vit = rewards.getVit();
			RobotLog.cityPrintln("reset vit by rewardInfo, playerId: {}, player vit: {}", playerId, rewards.getVit());
		}
		
		if (rewards.getLevel() > 0) {
			this.level = rewards.getLevel();
		}
		
		if (rewards.getVipLevel() > 0) {
			this.vipLevel = rewards.getVipLevel();
		}
	}
	
	public void reset(PlayerInfo playerInfo) {
		if (!HawkOSOperator.isEmptyString(playerInfo.getName())) {
			this.name = playerInfo.getName();
		}
		
		if (playerInfo.getVit() > 0) {
			this.vit = playerInfo.getVit();
			RobotLog.cityPrintln("reset vit by playerInfo, playerId: {}, player vit: {}", playerId, playerInfo.getVit());
		}
		
		if (playerInfo.getLevel() > 0) {
			this.level = playerInfo.getLevel();
		}

		if (playerInfo.getVipLevel() > 0) {
			this.vipLevel = playerInfo.getVipLevel();
		}
		
		if (playerInfo.getMilitaryRankLvl() > 0) {
			this.militaryLevel = playerInfo.getMilitaryRankLvl();
		}
	}
	
	public void reset(SyncAttrInfo consumeAfterInfo) {
		if (consumeAfterInfo.getVit() > 0) {
			this.vit = consumeAfterInfo.getVit();
			RobotLog.cityPrintln("reset vit by consumeAfterInfo, playerId: {}, player vit: {}", playerId, consumeAfterInfo.getVit());
		}
	}

}
