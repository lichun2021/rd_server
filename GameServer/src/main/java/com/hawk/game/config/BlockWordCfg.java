package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 屏蔽字配置
 * 
 * @author shadow
 *
 */
@HawkConfigManager.XmlResource(file = "xml/block_word.xml")
public class BlockWordCfg extends HawkConfigBase {

	@Id
	protected final int id;
	protected final String name;

	public BlockWordCfg() {
		this.id = 0;
		this.name = "";
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}
}
