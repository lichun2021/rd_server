package com.hawk.game.queryentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * 城防信息
 *
 * @author lating
 *
 */
@Entity
public class CityDefInfo {
	@Id
	@Column(name = "playerId")
	protected String playerId;

	@Column(name = "onFireEndTime")
	protected long onFireEndTime = 0;
	
	@Column(name = "cityDefVal")
	private int cityDefVal = 0;
	
	@Column(name = "cityDefConsumeTime")
	private long cityDefConsumeTime = 0;
	
	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public long getOnFireEndTime() {
		return onFireEndTime;
	}

	public void setOnFireEndTime(long onFireEndTime) {
		this.onFireEndTime = onFireEndTime;
	}
	
	public int getCityDefVal() {
		return cityDefVal;
	}

	public void setCityDefVal(int cityDefVal) {
		this.cityDefVal = cityDefVal;
	}
	
	public long getCityDefConsumeTime() {
		return cityDefConsumeTime;
	}

	public void setCityDefConsumeTime(long cityDefConsumeTime) {
		this.cityDefConsumeTime = cityDefConsumeTime;
	}
}
