package com.hawk.activity.type.impl.powercollect.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.KVResource(file = "activity/super_lab/super_lab_cfg.xml")
public class PowerCollectKVCfg extends HawkConfigBase {
	
	private final long serverDelay;
	
	private final int personRankSize;
	
	private final int guildRankSize;
	
	private final int itemId;
	
	/** 个人排行奖励邮件 **/
	private final int personalRankAwardMail;
	
	/** 联盟排名奖励邮件 **/
	private final int allianceRankAwardMail;
	
	public PowerCollectKVCfg(){
		this.serverDelay = 0;
		this.personRankSize = 0;
		this.guildRankSize = 0;
		this.itemId = 0;
		this.personalRankAwardMail = 0;
		this.allianceRankAwardMail = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public int getPersonRankSize() {
		return personRankSize;
	}

	public int getGuildRankSize() {
		return guildRankSize;
	}

	public int getItemId() {
		return itemId;
	}

	public int getPersonalRankAwardMail() {
		return personalRankAwardMail;
	}

	public int getAllianceRankAwardMail() {
		return allianceRankAwardMail;
	}
}
