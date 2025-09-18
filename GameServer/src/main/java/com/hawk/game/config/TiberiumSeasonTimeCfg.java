package com.hawk.game.config;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.hawk.activity.extend.TiberiumSeasonTimeAbstract;
import com.hawk.game.service.tiberium.TiberiumConst.TLWWarType;

/**
 * 泰伯利亚联赛时间配置
 * 
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "xml/tiberium_season_war_time.xml")
public class TiberiumSeasonTimeCfg extends TiberiumSeasonTimeAbstract{
	
	/** 活动期数 */
	@Id
	private final int id;
	
	private final int season;
	
	private final int termId;
	
	private final int type;

	/** 报名开启时间 */
	private final String seasonStartTime;

	/** 报名结束时间 */
	private final String matchStartTime;
	
	/** 报名结束时间 */
	private final String matchEndTime;

	/** 匹配结束时间 */
	private final String manageEndTime;

	/** 战斗阶段开启时间 */
	private final String warStartTime;

	/** 战斗阶段结束时间 */
	private final String warEndTime;
	
	/** 赛季结果展示时间 */
	private final String seasonEndShowTime;
	
	/** 赛季结束时间 */
	private final String seasonEndTime;
	
	/** 胜者组免赛*/
	private final int winFree;

	private long seasonStartTimeValue;

	private long matchStartTimeValue;

	private long matchEndTimeValue;
	
	private long manageEndTimeValue;

	private long warStartTimeValue;

	private long warEndTimeValue;
	
	private long seasonEndShowTimeValue;
	
	private long seasonEndTimeValue;

	private TLWWarType warType;

	
	
	
	public TiberiumSeasonTimeCfg() {
		id = 0;
		season = 0;
		termId = 0;
		type = 0;
		seasonStartTime = "";
		matchStartTime = "";
		matchEndTime = "";
		manageEndTime = "";
		warStartTime = "";
		warEndTime = "";
		seasonEndShowTime = "";
		seasonEndTime = "";
		winFree = 0;
	}

	public int getTermId() {
		return termId;
	}


	public int getId() {
		return id;
	}

	public int getSeason() {
		return season;
	}

	public int getType() {
		return type;
	}

	public String getSeasonStartTime() {
		return seasonStartTime;
	}

	public String getMatchStartTime() {
		return matchStartTime;
	}

	public String getMatchEndTime() {
		return matchEndTime;
	}

	public String getManageEndTime() {
		return manageEndTime;
	}

	public String getWarStartTime() {
		return warStartTime;
	}

	public String getWarEndTime() {
		return warEndTime;
	}

	public long getSeasonStartTimeValue() {
		return seasonStartTimeValue;
	}

	public long getMatchStartTimeValue() {
		return matchStartTimeValue;
	}

	public long getMatchEndTimeValue() {
		return matchEndTimeValue;
	}

	public long getManageEndTimeValue() {
		return manageEndTimeValue;
	}

	public long getWarStartTimeValue() {
		return warStartTimeValue;
	}

	public long getWarEndTimeValue() {
		return warEndTimeValue;
	}

	public String getSeasonEndTime() {
		return seasonEndTime;
	}

	public long getSeasonEndTimeValue() {
		return seasonEndTimeValue;
	}

	public TLWWarType getWarType() {
		return warType;
	}

	public String getSeasonEndShowTime() {
		return seasonEndShowTime;
	}

	public long getSeasonEndShowTimeValue() {
		return seasonEndShowTimeValue;
	}

	public boolean winGroupFree() {
		return winFree > 0;
	}
	
	protected boolean assemble() {
		if (!HawkOSOperator.isEmptyString(seasonStartTime)) {
			seasonStartTimeValue = HawkTime.parseTime(seasonStartTime);
		}
		matchStartTimeValue = HawkTime.parseTime(matchStartTime);
		matchEndTimeValue = HawkTime.parseTime(matchEndTime);
		manageEndTimeValue = HawkTime.parseTime(manageEndTime);
		warStartTimeValue = HawkTime.parseTime(warStartTime);
		warEndTimeValue = HawkTime.parseTime(warEndTime);
		if (!HawkOSOperator.isEmptyString(seasonEndTime)) {
			seasonEndTimeValue = HawkTime.parseTime(seasonEndTime);
		}
		if (!HawkOSOperator.isEmptyString(seasonEndShowTime)) {
			seasonEndShowTimeValue = HawkTime.parseTime(seasonEndShowTime);
		}
		warType = TLWWarType.getType(type);
		return true;
	}

	@Override
	protected boolean checkValid() {
		ConfigIterator<TiberiumSeasonTimeCfg> it = HawkConfigManager.getInstance().getConfigIterator(TiberiumSeasonTimeCfg.class);
		long baseTermId = 0;
		long baseSeason = 0;
		for (TiberiumSeasonTimeCfg timeCfg : it) {
			if(timeCfg.getSeason() != baseSeason){
				baseSeason = timeCfg.getSeason();
				baseTermId = 0;
			}
			int termId = timeCfg.getTermId();
			if (termId <= baseTermId) {
				HawkLog.errPrintln(" TiberiumSeasonTimeCfg check valid failed, term order error:termId: {}", termId);
				return false;
			}
			baseTermId = termId;
			long seasonStartTime = timeCfg.getSeasonStartTimeValue();
			long matchStartTime = timeCfg.getMatchStartTimeValue();
			long matchEndTime = timeCfg.getMatchEndTimeValue();
			long manageEndTime = timeCfg.getManageEndTimeValue();
			long warStartTime = timeCfg.getWarStartTimeValue();
			long warEndTime = timeCfg.getWarEndTimeValue();
			long seasonEndShowTime = timeCfg.getSeasonEndShowTimeValue();
			long seasonEndTime = timeCfg.getSeasonEndTimeValue();
			if (seasonStartTime > 0 && matchStartTime < seasonStartTime) {
				HawkLog.errPrintln(" TiberiumSeasonTimeCfg check valid failed, termId: {}", termId);
				return false;
			}
			if (matchEndTime < matchStartTime || manageEndTime < matchEndTime || warStartTime < manageEndTime || warEndTime < warStartTime) {
				HawkLog.errPrintln(" TiberiumSeasonTimeCfg check valid failed, termId: {}", termId);
				return false;
			}
			if (seasonEndShowTime > 0 && (seasonEndShowTime < warEndTime || seasonEndTime < seasonEndShowTime)) {
				HawkLog.errPrintln(" TiberiumSeasonTimeCfg check valid failed, termId: {}", termId);
				return false;
			}
		}
		return true;
	}
}
