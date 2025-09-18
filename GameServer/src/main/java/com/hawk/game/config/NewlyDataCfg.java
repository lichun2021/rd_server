package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 新手数据配置
 * 
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/initialization.xml")
public class NewlyDataCfg extends HawkConfigBase {
	/**
	 * id
	 */
	@Id
	protected final int id;

	/**
	 * 类型
	 */
	protected final int type;

	/**
	 * 值
	 */
	protected final String value;

	public NewlyDataCfg() {
		this.id = 0;
		this.type = 0;
		this.value = "";
	}

	public int getId() {
		return id;
	}

	public int getType() {
		return type;
	}

	public String getValue() {
		return value;
	}
}
