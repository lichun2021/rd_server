package com.hawk.game.lianmengcyb.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.game.lianmengcyb.CYBORGBattleRoom;
import com.hawk.game.lianmengcyb.player.ICYBORGPlayer;


/** 最终会操作玩家数据. 尤其是队列和兵的数据. 这一步要保持单线程
 * 
 * @author lwt
 * @date 2018年12月27日 */
public class CYBORGJoinRoomMsg extends HawkMsg {
	private CYBORGBattleRoom battleRoom;
	private ICYBORGPlayer player;

	private CYBORGJoinRoomMsg() {
	}

	public static CYBORGJoinRoomMsg valueOf(CYBORGBattleRoom room, ICYBORGPlayer player) {
		CYBORGJoinRoomMsg msg = new CYBORGJoinRoomMsg();
		msg.battleRoom = room;
		msg.player = player;
		return msg;
	}

	public CYBORGBattleRoom getBattleRoom() {
		return battleRoom;
	}

	public void setBattleRoom(CYBORGBattleRoom battleRoom) {
		this.battleRoom = battleRoom;
	}

	public ICYBORGPlayer getPlayer() {
		return player;
	}

	public void setPlayer(ICYBORGPlayer player) {
		this.player = player;
	}

}
