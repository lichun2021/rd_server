package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 攻打rts关卡
 * @author golden
 *
 */
public class EventPlotBattle extends MissionEvent {

	/**
	 * 关卡id
	 */
	int plotId;

	/**
	 * 是否胜利
	 */
	boolean isWin;
	
	public EventPlotBattle(int plotId, boolean isWin) {
		this.plotId = plotId;
		this.isWin = isWin;
	}
	
	public int getPlotId() {
		return plotId;
	}

	public boolean isWin() {
		return isWin;
	}

	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MISSION_PLOT_BATTLE);
		return touchMissionList;
	}
}
