package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 联盟旗帜配置
 * 
 * @author shadow
 *
 */
@HawkConfigManager.XmlResource(file = "xml/alliance_flag.xml")
public class GuildFlagCfg extends HawkConfigBase {
	public static final int NORMAL = 1;
	public static final int REWARD = 2;


	@Id
	protected final int id;

	protected final int type;

	protected final String pic;

	public GuildFlagCfg() {
		this.id = 0;
		this.type = 0;
		this.pic = null;
	}

	public int getId() {
		return id;
	}

	public int getType() {
		return type;
	}

	public String getPic() {
		return pic;
	}
}
