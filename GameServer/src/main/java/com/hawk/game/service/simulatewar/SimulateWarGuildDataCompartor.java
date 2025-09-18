package com.hawk.game.service.simulatewar;

import java.util.Comparator;

import com.hawk.game.service.simulatewar.data.SimulateWarGuildData;

public class SimulateWarGuildDataCompartor implements Comparator<SimulateWarGuildData> {

	@Override
	public int compare(SimulateWarGuildData guildData, SimulateWarGuildData guildData2) {
		if (guildData2.getBattleValue() == guildData.getBattleValue()) {
			return guildData.getTeamNum() - guildData2.getTeamNum();
		} else {
			return guildData2.getBattleValue() > guildData.getBattleValue() ? 1 : -1;
		}
	}

}
