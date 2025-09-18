package com.hawk.activity.type.impl.backFlow.powerSend.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
/**
 * 赠送体力活动
 * 
 * @author che
 *
 */
@HawkConfigManager.KVResource(file = "activity/return_send_gift/return_send_gift_cfg.xml")
public class PowerSendKVCfg extends HawkConfigBase {
	


	//赠送体力数量
	private final int serverDelay;
	
	//非回流玩家每日可接受消息次数限制
	private final int dailyGainMessageLimit;
	
	//非回流玩家每日可接受礼物奖励数限制
	private final int dailyGainGiftLimit;
	
	//非回流玩家单次接受礼物奖励内容
	private final String gainGiftReward;
	


	public PowerSendKVCfg() {
		serverDelay =0;
		dailyGainMessageLimit = 0;
		dailyGainGiftLimit = 0;
		gainGiftReward = "";
		
	}



	public long getServerDelay() {
		return serverDelay * 1000L;
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
