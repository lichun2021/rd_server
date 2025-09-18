package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/***
 * 福袋分享配置
 * @author yang.rao
 *
 */

@HawkConfigManager.KVResource(file = "xml/daily_gift.xml")
public class LuckyBagShareCfg extends HawkConfigBase {
	
	/** 活动id(腾讯提供) **/
	private final String actid;
	
	private final String wxNoticeid;
	
	/** 根据腾讯提供的要求，此值默认为10 **/
	private final int wxNum;
	
	/** 密钥 **/
	private final String wxKey;
	
	private final String wxLuckyBagUrl;
	
	/** 超时时间 **/
	private final int timeout;
	
	private static LuckyBagShareCfg instance;

	private final String qqLuckyBagUrl;
	
	/** qq平台的密钥 **/
	private final String qqKey;
	
	private final String qqAppid;
	
	private final String qqActid;
	
	private final String _c;
	
	private final int g_tk;
	
	private final String qqFrom;
	
	
	public static LuckyBagShareCfg getInstance(){
		return instance;
	}
	
	public LuckyBagShareCfg(){
		actid = "";
		wxNoticeid = "";
		wxNum = 0;
		wxKey = "";
		wxLuckyBagUrl = "";
		timeout = 0;
		qqLuckyBagUrl = "";
		qqKey = "";
		qqAppid = "";
		qqActid = "";
		_c = "";
		g_tk = 0;
		qqFrom = "";
		instance = this;
	}

	public String getActid() {
		return actid;
	}

	public String getWxNoticeid() {
		return wxNoticeid;
	}

	public int getWxNum() {
		return wxNum;
	}

	public String getWxKey() {
		return wxKey;
	}

	public String getWxLuckyBagUrl() {
		return wxLuckyBagUrl;
	}

	public int getTimeout() {
		return timeout;
	}

	public String getQqLuckyBagUrl() {
		return qqLuckyBagUrl;
	}

	public String getQqKey() {
		return qqKey;
	}

	public String getQqAppid() {
		return qqAppid;
	}

	public String getQqActid() {
		return qqActid;
	}

	public String get_c() {
		return _c;
	}

	public int getG_tk() {
		return g_tk;
	}

	public String getQqFrom() {
		return qqFrom;
	}
}
