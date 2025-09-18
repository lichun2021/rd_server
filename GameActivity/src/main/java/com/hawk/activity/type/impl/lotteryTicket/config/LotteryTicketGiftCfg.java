package com.hawk.activity.type.impl.lotteryTicket.config;

import java.util.HashMap;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
@HawkConfigManager.XmlResource(file = "activity/lottery_ticket/lottery_ticket_shop.xml")
public class LotteryTicketGiftCfg extends HawkConfigBase{
	@Id
	private final int id;
	private final int times;
	private final String iosPayId;
	private final String androidPayID;
	private final int payQuota;
	private final String reward;
	
	private static Map<String,Integer> giftMap = new HashMap<>();
	
	public LotteryTicketGiftCfg() {
		this.id = 0;
		this.times = 0;
		this.iosPayId = "";
		this.androidPayID = "";
		this.payQuota = 0;
		this.reward = "";
	}
	

	public int getId() {
		return id;
	}
	
	
	public int getTimes() {
		return times;
	}
	
	
	public int getPayQuota() {
		return payQuota;
	}
	
	public String getReward() {
		return reward;
	}
	
	
	public static int getBuyId(String giftId){
		return giftMap.getOrDefault(giftId, 0);
	}
	
	public boolean assemble() {
		giftMap.put(this.iosPayId,this.id);
		giftMap.put(this.androidPayID,this.id);
		return true;
	}
	
	
	
}
