package com.hawk.game.module.dayazhizhan.battleroom.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.game.module.dayazhizhan.battleroom.DYZZBattleRoom;
import com.hawk.game.module.dayazhizhan.battleroom.player.IDYZZPlayer;


/** 最终会操作玩家数据. 尤其是队列和兵的数据. 这一步要保持单线程
 * 
 * @author lwt
 * @date 2018年12月27日 */
public class DYZZJoinRoomMsg extends HawkMsg {
	private DYZZBattleRoom battleRoom;
	private IDYZZPlayer player;

	private DYZZJoinRoomMsg() {
	}

	public static DYZZJoinRoomMsg valueOf(DYZZBattleRoom room, IDYZZPlayer player) {
		DYZZJoinRoomMsg msg = new DYZZJoinRoomMsg();
		msg.battleRoom = room;
		msg.player = player;
		return msg;
	}

	public DYZZBattleRoom getBattleRoom() {
		return battleRoom;
	}

	public void setBattleRoom(DYZZBattleRoom battleRoom) {
		this.battleRoom = battleRoom;
	}

	public IDYZZPlayer getPlayer() {
		return player;
	}

	public void setPlayer(IDYZZPlayer player) {
		this.player = player;
	}

}
