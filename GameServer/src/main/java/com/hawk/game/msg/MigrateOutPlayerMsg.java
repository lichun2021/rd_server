package com.hawk.game.msg;

import org.hawk.msg.HawkFutureMsg;

import com.hawk.game.player.Player;
import com.hawk.gamelib.GameConst;

public class MigrateOutPlayerMsg extends HawkFutureMsg<Boolean> {
	private Player player;
	public MigrateOutPlayerMsg() {
		super(GameConst.MsgId.MIGRATE_OUT);
	}
	public Player getPlayer() {
		return player;
	}

	public void setPlayerId(Player player) {
		this.player = player;
	}
	
	public static MigrateOutPlayerMsg valueOf(Player player) {
		MigrateOutPlayerMsg migratePlayerMsg = new MigrateOutPlayerMsg();
		migratePlayerMsg.player = player; 
		
		return migratePlayerMsg;
	}
}
