package com.hawk.game.module.lianmengfgyl.battleroom.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.game.module.lianmengfgyl.battleroom.FGYLBattleRoom;
import com.hawk.game.module.lianmengfgyl.battleroom.player.IFGYLPlayer;




/** 最终会操作玩家数据. 尤其是队列和兵的数据. 这一步要保持单线程
 * 
 * @author lwt
 * @date 2018年12月27日 */
public class FGYLJoinRoomMsg extends HawkMsg {
	private FGYLBattleRoom battleRoom;
	private IFGYLPlayer player;

	private FGYLJoinRoomMsg() {
	}

	public static FGYLJoinRoomMsg valueOf(FGYLBattleRoom room, IFGYLPlayer player) {
		FGYLJoinRoomMsg msg = new FGYLJoinRoomMsg();
		msg.battleRoom = room;
		msg.player = player;
		return msg;
	}

	public FGYLBattleRoom getBattleRoom() {
		return battleRoom;
	}

	public void setBattleRoom(FGYLBattleRoom battleRoom) {
		this.battleRoom = battleRoom;
	}

	public IFGYLPlayer getPlayer() {
		return player;
	}

	public void setPlayer(IFGYLPlayer player) {
		this.player = player;
	}

}
