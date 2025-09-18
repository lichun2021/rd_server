package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.StrongestEvent;
import com.hawk.gamelib.player.PowerChangeData;
import com.hawk.gamelib.player.PowerData;
import com.hawk.log.LogConst.PowerChangeReason;

/**
 * 战斗力变化事件(战力值)
 * @author PhilChen
 *
 */
public class BattlePointChangeEvent extends ActivityEvent implements StrongestEvent {

	private PowerData powerData;
	
	private PowerChangeData changeData;
	
	private boolean isSoliderCure;
	
	private PowerChangeReason reason;

	public BattlePointChangeEvent(){ super(null);}
	public BattlePointChangeEvent(String playerId, PowerData powerData, PowerChangeData changeData, boolean isSoliderCure, PowerChangeReason reason) {
		super(playerId);
		this.powerData = powerData;
		this.changeData = changeData;
		this.isSoliderCure = isSoliderCure;
		this.reason = reason;

	}

	public PowerData getPowerData() {
		return powerData;
	}

	public PowerChangeData getChangeData() {
		return changeData;
	}

	public boolean isSoliderCure() {
		return isSoliderCure;
	}
	
	public PowerChangeReason getReason() {
		return reason;
	}

	public void summation(PowerData powerData, PowerChangeData changeData){
		this.powerData = powerData;
		this.changeData.addChangeData(changeData);
		
	}

	@Override
	public String toString() {
		return String.format("BattlePointChange, total:%d,1:%d,2:%d,3:%d,4:%d,5:%d,6:%d,7:%s",
				powerData.getBattlePoint(), powerData.getArmyBattlePoint(), powerData.getTechBattlePoint(),
				powerData.getBuildBattlePoint(), changeData.getArmyBattleChange(), changeData.getTechBattleChange(),
				changeData.getBuildBattleChange(), isSoliderCure);
	}
}
