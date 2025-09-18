package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/** 
 * 心悦角色交易协议控制
 */
@HawkConfigManager.XmlResource(file = "cfg/roleExchangeProto.xml")
public class RoleExchangeProtoCfg extends HawkConfigBase implements IDungeonProtoCfg {
	@Id
	protected final int id;
	/** 忽略, 不处理 */
	protected final int ignore;
	/** 禁止 */
	protected final int ban;
	/** 描述 */
	protected final String desc;

	public RoleExchangeProtoCfg() {
		id = 0;
		ignore = 0;
		ban = 0;
		desc = "";
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public int getIgnore() {
		return ignore;
	}

	@Override
	public int getBan() {
		return ban;
	}
	
	public String getDesc() {
		return desc;
	}

}
