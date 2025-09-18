package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 添加旗帜（包括夺旗获得的）事件
 * 
 */
public class AddBannerEvent extends ActivityEvent {
	
	private String guildId;
	
	private int bannerCount;

	public AddBannerEvent(){ super(null);}
	public AddBannerEvent(String guildId, int bannerCount) {
		super("");
		this.guildId = guildId;
		this.bannerCount = bannerCount;
	}
	
	public String getGuildId() {
		return guildId;
	}
	
	public int getBannerCount() {
		return bannerCount;
	}

	@Override
	public String toString() {
		return "AddBanner: " + guildId;
	}

}
