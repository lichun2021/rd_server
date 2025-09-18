package com.hawk.activity.type.impl.powerfund.cfg;

import java.security.InvalidParameterException;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

import com.hawk.gamelib.activity.ConfigChecker;

/**
 * 战力基金活动全局K-V配置
 * @author Jesse
 *
 */
@HawkConfigManager.KVResource(file = "activity/power_fund/powerfund_activity_cfg.xml")
public class PowerFundActivityKVCfg extends HawkConfigBase {
	
	/** 激活基金直购类型*/
	private final int payGiftType;
	
	private final String endDate;
	
	private final String restartDate;
	
	
	private long endDateTimeValue;
	private long restartDateTimeValue;
	
	/** 购买价格*/
	private final String price;
	
	public PowerFundActivityKVCfg() {
		payGiftType = 0;
		endDate = "";
		restartDate = "";
		price = "";
	}
	
	@Override
	protected boolean assemble() {
		endDateTimeValue = HawkTime.parseTime(endDate);
		restartDateTimeValue = HawkTime.parseTime(restartDate);
		return true;
	}

	public int getPayGiftType() {
		return payGiftType;
	}
	
	
	
	public long getEndDateTimeValue() {
		return endDateTimeValue;
	}

	
	public long getRestartDateTimeValue() {
		return restartDateTimeValue;
	}

	public String getPrice() {
		return price;
	}
	
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(price);
		if (!valid) {
			throw new InvalidParameterException(String.format("PowerFundActivityKVCfg reward error, price: %s", price));
		}
		return super.checkValid();
	}
}