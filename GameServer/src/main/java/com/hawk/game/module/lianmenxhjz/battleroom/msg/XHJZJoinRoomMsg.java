package com.hawk.game.module.lianmenxhjz.battleroom.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.game.module.lianmenxhjz.battleroom.XHJZBattleRoom;
import com.hawk.game.module.lianmenxhjz.battleroom.player.IXHJZPlayer;



/** 最终会操作玩家数据. 尤其是队列和兵的数据. 这一步要保持单线程
 * 
 * @author lwt
 * @date 2018年12月27日 */
public class XHJZJoinRoomMsg extends HawkMsg {
	private XHJZBattleRoom battleRoom;
	private IXHJZPlayer player;

	private XHJZJoinRoomMsg() {
	}

	public static XHJZJoinRoomMsg valueOf(XHJZBattleRoom room, IXHJZPlayer player) {
		XHJZJoinRoomMsg msg = new XHJZJoinRoomMsg();
		msg.battleRoom = room;
		msg.player = player;
		return msg;
	}

	public XHJZBattleRoom getBattleRoom() {
		return battleRoom;
	}

	public void setBattleRoom(XHJZBattleRoom battleRoom) {
		this.battleRoom = battleRoom;
	}

	public IXHJZPlayer getPlayer() {
		return player;
	}

	public void setPlayer(IXHJZPlayer player) {
		this.player = player;
	}

}
