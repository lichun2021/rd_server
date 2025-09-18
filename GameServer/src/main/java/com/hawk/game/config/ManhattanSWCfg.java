package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

/**
 * 超武配置
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "xml/manhattan_sw.xml")
public class ManhattanSWCfg extends HawkConfigBase {

	/**
	 * 超武id
	 */
	@Id
	protected final int swId;

	/**
	 * 超武类型
	 */
	protected final int type;

	/**
	 * 解锁消耗
	 */
	protected final String unlockConsumption;
	/**
	 * 
	 */
	protected final String showTime;
	
	private long showTimeValue;
	
	public ManhattanSWCfg() {
		swId = 0;
		type = 0;
		unlockConsumption = "";
		showTime = "";
	}

	public int getSwId() {
		return swId;
	}

	public int getType() {
		return type;
	}

	public String getUnlockConsumption() {
		return unlockConsumption;
	}

	@Override
	protected boolean assemble() {
		showTimeValue = HawkTime.parseTime(showTime);
		return true;
	}
	
	public long getShowTimeValue() {
		return showTimeValue;
	}
}
