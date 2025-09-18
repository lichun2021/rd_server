package com.hawk.game.msg;

import java.util.List;

import org.hawk.msg.HawkMsg;

import com.hawk.game.item.ItemInfo;

public class HospiceLostPowerMsg extends HawkMsg {
	double deadSoldierPower;
	double injuredSoldierPower;
	String atttackerId;
	List<ItemInfo> curelist;
	List<ItemInfo> deadlist;
	int overwhelming;

	/**
	 * 构造消息对象
	 * 
	 * @return
	 */
	public static HospiceLostPowerMsg valueOf(String attackerId, double deadSoldierPower, double injuredSoldierPower, List<ItemInfo> curelist, List<ItemInfo> deadlist) {
		HospiceLostPowerMsg msg = new HospiceLostPowerMsg();
		msg.atttackerId = attackerId;
		msg.deadSoldierPower = deadSoldierPower;
		msg.injuredSoldierPower = injuredSoldierPower;
		msg.curelist = curelist;
		msg.deadlist = deadlist;
		return msg;
	}

	public double getDeadSoldierPower() {
		return deadSoldierPower;
	}

	public void setDeadSoldierPower(double deadSoldierPower) {
		this.deadSoldierPower = deadSoldierPower;
	}

	public double getInjuredSoldierPower() {
		return injuredSoldierPower;
	}

	public void setInjuredSoldierPower(double injuredSoldierPower) {
		this.injuredSoldierPower = injuredSoldierPower;
	}

	public List<ItemInfo> getCurelist() {
		return curelist;
	}

	public void setCurelist(List<ItemInfo> curelist) {
		this.curelist = curelist;
	}

	public List<ItemInfo> getDeadlist() {
		return deadlist;
	}

	public void setDeadlist(List<ItemInfo> deadlist) {
		this.deadlist = deadlist;
	}

	public String getAtttackerId() {
		return atttackerId;
	}

	public void setAtttackerId(String atttackerId) {
		this.atttackerId = atttackerId;
	}

	public int getOverwhelming() {
		return overwhelming;
	}

	public void setOverwhelming(int overwhelming) {
		this.overwhelming = overwhelming;
	}

}
