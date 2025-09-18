package com.hawk.game.module.lianmengyqzz.battleroom.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.config.IDungeonProtoCfg;

/** 安全协议id
 *
 * @author hawk */
@HawkConfigManager.XmlResource(file = "cfg/moonWarProto.xml")
public class YQZZProtoCfg extends HawkConfigBase implements IDungeonProtoCfg {
	@Id
	protected final int id;
	/** 忽略, 不处理 */
	protected final int ignore;
	/** 禁止 */
	protected final int ban;

	protected final boolean multithread;

	public YQZZProtoCfg() {
		id = 0;
		ignore = 0;
		ban = 0;
		multithread = false;
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

	public boolean isMultithread() {
		return multithread;
	}

}
