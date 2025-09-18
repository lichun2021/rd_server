package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 攻击迷雾要塞事件
 * @author golden
 *
 */
public class EventAttackFoggy extends MissionEvent {

	private int foggyId;
	
	private int foggyLvl;
	
	private boolean isWin;

	private boolean isMass;
	private String marchId;
	
	public EventAttackFoggy(int foggyId, int foggyLvl, boolean isWin, boolean isMass,String marchId) {
		this.foggyId = foggyId;
		this.foggyLvl = foggyLvl;
		this.isWin = isWin;
		this.isMass = isMass;
		this.marchId = marchId;
	}

	public int getFoggyId() {
		return foggyId;
	}

	public void setFoggyId(int foggyId) {
		this.foggyId = foggyId;
	}

	public int getFoggyLvl() {
		return foggyLvl;
	}

	public void setFoggyLvl(int foggyLvl) {
		this.foggyLvl = foggyLvl;
	}

	public boolean isWin() {
		return isWin;
	}

	public void setWin(boolean isWin) {
		this.isWin = isWin;
	}
	
	public boolean isMass() {
		return isMass;
	}

	public void setMass(boolean isMass) {
		this.isMass = isMass;
	}

	public String getMarchId() {
		return marchId;
	}

	public void setMarchId(String marchId) {
		this.marchId = marchId;
	}

	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MISSION_ATK_FOGGY_WIN_TIMES);
		touchMissionList.add(MissionType.MISSION_ATK_FOGGY_WIN_TIMES_CUMULATIVE);
		touchMissionList.add(MissionType.MISSION_MASS_ATK_FOGGY_WIN_TIMES);
		return touchMissionList;
	}
}
