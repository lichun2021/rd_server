package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 系统邮件配置
 *
 * @author david
 *
 */
@HawkConfigManager.XmlResource(file = "xml/system_mail.xml")
public class SysMailCfg extends HawkConfigBase {
	@Id
	protected final int id;
	// 奖励
	protected final int award;

	public SysMailCfg() {
		id = 0;
		award = 0;
	}

	public int getId() {
		return id;
	}

	public int getAward() {
		return award;
	}
	
	@Override
	protected boolean checkValid() {
		if (award != 0) {
			AwardCfg awardCfg = HawkConfigManager.getInstance().getConfigByKey(AwardCfg.class, award);
			if (awardCfg == null) {
				return false;
			}
		}
		return true;
	}
}
