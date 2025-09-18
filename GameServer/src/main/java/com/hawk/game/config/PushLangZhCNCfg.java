package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 推送消息语言配置
 *
 * @author david
 *
 */
@HawkConfigManager.XmlResource(file = "xml/push_lang_zh_CN.xml")
public class PushLangZhCNCfg extends HawkConfigBase {
	@Id
	protected final String id;
	// 消息
	protected final String text;

	public PushLangZhCNCfg() {
		id = "";
		text = "";
	}

	public String getId() {
		return id;
	}

	public String getText() {
		return text;
	}

}
