package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 兵种信息配置
 *
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "xml/buff.xml")
public class BuffCfg extends HawkConfigBase {
	@Id
	protected final int id;
	// 作用号
	protected final int effect;
	// 作用号值
	protected final int value;
	// 持续时间
	protected final long time;
	//buff的使用cd
	protected final int buffCd;

	public BuffCfg() {
		id = 0;
		effect = 0;
		value = 0;
		time = 0;
		buffCd = 0;
	}

	/**
	 * buffId
	 * 
	 * @return
	 */
	public int getId() {
		return id;
	}

	/**
	 * 作用号
	 * 
	 * @return
	 */
	public int getEffect() {
		return effect;
	}

	/**
	 * 作用号值
	 * 
	 * @return
	 */
	public int getValue() {
		return value;
	}

	/**
	 * 持续时间
	 * 
	 * @return
	 */
	public long getTime() {
		return time;
	}

	public int getBuffCd() {
		return buffCd;
	}
}
