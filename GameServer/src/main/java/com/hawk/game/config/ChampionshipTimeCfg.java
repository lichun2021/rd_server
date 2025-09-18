package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

/**
 * 锦标赛时间配置
 * 
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "xml/championship_time.xml")
public class ChampionshipTimeCfg extends HawkConfigBase {
	@Id
	private final int termId;
	

	/** 活动开启时间 */
	private final String showStartTime;

	/** 报名开启时间 */
	private final String signStartTime;

	/** 匹配开始时间 */
	private final String matchStartTime;

	/** 战斗阶段开始时间 */
	private final String warStartTime;
	
	/** 战斗运算开始时间*/
	private final String warCalcTime;

	/** 16进8时间 */
	private final String war16to8Time;

	/** 8进4时间 */
	private final String war8to4Time;

	/** 4进2时间 */
	private final String war4to2Time;

	/** 2进1时间 */
	private final String war2to1Time;

	/** 结算开启时间 */
	private final String endStartTime;

	/** 活动关闭时间 */
	private final String hiddenTime;

	/** 开放服务器列表 */
	private final String limitServer;

	/** 关闭服务器列表 */
	private final String forbidServer;

	private long showStartTimeValue;

	private long signStartTimeValue;

	private long matchStartTimeValue;

	private long warStartTimeValue;
	
	private long warCalcTimeValue;

	private long war16to8TimeValue;

	private long war8to4TimeValue;

	private long war4to2TimeValue;

	private long war2to1TimeValue;

	private long endStartTimeValue;

	private long hiddenTimeValue;

	private List<String> limitServerList;

	private List<String> forbidServerList;

	public ChampionshipTimeCfg() {
		termId = 0;
		showStartTime = "";
		signStartTime = "";
		matchStartTime = "";
		warStartTime = "";
		warCalcTime = "";
		war16to8Time = "";
		war8to4Time = "";
		war4to2Time = "";
		war2to1Time = "";
		endStartTime = "";
		hiddenTime = "";
		limitServer = "";
		forbidServer = "";
	}

	public int getTermId() {
		return termId;
	}

	public long getShowStartTimeValue() {
		return showStartTimeValue;
	}

	public final long getSignStartTimeValue() {
		return signStartTimeValue;
	}

	public long getMatchStartTimeValue() {
		return matchStartTimeValue;
	}
	
	public long getWarStartTimeValue() {
		return warStartTimeValue;
	}

	public long getWarCalcTimeValue() {
		return warCalcTimeValue;
	}

	public long getWar16to8TimeValue() {
		return war16to8TimeValue;
	}

	public long getWar8to4TimeValue() {
		return war8to4TimeValue;
	}

	public long getWar4to2TimeValue() {
		return war4to2TimeValue;
	}

	public long getWar2to1TimeValue() {
		return war2to1TimeValue;
	}

	public long getEndStartTimeValue() {
		return endStartTimeValue;
	}

	public long getHiddenTimeValue() {
		return hiddenTimeValue;
	}

	/**
	 * 获取可开启本期锦标赛区服列表
	 * 
	 * @return
	 */
	public List<String> getLimitServerList() {
		List<String> copy = new ArrayList<>();
		for (String serverId : limitServerList) {
			copy.add(serverId);
		}
		return copy;
	}

	/**
	 * 获取禁止开启本期锦标赛区服列表
	 * 
	 * @return
	 */
	public List<String> getForbidServerList() {
		List<String> copy = new ArrayList<>();
		for (String serverId : forbidServerList) {
			copy.add(serverId);
		}
		return copy;
	}

	protected boolean assemble() {
		showStartTimeValue = HawkTime.parseTime(showStartTime);
		signStartTimeValue = HawkTime.parseTime(signStartTime);
		matchStartTimeValue = HawkTime.parseTime(matchStartTime);
		warStartTimeValue = HawkTime.parseTime(warStartTime);
		warCalcTimeValue = HawkTime.parseTime(warCalcTime);
		war16to8TimeValue = HawkTime.parseTime(war16to8Time);
		war8to4TimeValue = HawkTime.parseTime(war8to4Time);
		war4to2TimeValue = HawkTime.parseTime(war4to2Time);
		war2to1TimeValue = HawkTime.parseTime(war2to1Time);
		endStartTimeValue = HawkTime.parseTime(endStartTime);
		hiddenTimeValue = HawkTime.parseTime(hiddenTime);
		
		limitServerList = new ArrayList<>();
		forbidServerList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(limitServer)) {
			for (String serverId : limitServer.split(",")) {
				limitServerList.add(serverId);
			}
		}
		if (!HawkOSOperator.isEmptyString(forbidServer)) {
			for (String serverId : forbidServer.split(",")) {
				forbidServerList.add(serverId);
			}
		}
		return true;
	}

	@Override
	protected boolean checkValid() {
		ConfigIterator<ChampionshipTimeCfg> it = HawkConfigManager.getInstance().getConfigIterator(ChampionshipTimeCfg.class);
		long baseTermId = 0;
		for (ChampionshipTimeCfg timeCfg : it) {
			int termId = timeCfg.getTermId();
			if (termId <= baseTermId) {
				HawkLog.errPrintln(" CrossTimeCfg check valid failed, term order error:termId: {}", termId);
				return false;
			}
			baseTermId = termId;
			//TODO  时间校验
//			long signStartTime = timeCfg.getSignStartTimeValue();
//			long signEndTime = timeCfg.getSignEndTimeValue();
//			long matchEndTime = timeCfg.getMatchEndTimeValue();
//			long warStartTime = timeCfg.getWarStartTimeValue();
//			long warEndTime = timeCfg.getWarEndTimeValue();
//
//			if (signEndTime < signStartTime || matchEndTime < signEndTime || warStartTime < signEndTime || warEndTime < matchEndTime || warEndTime < warStartTime) {
//				HawkLog.errPrintln(" CrossTimeCfg check valid failed, termId: {}", termId);
//				return false;
//			}
		}
		return true;
	}
}
