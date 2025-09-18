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
public class CalcDeadArmy extends HawkMsg {
	
	private List<ArmyInfo> armyDeadList;
	/**
	 * 是否是回到家的部队
	 */
	private boolean backHome;
	/**
	 * 是否是gm命令触发
	 */
	private boolean gmTrigger = false;
	
	public CalcDeadArmy(List<ArmyInfo> armyDeadList) {
		super(MsgId.CALC_DEAD_ARMY);
		this.armyDeadList = armyDeadList;
	}

	public List<ArmyInfo> getArmyDeadList() {
		return armyDeadList;
	}

	public void setArmyDeadList(List<ArmyInfo> armyDeadList) {
		this.armyDeadList = armyDeadList;
	}
	
	public static CalcDeadArmy valueOf(List<ArmyInfo> armyDeadList) {
		return new CalcDeadArmy(armyDeadList);
	}
	
	public static CalcDeadArmy valueOf(List<ArmyInfo> armyDeadList, boolean backHome) {
		CalcDeadArmy obj =  new CalcDeadArmy(armyDeadList);
		obj.setBackHome(backHome);
		return obj;
	}

	public boolean isBackHome() {
		return backHome;
	}

	public void setBackHome(boolean backHome) {
		this.backHome = backHome;
	}

	public boolean isGmTrigger() {
		return gmTrigger;
	}

	public void setGmTrigger(boolean gmTrigger) {
		this.gmTrigger = gmTrigger;
	}
}
