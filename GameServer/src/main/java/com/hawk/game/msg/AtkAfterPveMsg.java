package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.protocol.World.WorldMarchType;

/**
 * 消息 - pve攻方玩家战后处理
 * 
 * @author golden
 *
 */
public class AtkAfterPveMsg extends HawkMsg {

	BattleOutcome out;

	WorldMarchType marchType;
	
	int level;
	
	public AtkAfterPveMsg(BattleOutcome out, WorldMarchType marchType, int level) {
		this.out = out;
		this.marchType = marchType;
		this.level = level;
	}

	public BattleOutcome getOut() {
		return out;
	}

	public void setOut(BattleOutcome out) {
		this.out = out;
	}

	public WorldMarchType getMarchType() {
		return marchType;
	}

	public void setMarchType(WorldMarchType marchType) {
		this.marchType = marchType;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public static AtkAfterPveMsg valueOf(BattleOutcome out, WorldMarchType marchType, int level) {
		return new AtkAfterPveMsg(out, marchType, level);
	}
}
