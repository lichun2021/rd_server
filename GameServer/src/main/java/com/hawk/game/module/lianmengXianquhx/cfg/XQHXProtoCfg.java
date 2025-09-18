package com.hawk.game.module.lianmengXianquhx.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.config.IDungeonProtoCfg;

/** 安全协议id
 *
 * @author hawk */
@HawkConfigManager.XmlResource(file = "cfg/xqhxProto.xml")
public class XQHXProtoCfg extends HawkConfigBase implements IDungeonProtoCfg {
	@Id
	protected final int id;
	/** 忽略, 不处理 */
	protected final int ignore;
	/** 禁止 */
	protected final int ban;
	protected final boolean multi;
	public XQHXProtoCfg() {
		id = 0;
		ignore = 0;
		ban = 0;
		multi = false;
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

	public boolean isMulti() {
		return multi;
	}

}
