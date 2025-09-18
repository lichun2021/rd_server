package com.hawk.game.lianmengstarwars.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.game.lianmengstarwars.SWBattleRoom;
import com.hawk.game.lianmengstarwars.player.ISWPlayer;



/** 最终会操作玩家数据. 尤其是队列和兵的数据. 这一步要保持单线程
 * 
 * @author lwt
 * @date 2018年12月27日 */
public class SWJoinRoomMsg extends HawkMsg {
	private SWBattleRoom battleRoom;
	private ISWPlayer player;

	private SWJoinRoomMsg() {
	}

	public static SWJoinRoomMsg valueOf(SWBattleRoom room, ISWPlayer player) {
		SWJoinRoomMsg msg = new SWJoinRoomMsg();
		msg.battleRoom = room;
		msg.player = player;
		return msg;
	}

	public SWBattleRoom getBattleRoom() {
		return battleRoom;
	}

	public void setBattleRoom(SWBattleRoom battleRoom) {
		this.battleRoom = battleRoom;
	}

	public ISWPlayer getPlayer() {
		return player;
	}

	public void setPlayer(ISWPlayer player) {
		this.player = player;
	}

}
