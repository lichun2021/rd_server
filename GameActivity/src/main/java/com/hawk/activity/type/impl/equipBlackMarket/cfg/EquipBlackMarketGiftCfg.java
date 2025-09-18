package com.hawk.activity.type.impl.equipBlackMarket.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.gamelib.activity.ConfigChecker;


/**
 * 指挥官学院活动礼包配置
 * @author che
 *
 */
@HawkConfigManager.XmlResource(file = "activity/arms_market/arms_market_package.xml")
public class EquipBlackMarketGiftCfg extends HawkConfigBase {
	/** 对应PayGift.xml的id **/
	@Id
	private final String payGiftId;
	
	/** 1是ios 2是android **/
	private final int platform;
	

	public EquipBlackMarketGiftCfg() {
		
		this.payGiftId = "";
		this.platform = 0;
	}

	public String getPayGiftId() {
		return payGiftId;
	}

	public int getPlatform() {
		return platform;
	}



	
	@Override
	protected boolean checkValid() {
		if(platform != 1 && platform != 2){
			throw new RuntimeException("EquipBlackMarketGiftCfg 配置错误，ios平台为1，安卓平台为2，配置值为:" + platform);
		}
		return ConfigChecker.getDefaultChecker().checkPayGiftValid(payGiftId, getStrPlatform());
	}
	
	public String getStrPlatform(){
		String result = null;
		switch (platform) {
		case 1:
			result = "ios";
			break;
		case 2:
			result = "android";
			break;
		default:
			break;
		}
		return result;
	}
	
	
	
	
	
	
	
}
