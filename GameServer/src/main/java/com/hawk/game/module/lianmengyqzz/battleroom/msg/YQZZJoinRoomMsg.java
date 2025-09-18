package com.hawk.game.module.lianmengyqzz.battleroom.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.game.module.lianmengyqzz.battleroom.YQZZBattleRoom;
import com.hawk.game.module.lianmengyqzz.battleroom.player.IYQZZPlayer;

/** 最终会操作玩家数据. 尤其是队列和兵的数据. 这一步要保持单线程
 * 
 * @author lwt
 * @date 2018年12月27日 */
public class YQZZJoinRoomMsg extends HawkMsg {
	private YQZZBattleRoom battleRoom;
	private IYQZZPlayer player;

	private YQZZJoinRoomMsg() {
	}

	public static YQZZJoinRoomMsg valueOf(YQZZBattleRoom room, IYQZZPlayer player) {
		YQZZJoinRoomMsg msg = new YQZZJoinRoomMsg();
		msg.battleRoom = room;
		msg.player = player;
		return msg;
	}

	public YQZZBattleRoom getBattleRoom() {
		return battleRoom;
	}

	public void setBattleRoom(YQZZBattleRoom battleRoom) {
		this.battleRoom = battleRoom;
	}

	public IYQZZPlayer getPlayer() {
		return player;
	}

	public void setPlayer(IYQZZPlayer player) {
		this.player = player;
	}

}
