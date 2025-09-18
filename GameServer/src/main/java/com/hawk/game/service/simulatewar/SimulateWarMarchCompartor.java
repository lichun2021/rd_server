package com.hawk.game.service.simulatewar;

import java.util.Comparator;

import com.hawk.game.protocol.SimulateWar.PBSimulateWarBattleData;



public class SimulateWarMarchCompartor implements Comparator<PBSimulateWarBattleData> {

	@Override
	public int compare(PBSimulateWarBattleData data1, PBSimulateWarBattleData data2) {
		return data2.getBattleValue() - data1.getBattleValue();
	}

}
