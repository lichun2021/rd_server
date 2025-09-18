package com.hawk.game.service.simulatewar;

import java.util.Comparator;

import com.hawk.game.protocol.SimulateWar.SimulateWarBattlePlayer;
import com.hawk.game.protocol.SimulateWar.SimulateWarBattlePlayer.Builder;

public class SimulateWarMvpCompartor implements Comparator<SimulateWarBattlePlayer.Builder> {

	@Override
	public int compare(Builder builder1, Builder builder2) {
		if (builder1.getKillCount() == builder2.getKillCount()) {
			if (builder2.getPlayer().getBattlePoint() == builder1.getPlayer().getBattlePoint()) {
				return 0;
			}
			return builder2.getPlayer().getBattlePoint() > builder1.getPlayer().getBattlePoint() ? 1 : -1;
		} else {
			return builder2.getKillCount() - builder1.getKillCount();
		}	
	}

}
