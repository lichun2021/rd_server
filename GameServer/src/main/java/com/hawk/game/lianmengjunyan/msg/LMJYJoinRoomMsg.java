package com.hawk.game.lianmengjunyan.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.game.lianmengjunyan.LMJYBattleRoom;
import com.hawk.game.lianmengjunyan.player.ILMJYPlayer;

/** 最终会操作玩家数据. 尤其是队列和兵的数据. 这一步要保持单线程
 * 
 * @author lwt
 * @date 2018年12月27日 */
public class LMJYJoinRoomMsg extends HawkMsg {
	private LMJYBattleRoom battleRoom;
	private ILMJYPlayer player;

	private LMJYJoinRoomMsg() {
	}

	public static LMJYJoinRoomMsg valueOf(LMJYBattleRoom room, ILMJYPlayer player) {
		LMJYJoinRoomMsg msg = new LMJYJoinRoomMsg();
		msg.battleRoom = room;
		msg.player = player;
		return msg;
	}

	public LMJYBattleRoom getBattleRoom() {
		return battleRoom;
	}

	public void setBattleRoom(LMJYBattleRoom battleRoom) {
		this.battleRoom = battleRoom;
	}

	public ILMJYPlayer getPlayer() {
		return player;
	}

	public void setPlayer(ILMJYPlayer player) {
		this.player = player;
	}

}
