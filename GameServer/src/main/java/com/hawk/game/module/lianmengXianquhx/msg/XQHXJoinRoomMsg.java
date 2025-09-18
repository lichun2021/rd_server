package com.hawk.game.module.lianmengXianquhx.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.game.module.lianmengXianquhx.XQHXBattleRoom;
import com.hawk.game.module.lianmengXianquhx.player.IXQHXPlayer;


/** 最终会操作玩家数据. 尤其是队列和兵的数据. 这一步要保持单线程
 * 
 * @author lwt
 * @date 2018年12月27日 */
public class XQHXJoinRoomMsg extends HawkMsg {
	private XQHXBattleRoom battleRoom;
	private IXQHXPlayer player;

	private XQHXJoinRoomMsg() {
	}

	public static XQHXJoinRoomMsg valueOf(XQHXBattleRoom room, IXQHXPlayer player) {
		XQHXJoinRoomMsg msg = new XQHXJoinRoomMsg();
		msg.battleRoom = room;
		msg.player = player;
		return msg;
	}

	public XQHXBattleRoom getBattleRoom() {
		return battleRoom;
	}

	public void setBattleRoom(XQHXBattleRoom battleRoom) {
		this.battleRoom = battleRoom;
	}

	public IXQHXPlayer getPlayer() {
		return player;
	}

	public void setPlayer(IXQHXPlayer player) {
		this.player = player;
	}

}
