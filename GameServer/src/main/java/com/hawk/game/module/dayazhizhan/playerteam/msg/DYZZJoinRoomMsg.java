package com.hawk.game.module.dayazhizhan.playerteam.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.game.module.lianmengtaiboliya.TBLYBattleRoom;
import com.hawk.game.module.lianmengtaiboliya.player.ITBLYPlayer;



/** 最终会操作玩家数据. 尤其是队列和兵的数据. 这一步要保持单线程
 * 
 * @author lwt
 * @date 2018年12月27日 */
public class DYZZJoinRoomMsg extends HawkMsg {
	private TBLYBattleRoom battleRoom;
	private ITBLYPlayer player;

	private DYZZJoinRoomMsg() {
	}

	public static DYZZJoinRoomMsg valueOf(TBLYBattleRoom room, ITBLYPlayer player) {
		DYZZJoinRoomMsg msg = new DYZZJoinRoomMsg();
		msg.battleRoom = room;
		msg.player = player;
		return msg;
	}

	public TBLYBattleRoom getBattleRoom() {
		return battleRoom;
	}

	public void setBattleRoom(TBLYBattleRoom battleRoom) {
		this.battleRoom = battleRoom;
	}

	public ITBLYPlayer getPlayer() {
		return player;
	}

	public void setPlayer(ITBLYPlayer player) {
		this.player = player;
	}

}
