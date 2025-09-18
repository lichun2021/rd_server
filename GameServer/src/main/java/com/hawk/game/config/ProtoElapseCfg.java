package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 协议时间间隔配置
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "cfg/protoElapse.xml")
public class ProtoElapseCfg extends HawkConfigBase {

	/**
	 * 协议id
	 */
	@Id
	private final int protoId;
	
	/**
	 * 协议间隔时间(ms)
	 */
	private final int elapse;

	
	/**
	 * 构造
	 */
	public ProtoElapseCfg() {
		protoId = 0;
		elapse = 0;
	}
	
	
	public int getProtoId() {
		return protoId;
	}

	public int getElapse() {
		return elapse;
	}
}
