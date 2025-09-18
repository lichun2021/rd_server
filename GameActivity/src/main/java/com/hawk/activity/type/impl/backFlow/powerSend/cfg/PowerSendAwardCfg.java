package com.hawk.activity.type.impl.backFlow.powerSend.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;


@HawkConfigManager.XmlResource(file = "activity/return_send_gift/return_send_gift.xml")
public class PowerSendAwardCfg extends HawkConfigBase{
	@Id
	private final int id;
	
	private final int playerType;
	
	//可发送次数
	private final int dailySendGiftLimit;
	
	//可收到的回礼次数
	private final int dailyGainReceiptLimit;
	
	//可收到的回礼
	private final String gainReceiptReward;
	
	
	//可收到的信息次数
	private final int dailyGainMessageLimit;
	
	//可获取信息礼品的次数
	private final int dailyGainGiftLimit;
	
	//收获信息的礼品
	private final String gainGiftReward;
	

	
	public PowerSendAwardCfg(){
		id = 0;
		playerType = 0;
		dailySendGiftLimit = 0;
		dailyGainReceiptLimit = 0;
		gainReceiptReward = "";
		dailyGainMessageLimit = 0;
		dailyGainGiftLimit = 0;
		gainGiftReward = "";
		
	}

	
	
	@Override
	protected boolean assemble() {
		return super.assemble();
	}



	public int getId() {
		return id;
	}



	public int getPlayerType() {
		return playerType;
	}



	public int getDailySendGiftLimit() {
		return dailySendGiftLimit;
	}



	public int getDailyGainReceiptLimit() {
		return dailyGainReceiptLimit;
	}



	public String getGainReceiptReward() {
		return gainReceiptReward;
	}



	public int getDailyGainMessageLimit() {
		return dailyGainMessageLimit;
	}



	public int getDailyGainGiftLimit() {
		return dailyGainGiftLimit;
	}



	public String getGainGiftReward() {
		return gainGiftReward;
	}


	


	
	
}
