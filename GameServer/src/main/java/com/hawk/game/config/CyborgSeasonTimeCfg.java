package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkTime;

/**
 * 赛博之战赛季时间配置
 * 
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "xml/cyborg_season_time.xml")
public class CyborgSeasonTimeCfg extends HawkConfigBase {
	/** 活动期数 */
	@Id
	private final int season;

	/** 活动开启时间 */
	private final String showTime;

	/** 报名开启时间 */
	private final String openTime;

	/** 报名结束时间 */
	private final String endShowTime;

	/** 匹配结束时间 */
	private final String endTime;

	/** 排位开启期数 */
	private final int beginTerm;

	/** 排位结束期数 */
	private final int endTerm;

	private long showTimeValue;

	private long openTimeValue;

	private long endShowTimeValue;

	private long endTimeValue;

	public CyborgSeasonTimeCfg() {
		season = 0;
		showTime = "";
		openTime = "";
		endShowTime = "";
		endTime = "";
		beginTerm = 0;
		endTerm = 0;
	}

	public int getSeason() {
		return season;
	}

	public String getShowTime() {
		return showTime;
	}

	public String getOpenTime() {
		return openTime;
	}

	public String getEndShowTime() {
		return endShowTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public int getBeginTerm() {
		return beginTerm;
	}

	public int getEndTerm() {
		return endTerm;
	}

	public long getShowTimeValue() {
		return showTimeValue;
	}

	public long getOpenTimeValue() {
		return openTimeValue;
	}

	public long getEndShowTimeValue() {
		return endShowTimeValue;
	}

	public long getEndTimeValue() {
		return endTimeValue;
	}

	protected boolean assemble() {
		showTimeValue = HawkTime.parseTime(showTime);
		openTimeValue = HawkTime.parseTime(openTime);
		endShowTimeValue = HawkTime.parseTime(endShowTime);
		endTimeValue = HawkTime.parseTime(endTime);
		return true;
	}

	@Override
	protected boolean checkValid() {
		ConfigIterator<CyborgSeasonTimeCfg> it = HawkConfigManager.getInstance().getConfigIterator(CyborgSeasonTimeCfg.class);
		int baseSeason = 0;
		int lastEndTerm = 0;
		for (CyborgSeasonTimeCfg timeCfg : it) {
			int season = timeCfg.getSeason();
			if (season <= baseSeason) {
				HawkLog.errPrintln(" CyborgSeasonTimeCfg check valid failed, season order error, season: {}", season);
				return false;
			}
			baseSeason = season;
			if (timeCfg.getBeginTerm() > timeCfg.getEndTerm()) {
				HawkLog.errPrintln(" CyborgSeasonTimeCfg check valid failed, beginTerm bigger than endTerm, season: {}, beginTerm:{}, endTerm:{}", season, timeCfg.getBeginTerm(),
						timeCfg.getEndTerm());
				return false;
			}
			if (timeCfg.getBeginTerm() <= lastEndTerm) {
				HawkLog.errPrintln(" CyborgSeasonTimeCfg check valid failed, beginTerm less than lastSeason: {}, beginTerm:{}, lastEndTerm:{}", season, timeCfg.getBeginTerm(),
						lastEndTerm);
				return false;
			}
			long showTime = timeCfg.getShowTimeValue();
			long openTime = timeCfg.getOpenTimeValue();
			long endShowTime = timeCfg.getEndShowTimeValue();
			long endTime = timeCfg.getEndTimeValue();
			int beginTermId = timeCfg.getBeginTerm();
			int endTermId = timeCfg.getEndTerm();

			if (openTime < showTime || endShowTime < openTime || endTime < endShowTime || endTermId < beginTermId) {
				HawkLog.errPrintln(" CyborgSeasonTimeCfg check valid failed, timeError, season: {}", season);
				return false;
			}
			CyborgWarTimeCfg startCfg = HawkConfigManager.getInstance().getConfigByKey(CyborgWarTimeCfg.class, timeCfg.getBeginTerm());
			CyborgWarTimeCfg endCfg = HawkConfigManager.getInstance().getConfigByKey(CyborgWarTimeCfg.class, timeCfg.getEndTerm());
			if (startCfg == null || endCfg == null) {
				HawkLog.errPrintln(" CyborgSeasonTimeCfg check valid failed, cyborgWarTime is null, season: {}", season);
				return false;
			}
			// 排位开启时间需要早于开启期数的报名时间
			if (openTime > startCfg.getSignStartTimeValue()) {
				HawkLog.errPrintln(" CyborgSeasonTimeCfg check valid failed, beginTerm cyborg alread start sign, season: {}, openTime:{}, signStartTime:{}", season,
						timeCfg.getOpenTime(), startCfg.getSignStartTime());
				return false;
			}

			// 排位结束时间需要晚于结束期数的战斗开启时间且早于结束期数的战斗结束时间
			if (!(endShowTime > endCfg.getWarStartTimeValue() && endShowTime < endCfg.getWarEndTimeValue())) {
				HawkLog.errPrintln(" CyborgSeasonTimeCfg check valid failed, endTerm time error, season: {}, endShowTime:{}, warStarTime:{}, warEndTime:{}", season,
						timeCfg.getEndShowTime(), endCfg.getWarStartTime(), endCfg.getWarEndTime());
				return false;
			}

		}
		return true;
	}
}
