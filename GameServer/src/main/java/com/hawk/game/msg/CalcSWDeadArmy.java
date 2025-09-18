package com.hawk.game.msg;

import java.util.List;

import org.hawk.msg.HawkMsg;

import com.hawk.game.march.ArmyInfo;
import com.hawk.gamelib.GameConst.MsgId;

/**
 * 消息 - 死兵计算
 * 
 * @author golden
 *
 */
public class CalcSWDeadArmy extends HawkMsg {
	
	private List<ArmyInfo> armyDeadList;
	/**
	 * 是否是gm命令触发
	 */
	private boolean gmTrigger = false;
	
	public CalcSWDeadArmy(List<ArmyInfo> armyDeadList) {
		super(MsgId.CALC_SW_DEAD_ARMY);
		this.armyDeadList = armyDeadList;
	}

	public List<ArmyInfo> getArmyDeadList() {
		return armyDeadList;
	}

	public void setArmyDeadList(List<ArmyInfo> armyDeadList) {
		this.armyDeadList = armyDeadList;
	}
	
	public static CalcSWDeadArmy valueOf(List<ArmyInfo> armyDeadList) {
		return new CalcSWDeadArmy(armyDeadList);
	}
	
	public boolean isGmTrigger() {
		return gmTrigger;
	}

	public void setGmTrigger(boolean gmTrigger) {
		this.gmTrigger = gmTrigger;
	}
}
