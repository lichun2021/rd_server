package com.hawk.game.config;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "xml/guildRank.xml")
public class GuildRankCfg extends HawkConfigBase {
	@Id
	protected final int id;// ="1"
	
	protected final int close_day;// ="0" 排行榜关闭日期
	protected final int open_day;// ="15" 排行榜开启日期 
	protected final String name;//  //不知道干嘛用的
	protected final int mailTopNumber; // 前多少名发邮件
	protected final int overdueTime;
	public GuildRankCfg() {
		id = 0;
		close_day = 0;
		open_day = 0;
		name = "";
		mailTopNumber = 0;
		overdueTime = 0;
	}

	public int getId() {
		return id;
	}

	public int getCloseDay() {
		return close_day;
	}

	public int getOpenDay() {
		return open_day;
	}
	
	public int getMailTopNumber() {
		return mailTopNumber;
	}
	
	public int getOverdueTime(){
		return overdueTime;
	}
}
