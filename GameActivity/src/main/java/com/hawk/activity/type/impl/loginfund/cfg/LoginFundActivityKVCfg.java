package com.hawk.activity.type.impl.loginfund.cfg;

import java.security.InvalidParameterException;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.gamelib.activity.ConfigChecker;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

/**
 * 登录基金活动全局K-V配置
 * @author PhilChen
 *
 */
@HawkConfigManager.KVResource(file = "activity/login_fund/loginfund_activity_cfg.xml")
public class LoginFundActivityKVCfg extends HawkConfigBase {
	
	/** 服务器开服延时开启活动时间*/
	private final int serverDelay;
	
	/** 可购买登录基金的vip等级限制*/
	private final int limitVipLevel;
	
	/** 购买价格*/
	private final String price;

	/**活动开启的服务器开服时间*/
	private final String openTimeBegin;
	/**活动截止的服务器开服时间*/
	private final String openTimeEnd;

	private long openTimeBeginValue;

	private long openTimeEndValue;

	public LoginFundActivityKVCfg() {
		serverDelay = 0;
		limitVipLevel = 0;
		price = "";
		openTimeBegin = "";
		openTimeEnd = "";
	}

	@Override
	protected boolean assemble() {
		if (!HawkOSOperator.isEmptyString(openTimeBegin)) {
			openTimeBeginValue = HawkTime.parseTime(openTimeBegin);
		}
		if (!HawkOSOperator.isEmptyString(openTimeEnd)) {
			openTimeEndValue = HawkTime.parseTime(openTimeEnd);
		}
		return true;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public int getLimitVipLevel() {
		return limitVipLevel;
	}

	public String getPrice() {
		return price;
	}

	public long getOpenTimeBeginValue() {
		return openTimeBeginValue;
	}

	public long getOpenTimeEndValue() {
		return openTimeEndValue;
	}
	
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(price);
		if (!valid) {
			throw new InvalidParameterException(String.format("LoginFundActivityKVCfg reward error, price: %s", price));
		}
		return super.checkValid();
	}

}