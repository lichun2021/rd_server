package com.hawk.activity.type.impl.growfundnew.cfg;

import java.security.InvalidParameterException;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.gamelib.activity.ConfigChecker;

/**
 * 新成长基金活动全局K-V配置
 * @author lating
 *
 */
@HawkConfigManager.KVResource(file = "activity/new_grow_fund/%s/new_growfund_activity_cfg.xml", autoLoad=false, loadParams="263")
public class GrowFundNewActivityKVCfg extends HawkConfigBase {
	
	/** 服务器开服延时开启活动时间*/
	private final int serverDelay;
	
	/** 可购买成长基金的vip等级限制*/
	private final int limitVipLevel;
	
	/** 购买价格*/
	private final String costNum;
	
	public GrowFundNewActivityKVCfg() {
		serverDelay = 0;
		limitVipLevel = 0;
		costNum = "";
	}
	
	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public int getLimitVipLevel() {
		return limitVipLevel;
	}

	public String getCostNum() {
		return costNum;
	}
	
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(costNum);
		if (!valid) {
			throw new InvalidParameterException(String.format("GrowFundActivityKVCfg reward error, costNum: %s", costNum));
		}
		return super.checkValid();
	}
}