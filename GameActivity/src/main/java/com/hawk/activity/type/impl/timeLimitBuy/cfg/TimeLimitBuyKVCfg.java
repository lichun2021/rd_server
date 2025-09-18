package com.hawk.activity.type.impl.timeLimitBuy.cfg;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

/**
 * 限时抢购
 * @author Golden
 *
 */
@HawkConfigManager.KVResource(file = "activity/time_limit_buy/time_limit_buy_cfg.xml")
public class TimeLimitBuyKVCfg extends HawkConfigBase {

	/**
	 * 起服延迟开放时间
	 */
	private final int serverDelay;
	
	/**
	 * 开启时间
	 */
	private final String openTime;
	
	/**
	 * 持续时间(分钟)
	 */
	private final int continueTime;

	/**
	 * 重置已购商品周期(毫秒)
	 */
	private final int resetGoodsBuyPeroid;
	
	/**
	 * 注水周期(毫秒)
	 */
	private final int waterFloodPeroid;
	
	/**
	 * 跑马灯时间
	 */
	private final int preheatTime;
	
	/**
	 * 开启时间
	 */
	private List<Integer> openTimeList;
	
	/**
	 * 单例
	 */
	private static TimeLimitBuyKVCfg instance;
	
	/**
	 * 获取单例
	 * @return
	 */
	public static TimeLimitBuyKVCfg getInstance(){
		return instance;
	}
	
	/**
	 * 构造
	 */
	public TimeLimitBuyKVCfg(){
		serverDelay = 0;
		openTime = "";
		continueTime = 0;
		resetGoodsBuyPeroid = 500;
		waterFloodPeroid = 5000;
		preheatTime = 10;
		instance = this;
	}

	public long getServerDelay() {
		return serverDelay * 1000L;
	}
	
	public long getContinueTime() {
		return continueTime * 60 * 1000L;
	}

	/**
	 * 重置物品周期 300ms
	 * @return
	 */
	public long getResetGoodsBuyPeroid() {
		return resetGoodsBuyPeroid * 1L;
	}

	public int getWaterFloodPeroid() {
		return waterFloodPeroid;
	}

	/**
	 * 获取开启时间
	 * @return
	 */
	public List<Integer> getOpenTimeList() {
		return openTimeList;
	}

	public long getPreheatTime() {
		return preheatTime * 60 * 1000L;
	}

	@Override
	protected boolean assemble() {
		List<Integer> openTimeList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(openTime)) {
			String[] split = openTime.split(",");
			for (int i = 0; i < split.length; i++) {
				openTimeList.add(Integer.valueOf(split[i]));
			}
		}
		this.openTimeList = openTimeList;
		return true;
	}
}
