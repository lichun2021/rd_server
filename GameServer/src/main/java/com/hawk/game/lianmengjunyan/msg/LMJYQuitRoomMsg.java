package com.hawk.game.lianmengjunyan.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.game.lianmengjunyan.LMJYBattleRoom;
import com.hawk.game.lianmengjunyan.player.ILMJYPlayer;

/** 最终会操作玩家数据. 尤其是队列和兵的数据. 这一步要保持单线程
 * 
 * @author lwt
 * @date 2018年12月27日 */
public class LMJYQuitRoomMsg extends HawkMsg {
	public enum QuitReason {
		/** 准备时间结束 */
		PREPAROVER,
		/** 烧穿 */
		FIREOUT,
		/** 主动退出 */
		LEAVE;
	}

	private LMJYBattleRoom battleRoom;
	private ILMJYPlayer player;
	private QuitReason reason;

	private LMJYQuitRoomMsg() {
	}

	public static LMJYQuitRoomMsg valueOf(LMJYBattleRoom room, ILMJYPlayer player, QuitReason reason) {
		LMJYQuitRoomMsg msg = new LMJYQuitRoomMsg();
		msg.battleRoom = room;
		msg.player = player;
		msg.reason = reason;
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

	public QuitReason getReason() {
		return reason;
	}

	public void setReason(QuitReason reason) {
		this.reason = reason;
	}

}
