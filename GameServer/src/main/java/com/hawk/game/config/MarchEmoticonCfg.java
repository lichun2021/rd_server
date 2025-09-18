package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 行军表情配置
 *
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "xml/march_emoji_emoticon.xml")
public class MarchEmoticonCfg extends HawkConfigBase {
	@Id
	protected final int id;
	// 是否默认解锁
	protected final int defaultUnlock;
	// 所属的表情包
	protected final int emoticon;
	
	public MarchEmoticonCfg() {
		id = 0;
		defaultUnlock = 0;
		emoticon = 0;
	}
	
	public int getId() {
		return id;
	}
	
	public int getDefaultUnlock() {
		return defaultUnlock;
	}

	public int getEmoticon() {
		return emoticon;
	}

	@Override
	protected boolean assemble() {
		return true;
	}
	
}
