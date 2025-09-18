package com.hawk.game.invoker;

import java.util.ArrayList;
import java.util.List;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import com.hawk.game.city.CityManager;
import com.hawk.game.player.Player;

public class CityOnFireMsgInvoker extends HawkMsgInvoker {
	
	private Player atkLeader;
	private Player defPlayer;
	private List<Integer> heroIdList = new ArrayList<>();
	
	public CityOnFireMsgInvoker(Player atkLeader, Player defPlayer, List<Integer> heroIdList) {
		this.atkLeader = atkLeader;
		this.defPlayer = defPlayer;
		if (heroIdList != null && !heroIdList.isEmpty()) {
			this.heroIdList.addAll(heroIdList);
		}
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		CityManager.getInstance().cityOnFire(atkLeader, defPlayer, heroIdList);
		return true;
	}

	public Player getAtkLeader() {
		return atkLeader;
	}

	public Player getDefPlayer() {
		return defPlayer;
	}

	public List<Integer> getHeroIdList() {
		return heroIdList;
	}

}
