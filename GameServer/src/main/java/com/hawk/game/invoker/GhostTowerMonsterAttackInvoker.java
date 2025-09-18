package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.module.PlayerGhostTowerModule;
import com.hawk.game.player.Player;
import com.hawk.game.util.GsConst;

/**
 * 击杀幽灵工厂怪
 * 
 * @author che
 *
 */
public class GhostTowerMonsterAttackInvoker extends HawkMsgInvoker {
	/** 玩家 */
	private Player player;
	
	private int monsterId;
	
	
	public GhostTowerMonsterAttackInvoker(Player player,int monsterId) {
		this.player = player;
		this.monsterId = monsterId;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		PlayerGhostTowerModule module = player.getModule(GsConst.ModuleType.GHOST_TOWER);
		module.ghostKilled(this.monsterId);
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public int getMonsterId() {
		return monsterId;
	}

	public void setMonsterId(int monsterId) {
		this.monsterId = monsterId;
	}

	


	
}
