package com.hawk.activity.type.impl.dressup.energygather.cfg;

import com.hawk.serialize.string.SerializeHelper;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import java.util.Map;

/**
 * 装扮投放系列活动二:能量聚集  配置
 * @author hf
 *
 */
@HawkConfigManager.KVResource(file = "activity/dress_energy_gather/dress_energy_gather_cfg.xml")
public class EnergyGatherActivityKVCfg extends HawkConfigBase {
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;
	/**
	 * 此活动每日必买礼包对应的成就完成次数
	 */
	private final String giftNumStr;

	private Map<String, Integer> giftNumMap;

	public EnergyGatherActivityKVCfg() {
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
