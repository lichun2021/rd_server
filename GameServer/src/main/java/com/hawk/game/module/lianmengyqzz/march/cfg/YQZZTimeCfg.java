package com.hawk.game.module.lianmengyqzz.march.cfg;

import com.hawk.game.GsConfig;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;


/**
 * 月球之战时间配置
 * @author che
 *
 */
@HawkConfigManager.XmlResource(file = "xml/moon_war_time.xml")
public class YQZZTimeCfg extends HawkConfigBase {
	/** 活动期数 */
	@Id
	private final int termId;

	/** 预览时间 */
	private final String showTime;

	/** 开启时间 */
	private final String matchTime;

	/** 结束时间 */
	private final String battleTime;
	
	/** 结算发奖时间*/
	private final String rewardTime;
	
	/** 结束展示*/
	private final String endShowTime;

	/** 消失时间 */
	private final String hiddenTime;

	private final int season;
	private final int type;
	private final int turn;

	private final String seasonStartTime;
	private final String seasonEndTime;
	
	private final int matchNeedCount;
	private final int matchListCount;
	private long showTimeValue;

	private long matchTimeValue;
	
	private long battleTimeValue;
	
	private long rewardTimeValue;

	private long endShowTimeValue;

	private long hiddenTimeValue;

	private long seasonStartTimeValue;
	private long seasonEndTimeValue;

	private static long gmTime;
	

	public YQZZTimeCfg() {
		termId = 0;
		showTime = "";
		matchTime = "";
		battleTime = "";
		rewardTime = "";
		endShowTime = "";
		hiddenTime = "";
		matchNeedCount = 3;
		matchListCount= 20;
		season = 0;
		type = 0;
		turn = 0;
		seasonStartTime = "";
		seasonEndTime = "";
	}
	
	public int getTermId() {
		return termId;
	}

	public int getSeason() {
		return season;
	}

	public int getType() {
		return type;
	}

	public int getTurn() {
		return turn;
	}

	public String getShowTime() {
		return showTime;
	}

	public String getMatchTime() {
		return matchTime;
	}
	
	public String getBattleTime() {
		return battleTime;
	}

	public String getRewardTime() {
		return rewardTime;
	}

	public String getEndShowTime() {
		return endShowTime;
	}
	
	
	public String getHiddenTime() {
		return hiddenTime;
	}

	public long getShowTimeValue() {
		return showTimeValue + gmTime;
	}

	public long getMatchTimeValue() {
		return matchTimeValue + gmTime;
	}

	public long getBattleTimeValue() {
		return battleTimeValue + gmTime;
	}
	
	public long getRewardTimeValue() {
		return rewardTimeValue + gmTime;
	}
	
	public long getEndShowTimeValue() {
		return endShowTimeValue + gmTime;
	}


	public long getHiddenTimeValue() {
		return hiddenTimeValue + gmTime;
	}

	public long getSeasonStartTimeValue() {
		return seasonStartTimeValue == 0 ? 0L : seasonStartTimeValue + gmTime;
	}

	public long getSeasonEndTimeValue() {
		return  seasonEndTimeValue == 0 ? 0L : seasonEndTimeValue + gmTime;
	}

	public int getMatchNeedCount() {
		return matchNeedCount;
	}
	
	public int getMatchListCount() {
		return matchListCount;
	}

	protected boolean assemble() {
		showTimeValue = HawkTime.parseTime(showTime);
		matchTimeValue = HawkTime.parseTime(matchTime);
		battleTimeValue = HawkTime.parseTime(battleTime);
		rewardTimeValue = HawkTime.parseTime(rewardTime);
		endShowTimeValue = HawkTime.parseTime(endShowTime);
		hiddenTimeValue = HawkTime.parseTime(hiddenTime);
		if(!HawkOSOperator.isEmptyString(seasonStartTime)){
			seasonStartTimeValue = HawkTime.parseTime(seasonStartTime);
		}
		if(!HawkOSOperator.isEmptyString(seasonEndTime)){
			seasonEndTimeValue = HawkTime.parseTime(seasonEndTime);
		}
		return true;
	}

	@Override
	protected boolean checkValid() {
		if(this.matchNeedCount > this.matchListCount){
			return false;
		}
		ConfigIterator<YQZZTimeCfg> it = HawkConfigManager.getInstance().getConfigIterator(YQZZTimeCfg.class);
		long baseTime = 0;
		long baseTermId = 0;
		for (YQZZTimeCfg timeCfg : it) {
			int termId = timeCfg.getTermId();
			if (termId <= baseTermId) {
				HawkLog.errPrintln(" YQZZTimeCfg check valid failed, term order error:termId: {}", termId);
				return false;
			}
			baseTermId = termId;
			long showTime = timeCfg.getShowTimeValue();
			long matchTime = timeCfg.getMatchTimeValue();
			long battleTime = timeCfg.getBattleTimeValue();
			long rewardTime = timeCfg.getRewardTimeValue();
			long endTime = timeCfg.getEndShowTimeValue();
			long hiddenTime = timeCfg.getHiddenTimeValue();
			
			if (showTime < baseTime || matchTime < showTime || battleTime < matchTime ||
					rewardTime < battleTime || endTime < rewardTime || hiddenTime < endTime) {
				HawkLog.errPrintln(" YQZZTimeCfg check valid failed, termId: {}", termId);
				return false;
			}
			baseTime = hiddenTime;
		}
		return true;
	}

	public static void setGmTime(long gmTime) {
		if (!GsConfig.getInstance().isDebug()) {
			return;
		}
		YQZZTimeCfg.gmTime = gmTime;

	}
}
