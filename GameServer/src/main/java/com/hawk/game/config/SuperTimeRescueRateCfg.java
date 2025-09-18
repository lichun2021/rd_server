package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;

import com.hawk.game.util.GsConst;

/**
 * 超时空救援兵种等级转化比例配置
 *
 * @author Jesse
 *
 */
//@HawkConfigManager.XmlResource(file = "xml/superTimeRescueRate.xml")
public class SuperTimeRescueRateCfg extends HawkConfigBase {
	// 兵种等级
	@Id
	private final int id;
	
	// 转化比例万分比
	private final int rate;

	public SuperTimeRescueRateCfg() {
		id = 0;
		rate = 0;
	}

	public int getId() {
		return id;
	}

	public int getRate() {
		return rate;
	}

	@Override
	protected boolean checkValid() {
		// 救援比例不能超过百分百
		if(rate > GsConst.EFF_RATE){
			return false;
		}
		return super.checkValid();
	}
	
}
