package com.hawk.game.module.material.data;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.protocol.MaterialTransport.PBMTPlayer;
import com.hawk.game.protocol.MaterialTransport.PBMTTruckBattleRecord;

/**
 * 抢夺记录 
 * @author lwt
 * @date 2025年5月28日
 */
public class MTTruckBattleRecord {
	private long battleTime;
	private List<MTMember> attacker = new ArrayList<>();
	private String battleId; // 邮件id
	private boolean win;
	private String rewardGet;

	public PBMTTruckBattleRecord toPBObj() {
		PBMTTruckBattleRecord.Builder builder = PBMTTruckBattleRecord.newBuilder();
		builder.setBattleTime(battleTime);
		builder.setMailUUID(battleId);
		builder.setWin(win);
		builder.setRewardGet(rewardGet);
		for (MTMember atk : attacker) {
			builder.addAtker(atk.toPBObj());
		}

		return builder.build();
	}

	public void mergeFrom(PBMTTruckBattleRecord obj) {
		this.battleTime = obj.getBattleTime();
		this.battleId = obj.getMailUUID();
		this.win = obj.getWin();
		this.rewardGet = obj.getRewardGet();
		List<MTMember> attacker = new ArrayList<>();
		for( PBMTPlayer atk : obj.getAtkerList()){
			MTMember atker = new MTMember();
			atker.mergeFrom(atk);
			attacker.add(atker);
		}
		this.attacker = attacker;
	}

	public List<MTMember> getAttacker() {
		return attacker;
	}

	public void setAttacker(List<MTMember> attacker) {
		this.attacker = attacker;
	}

	public String getBattleId() {
		return battleId;
	}

	public void setBattleId(String battleId) {
		this.battleId = battleId;
	}

	public boolean isWin() {
		return win;
	}

	public void setWin(boolean win) {
		this.win = win;
	}

	public String getRewardGet() {
		return rewardGet;
	}

	public void setRewardGet(String rewardGet) {
		this.rewardGet = rewardGet;
	}

	public boolean isAttacker(String playerId) {
		for (MTMember m : attacker) {
			if (m.getPlayerId().equals(playerId)) {
				return true;
			}
		}
		return false;
	}

	public long getBattleTime() {
		return battleTime;
	}

	public void setBattleTime(long battleTime) {
		this.battleTime = battleTime;
	}

}
