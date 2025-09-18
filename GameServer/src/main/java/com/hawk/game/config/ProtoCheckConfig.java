package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 协议检测
 * @author Golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/proto_check.xml")
public class ProtoCheckConfig extends HawkConfigBase {

	/**
	 * 协议id
	 */
	@Id
	protected final int protoType;
	
	/**
	 * x秒之内(配置1m以上,最好是60s的整数倍)
	 */
	protected final int cacheSecond;
	
	/**
	 * 数量限制
	 */
	protected final int countLimit;
	
	/**
	 * 触发后禁止协议时间
	 */
	protected final int banSecond;
	
	/**协议按周期处理, 控制频率 当前只用在worldmove. 如需要,可升级到player onprotol层面*/
	protected final int tickPeriod;
	
	public ProtoCheckConfig() {
		this.protoType = 0;
		this.cacheSecond = 60 * 10;
		this.countLimit = 1000;
		this.banSecond = 300;
		this.tickPeriod = 0;
	}

	public int getProtoType() {
		return protoType;
	}

	public int getCacheSecond() {
		return cacheSecond;
	}

	public int getCountLimit() {
		return countLimit;
	}

	public int getBanSecond() {
		return banSecond;
	}

	public int getTickPeriod() {
		return tickPeriod;
	}
	
}
