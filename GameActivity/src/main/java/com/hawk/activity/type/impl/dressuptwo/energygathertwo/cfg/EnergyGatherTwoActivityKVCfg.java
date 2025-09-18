package com.hawk.activity.type.impl.dressuptwo.energygathertwo.cfg;

import com.hawk.serialize.string.SerializeHelper;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import java.util.Map;

/**
 * 圣诞节系列活动一:冰雪计划活动
 * @author hf
 *
 */
@HawkConfigManager.KVResource(file = "activity/christmas_snow_plan/christmas_snow_plan_cfg.xml")
public class EnergyGatherTwoActivityKVCfg extends HawkConfigBase {
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;
	/**
	 * 此活动每日必买礼包对应的成就完成次数
	 */
	private final String giftNumStr;

	private Map<String, Integer> giftNumMap;

	public EnergyGatherTwoActivityKVCfg() {
		serverDelay = 0;
		giftNumStr = "";
	}

	@Override
	protected boolean assemble() {
		giftNumMap = SerializeHelper.stringToMap(giftNumStr, String.class, Integer.class, "_", ",");
		return true;
	}

	public long getServerDelay() {
		return serverDelay * 1000L;
	}

	public Map<String, Integer> getGiftNumMap() {
		return giftNumMap;
	}

	public void setGiftNumMap(Map<String, Integer> giftNumMap) {
		this.giftNumMap = giftNumMap;
	}
}
