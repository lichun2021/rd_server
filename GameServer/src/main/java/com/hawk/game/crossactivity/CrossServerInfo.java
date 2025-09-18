package com.hawk.game.crossactivity;

import java.util.Date;

import org.hawk.os.HawkTime;

/**
 * 还是弄个结构体出来吧，担心后面又加其它的条件。.
 * @author jm
 *
 */
public class CrossServerInfo implements Comparable<CrossServerInfo> {
	/**
	 * 开服时间
	 */
	private String serverId;
	/**
	 * 开服时间.
	 */
	private long openServerTime;
	/**
	 * 战力
	 */
	private long battleValue;
	
	public CrossServerInfo() {
		
	}
	
	public String getServerId() {
		return serverId;
	}
	public void setServerId(String serverId) {
		this.serverId = serverId;
	}
	public long getOpenServerTime() {
		return openServerTime;
	}
	public void setOpenServerTime(long openServerTime) {
		this.openServerTime = openServerTime;
	}
	
	public long getBattleValue() {
		return battleValue;
	}

	public void setBattleValue(long battleValue) {
		this.battleValue = battleValue;
	}
	
	public int getOpenServerDays(long curTime){
		return HawkTime.calcBetweenDays(new Date(this.openServerTime), new Date(curTime));
	}
	
	
	@Override
	public int compareTo(CrossServerInfo cri) {		
		if (cri.battleValue == this.battleValue) {
			if (cri.openServerTime == this.battleValue) {
				return cri.serverId.compareTo(this.serverId); 
			} else {
				return cri.openServerTime - this.openServerTime > 0 ? -1 : 1;
			}
		} else {
			return cri.battleValue - this.battleValue > 0 ? 1 : -1;
		}		
	}
}
