package com.hawk.game.lianmengjunyan.player.npc.actionScript;

import java.util.ArrayList;
import java.util.List;

import org.hawk.os.HawkRand;

import com.hawk.game.lianmengjunyan.LMJYConst.PState;
import com.hawk.game.lianmengjunyan.player.ILMJYPlayer;
import com.hawk.game.lianmengjunyan.player.npc.LMJYNPCPlayer;

public abstract class ILMJYNPCAction {
	private long lastAction;
	private long coolDown;
	private LMJYNPCPlayer parent;

	public ILMJYNPCAction(LMJYNPCPlayer parent) {
		this.parent = parent;
	}

	public abstract boolean doAction();

	public ILMJYPlayer randomEnemy() {
		List<ILMJYPlayer> playerList = new ArrayList<>();
		for (ILMJYPlayer gamer : getParent().getParent().getPlayerList(PState.GAMEING)) {
			if (gamer.isInSameGuild(getParent())) {
				continue;
			}
			playerList.add(gamer);
		}

		ILMJYPlayer tar = HawkRand.randomObject(playerList);
		return tar;
	}

	public LMJYNPCPlayer getParent() {
		return parent;
	}

	public void setParent(LMJYNPCPlayer parent) {
		this.parent = parent;
	}

	public long getLastAction() {
		return lastAction;
	}

	public void setLastAction(long lastAction) {
		this.lastAction = lastAction;
	}

	public long getCoolDown() {
		return coolDown;
	}

	public void setCoolDown(long coolDown) {
		this.coolDown = coolDown;
	}

	public abstract void inCD();

}
