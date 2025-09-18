package com.hawk.game.config;

import java.util.ArrayList;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 兵种信息配置
 *
 * @author julia
 *
 */
@HawkConfigManager.XmlResource(file = "xml/smsModel.xml")
public class SmsFormatCfg extends HawkConfigBase {
	@Id
	protected final int id;

	// 激活码
	protected final String format;

	public SmsFormatCfg() {
		id = 0;
		format = null;
	}

	protected int getId() {
		return id;
	}

	protected String getFormat() {
		return format;
	}

	@Override
	protected boolean assemble() {
		return true;
	}

	public String format(ArrayList<String> params) {
		String tempStr = format;
		for (int i = 1; i <= params.size(); i++) {
			tempStr = tempStr.replaceFirst("%var" + i + "%", params.get(i - 1));
		}
		return tempStr;
	}

}
