package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "xml/appUrl.xml")
public class AppUrlCfg extends HawkConfigBase {
	@Id
	/**
	 * 渠道号
	 */
	protected final String channelId;
	/**
	 * 渠道标记
	 */
	protected final int channelFlag;
	/**
	 * 更新地址
	 */
	protected final String appUrl;
	/**
	 * 地址模式
	 */
	protected final int urlMode;
	/**
	 * 更新包md5
	 */
	protected final String appMd5;
	/**
	 * 是否为默认更新
	 */
	protected final boolean isDefault;
	
	/**
	 * 默认渠道号
	 */
	private static String defauleChannelId;
	
	/**
	 * 获取默认渠道号
	 * @return
	 */
	public static String getDefauleChannelId() {
		return defauleChannelId;
	}

	public AppUrlCfg() {
		this.channelId = "";
		this.channelFlag = 0;
		this.appUrl = "";
		this.urlMode = 0;
		this.appMd5 = "";
		this.isDefault = false;
	}

	public String getChannelId() {
		return channelId;
	}

	public int getChannelFlag() {
		return channelFlag;
	}

	public String getAppUrl() {
		return appUrl;
	}

	public int getUrlMode() {
		return urlMode;
	}

	public String getAppMd5() {
		return appMd5;
	}

	public boolean isDefault() {
		return isDefault;
	}
	
	@Override
	protected boolean assemble() {
		if (this.isDefault) {
			defauleChannelId = this.channelId;
		}
		return super.assemble();
	}
}